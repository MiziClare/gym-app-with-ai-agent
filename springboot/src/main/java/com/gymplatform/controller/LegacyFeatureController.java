package com.gymplatform.controller;

import com.gymplatform.service.CurrentUserService;
import com.gymplatform.service.EquipmentAvailabilityService;
import com.gymplatform.service.GymOperations;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class LegacyFeatureController {
    private final JdbcTemplate jdbc;
    private final CurrentUserService currentUserService;

    public LegacyFeatureController(JdbcTemplate jdbc, CurrentUserService currentUserService) {
        this.jdbc = jdbc;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/notices")
    List<Map<String, Object>> notices() {
        return jdbc.queryForList("""
                SELECT id, title, content, created_at AS createdAt
                FROM notices
                WHERE active = TRUE
                ORDER BY created_at DESC
                """);
    }

    @GetMapping("/coaches")
    List<Map<String, Object>> coaches() {
        return jdbc.queryForList("""
                SELECT u.id, u.username, u.display_name AS displayName, u.email,
                       p.bio, p.specialties
                FROM users u
                LEFT JOIN coach_profiles p ON p.user_id = u.id
                WHERE u.role = 'COACH' AND u.active = TRUE
                ORDER BY u.display_name
                """);
    }

    @GetMapping("/equipment")
    EquipmentAvailabilityService.Snapshot equipment() {
        return new EquipmentAvailabilityService(jdbc).snapshot();
    }

    @GetMapping("/operations/calendar")
    List<Map<String, Object>> operationsCalendar(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        if (to.isBefore(from) || from.plusDays(62).isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid calendar range");
        }
        return jdbc.queryForList("""
                SELECT id, closed_on AS closedOn, starts_at AS startsAt,
                       ends_at AS endsAt, reason
                FROM gym_closed_days
                WHERE closed_on BETWEEN ? AND ?
                ORDER BY closed_on
                """, from, to);
    }

    @GetMapping("/operations/hours")
    Map<String, Object> operationHours() {
        return jdbc.queryForMap("""
                SELECT opens_at AS opensAt, closes_at AS closesAt
                FROM gym_operation_hours WHERE id = 1
                """);
    }

    @GetMapping("/coach-availability")
    List<Map<String, Object>> coachAvailability(@RequestParam Long coachId) {
        return listCoachAvailability(coachId);
    }

    @GetMapping("/coach/availability")
    List<Map<String, Object>> myCoachAvailability(Authentication authentication) {
        return listCoachAvailability(currentUserService.require(authentication).id());
    }

    @PostMapping("/coach/availability")
    @ResponseStatus(HttpStatus.CREATED)
    void createCoachAvailability(
            @Valid @RequestBody CoachAvailabilityRequest body,
            Authentication authentication
    ) {
        if (!body.endsAt().isAfter(body.startsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        jdbc.update("""
                INSERT INTO coach_availability (coach_id, day_of_week, starts_at, ends_at)
                VALUES (?, ?, ?, ?)
                """, currentUserService.require(authentication).id(), body.dayOfWeek(), body.startsAt(), body.endsAt());
    }

    @DeleteMapping("/coach/availability/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCoachAvailability(@PathVariable Long id, Authentication authentication) {
        if (jdbc.update(
                "DELETE FROM coach_availability WHERE id = ? AND coach_id = ?",
                id, currentUserService.require(authentication).id()
        ) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found");
        }
    }

    @GetMapping("/coach-appointments/me")
    List<Map<String, Object>> myCoachAppointments(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT a.id, a.coach_id AS coachId, coach.display_name AS coachName,
                       a.starts_at AS startsAt,
                       a.starts_at + INTERVAL 60 MINUTE AS endsAt, a.note,
                       CASE
                           WHEN a.status IN ('PENDING', 'CONFIRMED')
                                AND a.starts_at + INTERVAL 60 MINUTE <= CURRENT_TIMESTAMP
                               THEN 'COMPLETED'
                           ELSE a.status
                       END AS status
                FROM coach_appointments a
                JOIN users coach ON coach.id = a.coach_id
                WHERE a.member_id = ?
                ORDER BY a.starts_at DESC
                """, currentUserService.require(authentication).id());
    }

    @PostMapping("/coach-appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void requestCoachAppointment(
            @Valid @RequestBody CoachAppointmentRequest body,
            Authentication authentication
    ) {
        var startsOn = localDate(body.startsAt());
        var startsAt = localTime(body.startsAt());
        var endsAt = startsAt.plusHours(1);
        GymOperations.requireOpen(jdbc, body.startsAt(), body.startsAt().plus(Duration.ofHours(1)));
        var availableCoaches = jdbc.queryForList(
                "SELECT id FROM users WHERE id = ? AND role = 'COACH' AND active = TRUE FOR UPDATE",
                Long.class, body.coachId());
        if (availableCoaches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found");
        }
        var hasOpenSlot = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM coach_availability
                WHERE coach_id = ? AND day_of_week = ?
                  AND starts_at <= ? AND ends_at >= ?
                """, Integer.class, body.coachId(), startsOn.getDayOfWeek().getValue(), startsAt, endsAt);
        if (hasOpenSlot == null || hasOpenSlot == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coach is not available at that time");
        }
        var conflicts = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM coach_appointments
                WHERE coach_id = ? AND starts_at = ? AND status IN ('PENDING', 'CONFIRMED')
                """, Integer.class, body.coachId(), body.startsAt());
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coach already has an appointment then");
        }
        jdbc.update("""
                INSERT INTO coach_appointments
                    (coach_id, member_id, starts_at, note)
                VALUES (?, ?, ?, ?)
                """, body.coachId(), currentUserService.require(authentication).id(),
                body.startsAt(), body.note().trim());
    }

    @DeleteMapping("/coach-appointments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelCoachAppointment(@PathVariable Long id, Authentication authentication) {
        if (jdbc.update("""
                UPDATE coach_appointments SET status = 'CANCELLED'
                WHERE id = ? AND member_id = ? AND status IN ('PENDING', 'CONFIRMED')
                  AND starts_at + INTERVAL 60 MINUTE > CURRENT_TIMESTAMP
                """, id, currentUserService.require(authentication).id()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active appointment not found");
        }
    }

    @GetMapping("/coach/appointments")
    List<Map<String, Object>> coachAppointments(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT a.id, a.member_id AS memberId, member.display_name AS memberName,
                       member.email, a.starts_at AS startsAt,
                       a.starts_at + INTERVAL 60 MINUTE AS endsAt, a.note,
                       CASE
                           WHEN a.status IN ('PENDING', 'CONFIRMED')
                                AND a.starts_at + INTERVAL 60 MINUTE <= CURRENT_TIMESTAMP
                               THEN 'COMPLETED'
                           ELSE a.status
                       END AS status
                FROM coach_appointments a
                JOIN users member ON member.id = a.member_id
                WHERE a.coach_id = ?
                ORDER BY a.starts_at DESC
                """, currentUserService.require(authentication).id());
    }

    @PatchMapping("/coach/appointments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateCoachAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest body,
            Authentication authentication
    ) {
        if (!Set.of("CONFIRMED", "CANCELLED", "COMPLETED").contains(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported appointment status");
        }
        if (jdbc.update("""
                UPDATE coach_appointments SET status = ?
                WHERE id = ? AND coach_id = ?
                """, body.status(), id, currentUserService.require(authentication).id()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
    }

    @GetMapping("/posts")
    List<Map<String, Object>> posts(
            @RequestParam(defaultValue = "default") String sort,
            Authentication authentication
    ) {
        var order = switch (sort) {
            case "latestReply" -> "COALESCE(MAX(comments.created_at), p.created_at) DESC";
            case "latest" -> "p.created_at DESC";
            case "default" -> """
                    (COUNT(DISTINCT comments.id) + COUNT(DISTINCT likes.user_id)) DESC,
                    COALESCE(MAX(comments.created_at), p.created_at) DESC
                    """;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported post sort");
        };
        return listPosts(currentUserService.require(authentication).id(), "", order);
    }

    @GetMapping("/posts/me")
    List<Map<String, Object>> myPosts(Authentication authentication) {
        var userId = currentUserService.require(authentication).id();
        return listPosts(userId, "WHERE p.author_id = " + userId, "p.created_at DESC");
    }

    @GetMapping("/posts/favorites")
    List<Map<String, Object>> favoritePosts(Authentication authentication) {
        var userId = currentUserService.require(authentication).id();
        return listPosts(userId,
                "JOIN post_favorites saved ON saved.post_id = p.id AND saved.user_id = " + userId,
                "p.created_at DESC");
    }

    @GetMapping("/posts/{id}")
    Map<String, Object> post(@PathVariable Long id, Authentication authentication) {
        var posts = listPosts(
                currentUserService.require(authentication).id(),
                "WHERE p.id = " + id,
                "p.created_at DESC");
        if (posts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return posts.getFirst();
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    void createPost(@Valid @RequestBody PostRequest body, Authentication authentication) {
        jdbc.update(
                "INSERT INTO posts (author_id, title, content) VALUES (?, ?, ?)",
                currentUserService.require(authentication).id(),
                body.title().trim(),
                body.content().trim()
        );
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePost(@PathVariable Long id, Authentication authentication) {
        if (jdbc.update(
                "DELETE FROM posts WHERE id = ? AND author_id = ?",
                id,
                currentUserService.require(authentication).id()
        ) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }

    @GetMapping("/posts/{postId}/comments")
    List<Map<String, Object>> comments(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        requirePost(postId);
        return jdbc.queryForList("""
                SELECT c.id, c.parent_id AS parentId,
                       c.author_id AS authorId, author.display_name AS authorName,
                       parent_author.display_name AS parentAuthorName,
                       c.content, c.created_at AS createdAt,
                       COUNT(likes.user_id) AS likeCount,
                       EXISTS(SELECT 1 FROM post_comment_likes mine
                              WHERE mine.comment_id = c.id AND mine.user_id = ?) AS liked
                FROM post_comments c
                JOIN users author ON author.id = c.author_id
                LEFT JOIN post_comments parent ON parent.id = c.parent_id
                LEFT JOIN users parent_author ON parent_author.id = parent.author_id
                LEFT JOIN post_comment_likes likes ON likes.comment_id = c.id
                WHERE c.post_id = ?
                GROUP BY c.id, c.parent_id, c.author_id,
                         author.display_name, parent_author.display_name,
                         c.content, c.created_at
                ORDER BY c.created_at
                """, currentUserService.require(authentication).id(), postId);
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest body,
            Authentication authentication
    ) {
        var userId = currentUserService.require(authentication).id();
        var recipientIds = body.parentId() == null
                ? jdbc.queryForList("SELECT author_id FROM posts WHERE id = ?", Long.class, postId)
                : jdbc.queryForList(
                        "SELECT author_id FROM post_comments WHERE id = ? AND post_id = ?",
                        Long.class, body.parentId(), postId);
        if (recipientIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    body.parentId() == null ? "Post not found" : "Parent comment not found");
        }
        jdbc.update("""
                INSERT INTO post_comments (post_id, author_id, parent_id, content)
                VALUES (?, ?, ?, ?)
                """, postId, userId, body.parentId(), body.content().trim());
        var recipientId = recipientIds.getFirst();
        if (!recipientId.equals(userId)) {
            jdbc.update("""
                    INSERT INTO forum_notifications
                        (recipient_id, actor_id, type, post_id, content)
                    VALUES (?, ?, ?, ?, ?)
                    """, recipientId, userId, body.parentId() == null ? "COMMENT" : "REPLY",
                    postId, shortText(body.content()));
        }
    }

    @PutMapping("/posts/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void likePost(@PathVariable Long postId, Authentication authentication) {
        var userId = currentUserService.require(authentication).id();
        var authorId = requirePost(postId);
        var inserted = jdbc.update(
                "INSERT IGNORE INTO post_likes (post_id, user_id) VALUES (?, ?)",
                postId, userId);
        if (inserted > 0 && !authorId.equals(userId)) {
            jdbc.update("""
                    INSERT INTO forum_notifications (recipient_id, actor_id, type, post_id)
                    VALUES (?, ?, 'LIKE', ?)
                    """, authorId, userId, postId);
        }
    }

    @DeleteMapping("/posts/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlikePost(@PathVariable Long postId, Authentication authentication) {
        jdbc.update("DELETE FROM post_likes WHERE post_id = ? AND user_id = ?",
                postId, currentUserService.require(authentication).id());
    }

    @PutMapping("/posts/{postId}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void favoritePost(@PathVariable Long postId, Authentication authentication) {
        requirePost(postId);
        jdbc.update("INSERT IGNORE INTO post_favorites (post_id, user_id) VALUES (?, ?)",
                postId, currentUserService.require(authentication).id());
    }

    @DeleteMapping("/posts/{postId}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unfavoritePost(@PathVariable Long postId, Authentication authentication) {
        jdbc.update("DELETE FROM post_favorites WHERE post_id = ? AND user_id = ?",
                postId, currentUserService.require(authentication).id());
    }

    @PutMapping("/posts/{postId}/comments/{commentId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void likeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        var userId = currentUserService.require(authentication).id();
        var comments = jdbc.queryForList("""
                SELECT author_id AS authorId, content
                FROM post_comments WHERE id = ? AND post_id = ?
                """, commentId, postId);
        if (comments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        var inserted = jdbc.update("""
                INSERT IGNORE INTO post_comment_likes (comment_id, user_id)
                VALUES (?, ?)
                """, commentId, userId);
        var authorId = ((Number) comments.getFirst().get("authorId")).longValue();
        if (inserted > 0 && authorId != userId) {
            jdbc.update("""
                    INSERT INTO forum_notifications
                        (recipient_id, actor_id, type, post_id, content)
                    VALUES (?, ?, 'COMMENT_LIKE', ?, ?)
                    """, authorId, userId, postId,
                    shortText(String.valueOf(comments.getFirst().get("content"))));
        }
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlikeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        jdbc.update("""
                DELETE likes FROM post_comment_likes likes
                JOIN post_comments c ON c.id = likes.comment_id
                WHERE likes.comment_id = ? AND c.post_id = ? AND likes.user_id = ?
                """, commentId, postId, currentUserService.require(authentication).id());
    }

    @GetMapping("/forum-notifications")
    List<Map<String, Object>> forumNotifications(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT notification.id, notification.type, notification.post_id AS postId,
                       COALESCE(actor.display_name, 'Gym staff') AS actorName,
                       post.title AS postTitle, notification.content,
                       notification.created_at AS createdAt,
                       notification.read_at IS NOT NULL AS isRead
                FROM forum_notifications notification
                LEFT JOIN users actor ON actor.id = notification.actor_id
                LEFT JOIN posts post ON post.id = notification.post_id
                WHERE notification.recipient_id = ?
                ORDER BY notification.created_at DESC
                LIMIT 200
                """, currentUserService.require(authentication).id());
    }

    @GetMapping("/forum-notifications/unread-count")
    Map<String, Integer> unreadForumNotificationCount(Authentication authentication) {
        var count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM forum_notifications
                WHERE recipient_id = ? AND read_at IS NULL
                """, Integer.class, currentUserService.require(authentication).id());
        return Map.of("count", count == null ? 0 : count);
    }

    @PatchMapping("/forum-notifications/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void readForumNotifications(Authentication authentication) {
        jdbc.update("""
                UPDATE forum_notifications SET read_at = CURRENT_TIMESTAMP
                WHERE recipient_id = ? AND read_at IS NULL
                """, currentUserService.require(authentication).id());
    }

    @GetMapping("/forum-feedback/me")
    List<Map<String, Object>> myForumFeedback(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT feedback.id, feedback.post_id AS postId, post.title AS postTitle,
                       feedback.content, feedback.status, feedback.admin_reply AS adminReply,
                       feedback.created_at AS createdAt, feedback.replied_at AS repliedAt
                FROM forum_feedback feedback
                LEFT JOIN posts post ON post.id = feedback.post_id
                WHERE feedback.author_id = ?
                ORDER BY feedback.created_at DESC
                """, currentUserService.require(authentication).id());
    }

    @PostMapping("/forum-feedback")
    @ResponseStatus(HttpStatus.CREATED)
    void createForumFeedback(
            @Valid @RequestBody ForumFeedbackRequest body,
            Authentication authentication
    ) {
        if (body.postId() != null) requirePost(body.postId());
        jdbc.update("""
                INSERT INTO forum_feedback (author_id, post_id, content)
                VALUES (?, ?, ?)
                """, currentUserService.require(authentication).id(),
                body.postId(), body.content().trim());
    }

    @GetMapping("/coach-connections")
    List<Map<String, Object>> coachConnections(Authentication authentication) {
        var user = currentUserService.require(authentication);
        if ("MEMBER".equals(user.role().name())) {
            return jdbc.queryForList("""
                    SELECT request.id, request.coach_id AS coachId,
                           coach.display_name AS coachName, coach_profile.specialties,
                           request.message, request.status,
                           request.created_at AS createdAt,
                           request.responded_at AS respondedAt,
                           EXISTS(
                               SELECT 1 FROM coach_member_assignments assignment
                               WHERE assignment.coach_id = request.coach_id
                                 AND assignment.member_id = request.member_id
                                 AND assignment.status = 'ACTIVE'
                                 AND assignment.starts_on <= CURRENT_DATE
                                 AND (assignment.ends_on IS NULL
                                      OR assignment.ends_on >= CURRENT_DATE)
                           ) AS connected
                    FROM coach_connection_requests request
                    JOIN users coach ON coach.id = request.coach_id
                    LEFT JOIN coach_profiles coach_profile ON coach_profile.user_id = coach.id
                    WHERE request.member_id = ?
                    ORDER BY request.created_at DESC
                    """, user.id());
        }
        return jdbc.queryForList("""
                SELECT request.id, request.member_id AS memberId,
                       member.display_name AS memberName,
                       request.message, request.status,
                       request.created_at AS createdAt,
                       request.responded_at AS respondedAt,
                       EXISTS(
                           SELECT 1 FROM coach_member_assignments assignment
                           WHERE assignment.coach_id = request.coach_id
                             AND assignment.member_id = request.member_id
                             AND assignment.status = 'ACTIVE'
                             AND assignment.starts_on <= CURRENT_DATE
                             AND (assignment.ends_on IS NULL
                                  OR assignment.ends_on >= CURRENT_DATE)
                       ) AS connected
                FROM coach_connection_requests request
                JOIN users member ON member.id = request.member_id
                WHERE request.coach_id = ?
                ORDER BY CASE request.status WHEN 'PENDING' THEN 0 ELSE 1 END,
                         request.created_at DESC
                """, user.id());
    }

    @GetMapping("/coach-connections/pending-count")
    Map<String, Integer> pendingCoachConnectionCount(Authentication authentication) {
        var user = currentUserService.require(authentication);
        var count = "COACH".equals(user.role().name())
                ? jdbc.queryForObject("""
                    SELECT COUNT(*) FROM coach_connection_requests
                    WHERE coach_id = ? AND status = 'PENDING'
                    """, Integer.class, user.id())
                : jdbc.queryForObject("""
                    SELECT COUNT(*) FROM coach_connection_requests
                    WHERE member_id = ? AND status IN ('ACCEPTED', 'DECLINED')
                      AND member_read_at IS NULL
                    """, Integer.class, user.id());
        return Map.of("count", count == null ? 0 : count);
    }

    @PatchMapping("/coach-connections/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void readCoachConnectionResponses(Authentication authentication) {
        var user = currentUserService.require(authentication);
        if (!"MEMBER".equals(user.role().name())) return;
        jdbc.update("""
                UPDATE coach_connection_requests SET member_read_at = CURRENT_TIMESTAMP
                WHERE member_id = ? AND status IN ('ACCEPTED', 'DECLINED')
                  AND member_read_at IS NULL
                """, user.id());
    }

    @PostMapping("/coach-connections")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void requestCoachConnection(
            @Valid @RequestBody CoachConnectionRequest body,
            Authentication authentication
    ) {
        var user = currentUserService.require(authentication);
        if (!"MEMBER".equals(user.role().name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only members can contact a coach");
        }
        if (jdbc.queryForList("""
                SELECT id FROM users
                WHERE id = ? AND role = 'COACH' AND active = TRUE
                FOR UPDATE
                """, Long.class, body.coachId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found");
        }
        var existing = jdbc.queryForObject("""
                SELECT
                    (SELECT COUNT(*) FROM coach_connection_requests
                     WHERE member_id = ? AND coach_id = ? AND status = 'PENDING')
                  + (SELECT COUNT(*) FROM coach_member_assignments
                     WHERE member_id = ? AND coach_id = ? AND status = 'ACTIVE'
                       AND starts_on <= CURRENT_DATE
                       AND (ends_on IS NULL OR ends_on >= CURRENT_DATE))
                """, Integer.class, user.id(), body.coachId(), user.id(), body.coachId());
        if (existing != null && existing > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A pending or active coach connection already exists");
        }
        jdbc.update("""
                INSERT INTO coach_connection_requests (member_id, coach_id, message)
                VALUES (?, ?, ?)
                """, user.id(), body.coachId(), body.message().trim());
    }

    @PatchMapping("/coach-connections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void respondToCoachConnection(
            @PathVariable Long id,
            @Valid @RequestBody CoachConnectionStatusRequest body,
            Authentication authentication
    ) {
        var user = currentUserService.require(authentication);
        if (!"COACH".equals(user.role().name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only coaches can respond");
        }
        if (!Set.of("ACCEPTED", "DECLINED").contains(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coach response");
        }
        var requests = jdbc.queryForList("""
                SELECT member_id AS memberId, coach_id AS coachId
                FROM coach_connection_requests
                WHERE id = ? AND coach_id = ? AND status = 'PENDING'
                FOR UPDATE
                """, id, user.id());
        if (requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pending connection request not found");
        }
        var request = requests.getFirst();
        if ("ACCEPTED".equals(body.status())) {
            jdbc.update("""
                    INSERT INTO coach_member_assignments (coach_id, member_id, starts_on)
                    SELECT ?, ?, CURRENT_DATE
                    WHERE NOT EXISTS (
                        SELECT 1 FROM coach_member_assignments
                        WHERE coach_id = ? AND member_id = ? AND status = 'ACTIVE'
                          AND starts_on <= CURRENT_DATE
                          AND (ends_on IS NULL OR ends_on >= CURRENT_DATE)
                    )
                    """, request.get("coachId"), request.get("memberId"),
                    request.get("coachId"), request.get("memberId"));
        }
        jdbc.update("""
                UPDATE coach_connection_requests
                SET status = ?, responded_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, body.status(), id);
    }

    @DeleteMapping("/coach-connections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelCoachConnection(@PathVariable Long id, Authentication authentication) {
        var user = currentUserService.require(authentication);
        if (!"MEMBER".equals(user.role().name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only members can cancel requests");
        }
        if (jdbc.update("""
                    UPDATE coach_connection_requests
                    SET status = 'CANCELLED', responded_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND member_id = ? AND status = 'PENDING'
                    """, id, user.id()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pending connection request not found");
        }
    }

    @GetMapping("/messages/peers")
    List<Map<String, Object>> messagePeers(Authentication authentication) {
        var user = currentUserService.require(authentication);
        var role = user.role().name();
        return jdbc.queryForList("""
                SELECT peer.id, peer.username, peer.display_name AS displayName, peer.role,
                       (SELECT COUNT(*) FROM chat_messages message
                        WHERE message.sender_id = peer.id
                          AND message.recipient_id = ?
                          AND message.read_at IS NULL) AS unreadCount
                FROM coach_member_assignments assignment
                JOIN users peer ON (
                    (? = 'COACH' AND peer.id = assignment.member_id)
                    OR (? = 'MEMBER' AND peer.id = assignment.coach_id)
                )
                WHERE assignment.status = 'ACTIVE'
                  AND assignment.starts_on <= CURRENT_DATE
                  AND (assignment.ends_on IS NULL OR assignment.ends_on >= CURRENT_DATE)
                  AND (
                    (? = 'COACH' AND assignment.coach_id = ?)
                    OR (? = 'MEMBER' AND assignment.member_id = ?)
                  )
                  AND peer.active = TRUE
                GROUP BY peer.id, peer.username, peer.display_name, peer.role
                ORDER BY peer.display_name
                """, user.id(), role, role, role, user.id(), role, user.id());
    }

    @GetMapping("/messages/unread-count")
    Map<String, Integer> unreadMessageCount(Authentication authentication) {
        var user = currentUserService.require(authentication);
        var count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM chat_messages message
                WHERE message.recipient_id = ? AND message.read_at IS NULL
                  AND EXISTS (
                    SELECT 1 FROM coach_member_assignments assignment
                    WHERE assignment.status = 'ACTIVE'
                      AND assignment.starts_on <= CURRENT_DATE
                      AND (assignment.ends_on IS NULL OR assignment.ends_on >= CURRENT_DATE)
                      AND (
                        (? = 'COACH' AND assignment.coach_id = ?
                         AND assignment.member_id = message.sender_id)
                        OR
                        (? = 'MEMBER' AND assignment.member_id = ?
                         AND assignment.coach_id = message.sender_id)
                      )
                  )
                """, Integer.class,
                user.id(), user.role().name(), user.id(), user.role().name(), user.id());
        return Map.of("count", count == null ? 0 : count);
    }

    @GetMapping("/messages/{otherUserId}")
    List<Map<String, Object>> messages(
            @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        var user = currentUserService.require(authentication);
        requireAssignedPeer(user.id(), user.role().name(), otherUserId);
        var userId = user.id();
        jdbc.update("""
                UPDATE chat_messages SET read_at = CURRENT_TIMESTAMP
                WHERE sender_id = ? AND recipient_id = ? AND read_at IS NULL
                """, otherUserId, userId);
        return jdbc.queryForList("""
                SELECT id, sender_id AS senderId, recipient_id AS recipientId,
                       content, created_at AS createdAt
                FROM chat_messages
                WHERE (sender_id = ? AND recipient_id = ?)
                   OR (sender_id = ? AND recipient_id = ?)
                ORDER BY created_at
                """, userId, otherUserId, otherUserId, userId);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    void sendMessage(@Valid @RequestBody MessageRequest body, Authentication authentication) {
        var sender = currentUserService.require(authentication);
        requireAssignedPeer(sender.id(), sender.role().name(), body.recipientId());
        jdbc.update("""
                INSERT INTO chat_messages (sender_id, recipient_id, content)
                VALUES (?, ?, ?)
                """, sender.id(), body.recipientId(), body.content().trim());
    }

    private List<Map<String, Object>> listCoachAvailability(Long coachId) {
        return jdbc.queryForList("""
                SELECT id, coach_id AS coachId, day_of_week AS dayOfWeek,
                       starts_at AS startsAt, ends_at AS endsAt
                FROM coach_availability
                WHERE coach_id = ?
                ORDER BY day_of_week, starts_at
                """, coachId);
    }

    private List<Map<String, Object>> listPosts(Long userId, String filter, String order) {
        return jdbc.queryForList("""
                SELECT p.id, p.author_id AS authorId, u.display_name AS authorName,
                       p.title, p.content, p.created_at AS createdAt,
                       COUNT(DISTINCT likes.user_id) AS likeCount,
                       COUNT(DISTINCT comments.id) AS commentCount,
                       (SELECT top_comment.content
                        FROM post_comments top_comment
                        LEFT JOIN post_comment_likes top_likes
                          ON top_likes.comment_id = top_comment.id
                        WHERE top_comment.post_id = p.id
                        GROUP BY top_comment.id, top_comment.content, top_comment.created_at
                        ORDER BY COUNT(top_likes.user_id) DESC, top_comment.created_at
                        LIMIT 1) AS topComment,
                       EXISTS(SELECT 1 FROM post_likes mine
                              WHERE mine.post_id = p.id AND mine.user_id = ?) AS liked,
                       EXISTS(SELECT 1 FROM post_favorites mine
                              WHERE mine.post_id = p.id AND mine.user_id = ?) AS favorited
                FROM posts p
                JOIN users u ON u.id = p.author_id
                LEFT JOIN post_likes likes ON likes.post_id = p.id
                LEFT JOIN post_comments comments ON comments.post_id = p.id
                %s
                GROUP BY p.id, p.author_id, u.display_name, p.title, p.content, p.created_at
                ORDER BY %s
                """.formatted(filter, order), userId, userId);
    }

    private Long requirePost(Long postId) {
        var authors = jdbc.queryForList(
                "SELECT author_id FROM posts WHERE id = ?", Long.class, postId);
        if (authors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return authors.getFirst();
    }

    private void requireAssignedPeer(Long userId, String role, Long peerId) {
        var assigned = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM coach_member_assignments assignment
                JOIN users peer ON peer.id = ?
                WHERE assignment.status = 'ACTIVE'
                  AND assignment.starts_on <= CURRENT_DATE
                  AND (assignment.ends_on IS NULL OR assignment.ends_on >= CURRENT_DATE)
                  AND peer.active = TRUE
                  AND (
                    (? = 'COACH' AND peer.role = 'MEMBER'
                     AND assignment.coach_id = ? AND assignment.member_id = ?)
                    OR
                    (? = 'MEMBER' AND peer.role = 'COACH'
                     AND assignment.member_id = ? AND assignment.coach_id = ?)
                  )
                """, Integer.class,
                peerId, role, userId, peerId, role, userId, peerId);
        if (assigned == null || assigned == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not assigned to this coach");
        }
    }

    private String shortText(String value) {
        var text = value.trim();
        return text.length() <= 500 ? text : text.substring(0, 500);
    }

    private LocalDate localDate(Instant value) {
        return value.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime localTime(Instant value) {
        return value.atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public record CoachAppointmentRequest(
            @NotNull Long coachId,
            @NotNull @Future Instant startsAt,
            @NotBlank @Size(max = 500) String note
    ) {}

    public record CoachAvailabilityRequest(
            @NotNull @Min(1) @Max(7) Integer dayOfWeek,
            @NotNull LocalTime startsAt,
            @NotNull LocalTime endsAt
    ) {}

    public record AppointmentStatusRequest(@NotBlank String status) {}

    public record PostRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 5000) String content
    ) {}

    public record CommentRequest(
            Long parentId,
            @NotBlank @Size(max = 2000) String content
    ) {}

    public record ForumFeedbackRequest(
            Long postId,
            @NotBlank @Size(max = 2000) String content
    ) {}

    public record CoachConnectionRequest(
            @NotNull Long coachId,
            @NotBlank @Size(max = 500) String message
    ) {}

    public record CoachConnectionStatusRequest(@NotBlank String status) {}

    public record MessageRequest(
            @NotNull Long recipientId,
            @NotBlank @Size(max = 1000) String content
    ) {}
}
