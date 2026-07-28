package com.gymplatform.controller;

import com.gymplatform.service.CurrentUserService;
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
    List<Map<String, Object>> equipment() {
        return jdbc.queryForList("""
                SELECT id, name, category, description, status, cover_key AS coverKey
                FROM equipment
                WHERE status <> 'RETIRED'
                ORDER BY name
                """);
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
                SELECT id, closed_on AS closedOn, reason
                FROM gym_closed_days
                WHERE closed_on BETWEEN ? AND ?
                ORDER BY closed_on
                """, from, to);
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

    @GetMapping("/equipment-reservations/me")
    List<Map<String, Object>> myEquipmentReservations(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT r.id, r.equipment_id AS equipmentId, e.name AS equipmentName,
                       r.starts_at AS startsAt, r.ends_at AS endsAt, r.status
                FROM equipment_reservations r
                JOIN equipment e ON e.id = r.equipment_id
                WHERE r.member_id = ?
                ORDER BY r.starts_at DESC
                """, currentUserService.require(authentication).id());
    }

    @GetMapping("/equipment-reservations/availability")
    List<Map<String, Object>> equipmentAvailability(
            @RequestParam Long equipmentId,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        if (!to.isAfter(from) || Duration.between(from, to).compareTo(Duration.ofDays(8)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid availability range");
        }
        return jdbc.queryForList("""
                SELECT starts_at AS startsAt, ends_at AS endsAt
                FROM equipment_reservations
                WHERE equipment_id = ? AND status = 'CONFIRMED'
                  AND starts_at < ? AND ends_at > ?
                ORDER BY starts_at
                """, equipmentId, to, from);
    }

    @PostMapping("/equipment-reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void reserveEquipment(
            @Valid @RequestBody EquipmentReservationRequest body,
            Authentication authentication
    ) {
        if (!Set.of(Duration.ofMinutes(30), Duration.ofMinutes(60))
                .contains(Duration.between(body.startsAt(), body.endsAt()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation duration must be 30 or 60 minutes"
            );
        }
        var memberId = currentUserService.require(authentication).id();
        if (isClosed(localDate(body.startsAt()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gym is closed on that date");
        }
        jdbc.queryForObject(
                "SELECT id FROM users WHERE id = ? FOR UPDATE",
                Long.class, memberId);
        var available = jdbc.queryForList(
                "SELECT id FROM equipment WHERE id = ? AND status = 'AVAILABLE' FOR UPDATE",
                Long.class, body.equipmentId());
        if (available.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment is not available");
        }
        var memberConflicts = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM equipment_reservations
                WHERE member_id = ? AND status = 'CONFIRMED'
                  AND starts_at < ? AND ends_at > ?
                """, Integer.class, memberId, body.endsAt(), body.startsAt());
        if (memberConflicts != null && memberConflicts > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have equipment reserved for that time"
            );
        }
        var conflicts = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM equipment_reservations
                WHERE equipment_id = ? AND status = 'CONFIRMED'
                  AND starts_at < ? AND ends_at > ?
                """, Integer.class, body.equipmentId(), body.endsAt(), body.startsAt());
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Equipment is already reserved for that time");
        }
        jdbc.update("""
                INSERT INTO equipment_reservations
                    (equipment_id, member_id, starts_at, ends_at)
                VALUES (?, ?, ?, ?)
                """, body.equipmentId(), memberId, body.startsAt(), body.endsAt());
    }

    @DeleteMapping("/equipment-reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelEquipmentReservation(@PathVariable Long id, Authentication authentication) {
        if (jdbc.update("""
                UPDATE equipment_reservations SET status = 'CANCELLED'
                WHERE id = ? AND member_id = ? AND status = 'CONFIRMED'
                """, id, currentUserService.require(authentication).id()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active reservation not found");
        }
    }

    @GetMapping("/coach-appointments/me")
    List<Map<String, Object>> myCoachAppointments(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT a.id, a.coach_id AS coachId, coach.display_name AS coachName,
                       a.starts_at AS startsAt, a.note, a.status
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
        if (isClosed(startsOn)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gym is closed on that date");
        }
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
                """, id, currentUserService.require(authentication).id()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active appointment not found");
        }
    }

    @GetMapping("/coach/appointments")
    List<Map<String, Object>> coachAppointments(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT a.id, a.member_id AS memberId, member.display_name AS memberName,
                       member.email, a.starts_at AS startsAt, a.note, a.status
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
    List<Map<String, Object>> posts() {
        return jdbc.queryForList("""
                SELECT p.id, p.author_id AS authorId, u.display_name AS authorName,
                       p.title, p.content, p.created_at AS createdAt
                FROM posts p
                JOIN users u ON u.id = p.author_id
                ORDER BY p.created_at DESC
                """);
    }

    @GetMapping("/posts/me")
    List<Map<String, Object>> myPosts(Authentication authentication) {
        return jdbc.queryForList("""
                SELECT id, title, content, created_at AS createdAt
                FROM posts
                WHERE author_id = ?
                ORDER BY created_at DESC
                """, currentUserService.require(authentication).id());
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

    @GetMapping("/messages/peers")
    List<Map<String, Object>> messagePeers(Authentication authentication) {
        var user = currentUserService.require(authentication);
        var peerRole = user.role().name().equals("COACH") ? "MEMBER" : "COACH";
        return jdbc.queryForList("""
                SELECT id, username, display_name AS displayName, role
                FROM users
                WHERE role = ? AND active = TRUE
                ORDER BY display_name
                """, peerRole);
    }

    @GetMapping("/messages/{otherUserId}")
    List<Map<String, Object>> messages(
            @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        var userId = currentUserService.require(authentication).id();
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
        var recipientRoles = jdbc.queryForList(
                "SELECT role FROM users WHERE id = ? AND active = TRUE",
                String.class,
                body.recipientId()
        );
        if (recipientRoles.isEmpty()
                || sender.role().name().equals(recipientRoles.getFirst())
                || "ADMIN".equals(recipientRoles.getFirst())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid message recipient");
        }
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

    private boolean isClosed(LocalDate date) {
        var closed = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gym_closed_days WHERE closed_on = ?",
                Integer.class, date);
        return closed != null && closed > 0;
    }

    private LocalDate localDate(Instant value) {
        return value.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime localTime(Instant value) {
        return value.atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public record EquipmentReservationRequest(
            @NotNull Long equipmentId,
            @NotNull @Future Instant startsAt,
            @NotNull @Future Instant endsAt
    ) {}

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

    public record MessageRequest(
            @NotNull Long recipientId,
            @NotBlank @Size(max = 1000) String content
    ) {}
}
