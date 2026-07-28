package com.gymplatform.controller;

import com.gymplatform.service.SessionSchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final JdbcTemplate jdbc;
    private final SessionSchedulingService sessionSchedulingService;

    public AdminController(JdbcTemplate jdbc, SessionSchedulingService sessionSchedulingService) {
        this.jdbc = jdbc;
        this.sessionSchedulingService = sessionSchedulingService;
    }

    @GetMapping("/overview")
    Map<String, Object> overview() {
        return Map.of(
                "memberCount", count("SELECT COUNT(*) FROM users WHERE role = 'MEMBER'"),
                "coachCount", count("SELECT COUNT(*) FROM users WHERE role = 'COACH'"),
                "courseCount", count("SELECT COUNT(*) FROM courses WHERE active = TRUE"),
                "bookingCount", count("SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED'"),
                "currentOccupancy", count("SELECT COUNT(*) FROM member_visits WHERE checked_out_at IS NULL"),
                "equipmentCount", count("SELECT COUNT(*) FROM equipment_units WHERE base_status <> 'RETIRED'"),
                "postCount", count("SELECT COUNT(*) FROM posts"),
                "bookingByCourse", jdbc.queryForList("""
                        SELECT c.name, COUNT(b.id) AS total
                        FROM courses c
                        LEFT JOIN course_sessions s ON s.course_id = c.id
                        LEFT JOIN bookings b ON b.session_id = s.id AND b.status = 'CONFIRMED'
                        GROUP BY c.id, c.name
                        ORDER BY total DESC, c.name
                        """),
                "equipmentByStatus", jdbc.queryForList("""
                        SELECT CASE
                                   WHEN maintenance.id IS NOT NULL THEN 'MAINTENANCE'
                                   ELSE unit.base_status
                               END AS status,
                               COUNT(*) AS total
                        FROM equipment_units unit
                        LEFT JOIN equipment_maintenance maintenance
                          ON maintenance.unit_id = unit.id
                         AND maintenance.starts_at <= CURRENT_TIMESTAMP
                         AND maintenance.ends_at > CURRENT_TIMESTAMP
                        WHERE unit.base_status <> 'RETIRED'
                        GROUP BY status
                        """)
        );
    }

    @GetMapping("/users")
    List<Map<String, Object>> users(@RequestParam(required = false) String role) {
        if (role != null && !Set.of("MEMBER", "COACH", "ADMIN").contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported role");
        }
        return role == null
                ? jdbc.queryForList("""
                    SELECT id, username, display_name AS displayName, email, role, active, created_at AS createdAt
                    FROM users ORDER BY created_at DESC
                    """)
                : jdbc.queryForList("""
                    SELECT id, username, display_name AS displayName, email, role, active, created_at AS createdAt
                    FROM users WHERE role = ? ORDER BY created_at DESC
                    """, role);
    }

    @PatchMapping("/users/{id}/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateUserActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest body) {
        if (jdbc.update(
                "UPDATE users SET active = ? WHERE id = ? AND role <> 'ADMIN'",
                body.active(), id
        ) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member or coach not found");
        }
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    void createCourse(@Valid @RequestBody CourseRequest body) {
        jdbc.update("""
                INSERT INTO courses
                    (name, description, duration_minutes, default_capacity, cover_key)
                VALUES (?, ?, ?, ?, ?)
                """, body.name().trim(), body.description().trim(),
                body.durationMinutes(), body.defaultCapacity(), body.coverKey().trim());
    }

    @PatchMapping("/courses/{id}/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateCourseActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest body) {
        if (jdbc.update("UPDATE courses SET active = ? WHERE id = ?", body.active(), id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
    }

    @GetMapping("/notices")
    List<Map<String, Object>> notices() {
        return jdbc.queryForList("""
                SELECT id, title, content, active, created_at AS createdAt
                FROM notices ORDER BY created_at DESC
                """);
    }

    @PostMapping("/notices")
    @ResponseStatus(HttpStatus.CREATED)
    void createNotice(@Valid @RequestBody NoticeRequest body) {
        jdbc.update(
                "INSERT INTO notices (title, content) VALUES (?, ?)",
                body.title().trim(),
                body.content().trim()
        );
    }

    @DeleteMapping("/notices/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNotice(@PathVariable Long id) {
        if (jdbc.update("DELETE FROM notices WHERE id = ?", id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found");
        }
    }

    @PostMapping("/equipment")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void createEquipment(@Valid @RequestBody EquipmentRequest body) {
        requireSpace(body.spaceId());
        jdbc.update("""
                INSERT INTO equipment
                    (name, category, description, unit_label, cover_key, resource_type, space_id)
                VALUES (?, ?, ?, ?, 'equipment', 'EQUIPMENT', ?)
                """, body.name().trim(), body.category().trim(), body.description().trim(),
                body.unitLabel().trim(), body.spaceId());
        var equipmentId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        createUnits(equipmentId, body.initialUnits(), body.spaceId());
    }

    @PatchMapping("/equipment/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentUpdateRequest body) {
        if (jdbc.update("""
                UPDATE equipment
                SET name = ?, category = ?, description = ?, unit_label = ?
                WHERE id = ? AND status <> 'RETIRED' AND resource_type = 'EQUIPMENT'
                """, body.name().trim(), body.category().trim(), body.description().trim(),
                body.unitLabel().trim(), id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found");
        }
    }

    @DeleteMapping("/equipment/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void archiveEquipment(@PathVariable Long id) {
        if (jdbc.update("""
                UPDATE equipment SET status = 'RETIRED'
                WHERE id = ? AND status <> 'RETIRED' AND resource_type = 'EQUIPMENT'
                """, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found");
        }
        jdbc.update("UPDATE equipment_units SET base_status = 'RETIRED' WHERE equipment_id = ?", id);
    }

    @GetMapping("/resources")
    List<Map<String, Object>> resources() {
        return jdbc.queryForList("""
                SELECT equipment.id, equipment.name, equipment.category,
                       equipment.description, equipment.unit_label AS unitLabel,
                       COUNT(unit.id) AS totalUnits,
                       SUM(CASE WHEN unit.base_status = 'AVAILABLE'
                                 AND maintenance.id IS NULL THEN 1 ELSE 0 END) AS availableUnits,
                       SUM(CASE WHEN unit.base_status = 'IN_USE' THEN 1 ELSE 0 END) AS inUseUnits,
                       SUM(CASE WHEN maintenance.id IS NOT NULL THEN 1 ELSE 0 END) AS maintenanceUnits,
                       SUM(CASE WHEN unit.base_status = 'OUT_OF_SERVICE'
                                 AND maintenance.id IS NULL THEN 1 ELSE 0 END) AS outOfServiceUnits,
                       MAX(unit.updated_at) AS updatedAt
                FROM equipment
                LEFT JOIN equipment_units unit
                    ON unit.equipment_id = equipment.id AND unit.base_status <> 'RETIRED'
                LEFT JOIN equipment_maintenance maintenance
                    ON maintenance.unit_id = unit.id
                   AND maintenance.starts_at <= CURRENT_TIMESTAMP
                   AND maintenance.ends_at > CURRENT_TIMESTAMP
                WHERE equipment.status <> 'RETIRED' AND equipment.resource_type = 'EQUIPMENT'
                GROUP BY equipment.id, equipment.name, equipment.category,
                         equipment.description, equipment.unit_label
                ORDER BY equipment.category, equipment.name
                """);
    }

    @GetMapping("/equipment/{id}/units")
    List<Map<String, Object>> equipmentUnits(@PathVariable Long id) {
        requireEquipment(id);
        return jdbc.queryForList("""
                SELECT unit.id, unit.asset_code AS assetCode, unit.serial_number AS serialNumber,
                       unit.purchased_on AS purchasedOn, unit.notes,
                       CASE WHEN maintenance.id IS NULL THEN unit.base_status ELSE 'MAINTENANCE' END AS status,
                       unit.base_status AS baseStatus, unit.space_id AS spaceId,
                       space.name AS spaceName, floor.name AS floorName,
                       maintenance.id AS maintenanceId, maintenance.starts_at AS maintenanceStartsAt,
                       maintenance.ends_at AS maintenanceEndsAt, maintenance.reason AS maintenanceReason,
                       unit.updated_at AS updatedAt
                FROM equipment_units unit
                LEFT JOIN gym_spaces space ON space.id = unit.space_id
                LEFT JOIN gym_floors floor ON floor.id = space.floor_id
                LEFT JOIN equipment_maintenance maintenance
                    ON maintenance.unit_id = unit.id
                   AND maintenance.starts_at <= CURRENT_TIMESTAMP
                   AND maintenance.ends_at > CURRENT_TIMESTAMP
                WHERE unit.equipment_id = ? AND unit.base_status <> 'RETIRED'
                ORDER BY unit.asset_code
                """, id);
    }

    @PostMapping("/equipment/{id}/units")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void addEquipmentUnits(@PathVariable Long id, @Valid @RequestBody UnitBatchCreateRequest body) {
        requireEquipment(id);
        requireSpace(body.spaceId());
        createUnits(id, body.count(), body.spaceId());
    }

    @PatchMapping("/equipment-units/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateEquipmentUnit(@PathVariable Long id, @Valid @RequestBody EquipmentUnitRequest body) {
        requireSpace(body.spaceId());
        requireUnitStatus(body.status());
        try {
            if (jdbc.update("""
                    UPDATE equipment_units
                    SET asset_code = ?, space_id = ?, serial_number = ?, purchased_on = ?,
                        base_status = ?, notes = ?
                    WHERE id = ? AND base_status <> 'RETIRED'
                    """, body.assetCode().trim(), body.spaceId(), blankToNull(body.serialNumber()),
                    body.purchasedOn(), body.status(), body.notes().trim(), id) == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment unit not found");
            }
        } catch (org.springframework.dao.DuplicateKeyException error) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset code already exists");
        }
    }

    @PatchMapping("/equipment-units/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void updateEquipmentUnits(@Valid @RequestBody UnitBatchUpdateRequest body) {
        requireUnitStatus(body.status());
        requireSpace(body.spaceId());
        var placeholders = String.join(", ", java.util.Collections.nCopies(body.ids().size(), "?"));
        var parameters = new ArrayList<Object>();
        parameters.add(body.status());
        if (body.updateSpace()) parameters.add(body.spaceId());
        parameters.addAll(body.ids());
        var updated = jdbc.update("""
                UPDATE equipment_units SET base_status = ? %s
                WHERE id IN (%s) AND base_status <> 'RETIRED'
                """.formatted(body.updateSpace() ? ", space_id = ?" : "", placeholders), parameters.toArray());
        if (updated != body.ids().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more equipment units were not found");
        }
    }

    @DeleteMapping("/equipment-units/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void archiveEquipmentUnit(@PathVariable Long id) {
        if (jdbc.update("""
                UPDATE equipment_units SET base_status = 'RETIRED'
                WHERE id = ? AND base_status <> 'RETIRED'
                """, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment unit not found");
        }
    }

    @GetMapping("/equipment-units/{id}/maintenance")
    List<Map<String, Object>> equipmentMaintenance(@PathVariable Long id) {
        requireUnit(id);
        return jdbc.queryForList("""
                SELECT id, starts_at AS startsAt, ends_at AS endsAt, reason, notes,
                       created_at AS createdAt, updated_at AS updatedAt
                FROM equipment_maintenance
                WHERE unit_id = ?
                ORDER BY starts_at DESC
                """, id);
    }

    @PostMapping("/equipment-units/{id}/maintenance")
    @ResponseStatus(HttpStatus.CREATED)
    void createEquipmentMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequest body
    ) {
        requireUnit(id);
        validateMaintenance(id, null, body);
        jdbc.update("""
                INSERT INTO equipment_maintenance (unit_id, starts_at, ends_at, reason, notes)
                VALUES (?, ?, ?, ?, ?)
                """, id, body.startsAt(), body.endsAt(), body.reason().trim(), body.notes().trim());
    }

    @PostMapping("/equipment-units/batch-maintenance")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void createEquipmentMaintenanceBatch(@Valid @RequestBody BatchMaintenanceRequest body) {
        for (var id : body.ids()) {
            requireUnit(id);
            validateMaintenance(id, null, body.maintenance());
        }
        for (var id : body.ids()) {
            jdbc.update("""
                    INSERT INTO equipment_maintenance (unit_id, starts_at, ends_at, reason, notes)
                    VALUES (?, ?, ?, ?, ?)
                    """, id, body.maintenance().startsAt(), body.maintenance().endsAt(),
                    body.maintenance().reason().trim(), body.maintenance().notes().trim());
        }
    }

    @PutMapping("/equipment-units/{unitId}/maintenance/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateEquipmentMaintenance(
            @PathVariable Long unitId,
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequest body
    ) {
        validateMaintenance(unitId, id, body);
        if (jdbc.update("""
                UPDATE equipment_maintenance
                SET starts_at = ?, ends_at = ?, reason = ?, notes = ?
                WHERE id = ? AND unit_id = ? AND ends_at > CURRENT_TIMESTAMP
                """, body.startsAt(), body.endsAt(), body.reason().trim(),
                body.notes().trim(), id, unitId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Editable maintenance record not found");
        }
    }

    @DeleteMapping("/equipment-units/{unitId}/maintenance/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteEquipmentMaintenance(@PathVariable Long unitId, @PathVariable Long id) {
        if (jdbc.update("""
                DELETE FROM equipment_maintenance
                WHERE id = ? AND unit_id = ? AND ends_at > CURRENT_TIMESTAMP
                """, id, unitId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Editable maintenance record not found");
        }
    }

    @GetMapping("/course-sessions")
    List<Map<String, Object>> courseSessions() {
        return jdbc.queryForList("""
                SELECT session.id, course.name AS courseName,
                       coach.display_name AS coachName,
                       session.starts_at AS startsAt, session.ends_at AS endsAt,
                       session.capacity, session.status,
                       floor.name AS floorName, space.name AS spaceName,
                       GROUP_CONCAT(resource.name ORDER BY resource.name SEPARATOR ', ') AS resources
                FROM course_sessions session
                JOIN courses course ON course.id = session.course_id
                LEFT JOIN users coach ON coach.id = session.coach_id
                LEFT JOIN gym_spaces space ON space.id = session.space_id
                LEFT JOIN gym_floors floor ON floor.id = space.floor_id
                LEFT JOIN course_session_resources requirement ON requirement.session_id = session.id
                LEFT JOIN equipment resource ON resource.id = requirement.equipment_id
                GROUP BY session.id, course.name, coach.display_name,
                         session.starts_at, session.ends_at, session.capacity, session.status,
                         floor.name, space.name
                ORDER BY session.starts_at DESC
                LIMIT 500
                """);
    }

    @PostMapping("/course-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Long> createCourseSession(@Valid @RequestBody CourseSessionRequest body) {
        var id = sessionSchedulingService.schedule(new SessionSchedulingService.ScheduleRequest(
                body.courseId(), body.coachId(), body.startsAt(), body.endsAt(),
                body.capacity(), body.spaceId(), body.resources().stream()
                        .map(item -> new SessionSchedulingService.ResourceRequirement(
                                item.equipmentId(), item.requiredUnits()))
                        .toList()
        ));
        return Map.of("id", id);
    }

    @DeleteMapping("/course-sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelCourseSession(@PathVariable Long id) {
        if (jdbc.update("""
                UPDATE course_sessions SET status = 'CANCELLED'
                WHERE id = ? AND status = 'OPEN' AND starts_at > CURRENT_TIMESTAMP
                """, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Open future session not found");
        }
    }

    @GetMapping("/coach-appointments")
    List<Map<String, Object>> coachAppointments() {
        return jdbc.queryForList("""
                SELECT a.id, coach.display_name AS coachName, member.display_name AS memberName,
                       a.starts_at AS startsAt, a.note, a.status
                FROM coach_appointments a
                JOIN users coach ON coach.id = a.coach_id
                JOIN users member ON member.id = a.member_id
                ORDER BY a.starts_at DESC
                """);
    }

    @GetMapping("/closed-days")
    List<Map<String, Object>> closedDays() {
        return jdbc.queryForList("""
                SELECT id, closed_on AS closedOn, starts_at AS startsAt,
                       ends_at AS endsAt, reason, created_at AS createdAt
                FROM gym_closed_days
                ORDER BY closed_on DESC, starts_at
                """);
    }

    @PostMapping("/closed-days")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void createClosedDay(@Valid @RequestBody ClosedDayRequest body) {
        if (body.closedOn().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past dates cannot be closed");
        }
        var partial = body.startsAt() != null || body.endsAt() != null;
        if (partial && (body.startsAt() == null || body.endsAt() == null
                || !body.endsAt().isAfter(body.startsAt()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid start and end times are required");
        }
        if (!partial) {
            jdbc.update("DELETE FROM gym_closed_days WHERE closed_on = ?", body.closedOn());
        } else {
            var conflicts = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM gym_closed_days
                    WHERE closed_on = ?
                      AND (starts_at IS NULL OR (starts_at < ? AND ends_at > ?))
                    """, Integer.class, body.closedOn(), body.endsAt(), body.startsAt());
            if (conflicts != null && conflicts > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Closure overlaps an existing closure");
            }
        }
        jdbc.update("""
                INSERT INTO gym_closed_days (closed_on, starts_at, ends_at, reason)
                VALUES (?, ?, ?, ?)
                """, body.closedOn(), body.startsAt(), body.endsAt(), body.reason().trim());
    }

    @GetMapping("/operation-hours")
    Map<String, Object> operationHours() {
        return jdbc.queryForMap("""
                SELECT opens_at AS opensAt, closes_at AS closesAt
                FROM gym_operation_hours WHERE id = 1
                """);
    }

    @PutMapping("/operation-hours")
    void updateOperationHours(@Valid @RequestBody OperationHoursRequest body) {
        if (!body.closesAt().isAfter(body.opensAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing time must be after opening time");
        }
        jdbc.update("""
                UPDATE gym_operation_hours SET opens_at = ?, closes_at = ? WHERE id = 1
                """, body.opensAt(), body.closesAt());
    }

    @DeleteMapping("/closed-days/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteClosedDay(@PathVariable Long id) {
        var closedDates = jdbc.queryForList("SELECT closed_on FROM gym_closed_days WHERE id = ?", LocalDate.class, id);
        if (closedDates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Closed day not found");
        }
        var closedOn = closedDates.get(0);
        if (closedOn.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past closed days cannot be reopened");
        }
        jdbc.update("DELETE FROM gym_closed_days WHERE id = ?", id);
    }

    @GetMapping("/coach-assignments")
    List<Map<String, Object>> coachAssignments() {
        return jdbc.queryForList("""
                SELECT assignment.id, assignment.coach_id AS coachId,
                       coach.display_name AS coachName,
                       assignment.member_id AS memberId,
                       member.display_name AS memberName,
                       assignment.starts_on AS startsOn,
                       assignment.ends_on AS endsOn,
                       assignment.status
                FROM coach_member_assignments assignment
                JOIN users coach ON coach.id = assignment.coach_id
                JOIN users member ON member.id = assignment.member_id
                ORDER BY assignment.status, coach.display_name, member.display_name
                """);
    }

    @PostMapping("/coach-assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    void createCoachAssignment(@Valid @RequestBody CoachAssignmentRequest body) {
        if (body.endsOn() != null && body.endsOn().isBefore(body.startsOn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot precede start date");
        }
        if (countUser(body.coachId(), "COACH") == 0 || countUser(body.memberId(), "MEMBER") == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach or member not found");
        }
        var conflicts = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM coach_member_assignments
                WHERE coach_id = ? AND member_id = ? AND status = 'ACTIVE'
                  AND starts_on <= COALESCE(?, DATE('9999-12-31'))
                  AND COALESCE(ends_on, DATE('9999-12-31')) >= ?
                """, Integer.class,
                body.coachId(), body.memberId(), body.endsOn(), body.startsOn());
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coach assignment already overlaps");
        }
        jdbc.update("""
                INSERT INTO coach_member_assignments
                    (coach_id, member_id, starts_on, ends_on)
                VALUES (?, ?, ?, ?)
                """, body.coachId(), body.memberId(), body.startsOn(), body.endsOn());
        jdbc.update("""
                UPDATE coach_connection_requests
                SET status = 'ACCEPTED', responded_at = CURRENT_TIMESTAMP, member_read_at = NULL
                WHERE coach_id = ? AND member_id = ? AND status = 'PENDING'
                """, body.coachId(), body.memberId());
        jdbc.update("""
                INSERT INTO coach_connection_requests
                    (member_id, coach_id, message, status, responded_at)
                SELECT ?, ?, 'Assigned by gym administration', 'ACCEPTED', CURRENT_TIMESTAMP
                WHERE NOT EXISTS (
                    SELECT 1 FROM coach_connection_requests
                    WHERE member_id = ? AND coach_id = ? AND status = 'ACCEPTED'
                )
                """, body.memberId(), body.coachId(), body.memberId(), body.coachId());
    }

    @DeleteMapping("/coach-assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void endCoachAssignment(@PathVariable Long id) {
        if (jdbc.update("""
                UPDATE coach_member_assignments
                SET status = 'ENDED',
                    ends_on = CASE
                        WHEN starts_on > CURRENT_DATE THEN starts_on
                        ELSE LEAST(COALESCE(ends_on, CURRENT_DATE), CURRENT_DATE)
                    END
                WHERE id = ? AND status = 'ACTIVE'
                """, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active coach assignment not found");
        }
    }

    @GetMapping("/bookings")
    List<Map<String, Object>> bookings() {
        return jdbc.queryForList("""
                SELECT b.id, c.name AS courseName, member.display_name AS memberName,
                       coach.display_name AS coachName, s.starts_at AS startsAt, b.status
                FROM bookings b
                JOIN course_sessions s ON s.id = b.session_id
                JOIN courses c ON c.id = s.course_id
                JOIN users member ON member.id = b.member_id
                LEFT JOIN users coach ON coach.id = s.coach_id
                ORDER BY s.starts_at DESC
                """);
    }

    @GetMapping("/member-visits")
    List<Map<String, Object>> memberVisits() {
        return jdbc.queryForList("""
                SELECT visit.id, member.display_name AS memberName,
                       visit.checked_in_at AS checkedInAt,
                       check_in_staff.display_name AS checkedInBy,
                       visit.checked_out_at AS checkedOutAt,
                       check_out_staff.display_name AS checkedOutBy
                FROM member_visits visit
                JOIN users member ON member.id = visit.member_id
                JOIN users check_in_staff ON check_in_staff.id = visit.checked_in_by
                LEFT JOIN users check_out_staff ON check_out_staff.id = visit.checked_out_by
                ORDER BY visit.checked_in_at DESC
                LIMIT 500
                """);
    }

    @GetMapping("/posts")
    List<Map<String, Object>> posts() {
        return jdbc.queryForList("""
                SELECT p.id, u.display_name AS authorName, p.title,
                       p.content, p.created_at AS createdAt
                FROM posts p
                JOIN users u ON u.id = p.author_id
                ORDER BY p.created_at DESC
                """);
    }

    @GetMapping("/posts/{id}")
    Map<String, Object> postDetails(@PathVariable Long id) {
        var posts = jdbc.queryForList("""
                SELECT p.id, u.display_name AS authorName, p.title, p.content,
                       p.created_at AS createdAt,
                       COUNT(DISTINCT likes.user_id) AS likeCount,
                       COUNT(DISTINCT comments.id) AS commentCount
                FROM posts p
                JOIN users u ON u.id = p.author_id
                LEFT JOIN post_likes likes ON likes.post_id = p.id
                LEFT JOIN post_comments comments ON comments.post_id = p.id
                WHERE p.id = ?
                GROUP BY p.id, u.display_name, p.title, p.content, p.created_at
                """, id);
        if (posts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        var comments = jdbc.queryForList("""
                SELECT comment.id, comment.parent_id AS parentId,
                       author.display_name AS authorName,
                       parent_author.display_name AS parentAuthorName,
                       comment.content, comment.created_at AS createdAt,
                       COUNT(likes.user_id) AS likeCount
                FROM post_comments comment
                JOIN users author ON author.id = comment.author_id
                LEFT JOIN post_comments parent ON parent.id = comment.parent_id
                LEFT JOIN users parent_author ON parent_author.id = parent.author_id
                LEFT JOIN post_comment_likes likes ON likes.comment_id = comment.id
                WHERE comment.post_id = ?
                GROUP BY comment.id, comment.parent_id, author.display_name,
                         parent_author.display_name, comment.content, comment.created_at
                ORDER BY comment.created_at
                """, id);
        return Map.of("post", posts.getFirst(), "comments", comments);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePost(@PathVariable Long id) {
        if (jdbc.update("DELETE FROM posts WHERE id = ?", id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }

    private int count(String sql) {
        var value = jdbc.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private int countUser(Long id, String role) {
        var value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND role = ? AND active = TRUE",
                Integer.class,
                id,
                role
        );
        return value == null ? 0 : value;
    }

    private void requireSpace(Long spaceId) {
        if (spaceId != null && jdbc.queryForList(
                "SELECT id FROM gym_spaces WHERE id = ?", Long.class, spaceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Space not found");
        }
    }

    @GetMapping("/forum-feedback")
    List<Map<String, Object>> forumFeedback() {
        return jdbc.queryForList("""
                SELECT feedback.id,
                       CASE WHEN feedback.post_id IS NULL THEN 'FEEDBACK' ELSE 'COMPLAINT' END AS type,
                       author.display_name AS authorName, post.title AS postTitle,
                       feedback.content, feedback.status,
                       feedback.admin_reply AS adminReply,
                       feedback.created_at AS createdAt, feedback.replied_at AS repliedAt
                FROM forum_feedback feedback
                JOIN users author ON author.id = feedback.author_id
                LEFT JOIN posts post ON post.id = feedback.post_id
                ORDER BY CASE feedback.status WHEN 'OPEN' THEN 0 ELSE 1 END,
                         feedback.created_at DESC
                """);
    }

    @PatchMapping("/forum-feedback/{id}/reply")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void replyForumFeedback(
            @PathVariable Long id,
            @Valid @RequestBody ForumReplyRequest body
    ) {
        var feedback = jdbc.queryForList("""
                SELECT author_id AS authorId, post_id AS postId
                FROM forum_feedback WHERE id = ?
                """, id);
        if (feedback.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found");
        }
        jdbc.update("""
                UPDATE forum_feedback
                SET admin_reply = ?, status = 'REPLIED', replied_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, body.content().trim(), id);
        jdbc.update("""
                INSERT INTO forum_notifications
                    (recipient_id, type, post_id, content)
                VALUES (?, 'FEEDBACK_REPLY', ?, ?)
                """, feedback.getFirst().get("authorId"), feedback.getFirst().get("postId"),
                body.content().trim());
    }

    private void requireEquipment(Long id) {
        if (jdbc.queryForList("""
                SELECT id FROM equipment
                WHERE id = ? AND status <> 'RETIRED' AND resource_type = 'EQUIPMENT'
                """, Long.class, id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found");
        }
    }

    private void requireUnit(Long id) {
        if (jdbc.queryForList("""
                SELECT id FROM equipment_units WHERE id = ? AND base_status <> 'RETIRED'
                """, Long.class, id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment unit not found");
        }
    }

    private void requireUnitStatus(String status) {
        if (!Set.of("AVAILABLE", "IN_USE", "OUT_OF_SERVICE", "RETIRED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported equipment unit status");
        }
    }

    private void createUnits(Long equipmentId, int count, Long spaceId) {
        var next = jdbc.queryForObject("""
                SELECT COUNT(*) + 1 FROM equipment_units WHERE equipment_id = ?
                """, Long.class, equipmentId);
        for (int index = 0; index < count; index++) {
            var sequence = (next == null ? 1 : next) + index;
            var code = "EQ-%04d-%03d".formatted(equipmentId, sequence);
            jdbc.update("""
                    INSERT INTO equipment_units (equipment_id, asset_code, space_id)
                    VALUES (?, ?, ?)
                    """, equipmentId, code, spaceId);
        }
    }

    private void validateMaintenance(Long unitId, Long excludedId, MaintenanceRequest body) {
        if (!body.endsAt().isAfter(body.startsAt()) || !body.endsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maintenance must end in the future");
        }
        var conflicts = jdbc.queryForObject("""
                SELECT COUNT(*) FROM equipment_maintenance
                WHERE unit_id = ? AND id <> COALESCE(?, -1)
                  AND starts_at < ? AND ends_at > ?
                """, Integer.class, unitId, excludedId, body.endsAt(), body.startsAt());
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Maintenance periods cannot overlap");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record NoticeRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 1000) String content
    ) {}

    public record EquipmentRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 1000) String description,
            @NotBlank @Size(max = 32) String unitLabel,
            @NotNull @Min(1) @Max(100) Integer initialUnits,
            Long spaceId
    ) {}

    public record EquipmentUpdateRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 1000) String description,
            @NotBlank @Size(max = 32) String unitLabel
    ) {}

    public record UnitBatchCreateRequest(
            @NotNull @Min(1) @Max(100) Integer count,
            Long spaceId
    ) {}

    public record EquipmentUnitRequest(
            @NotBlank @Size(max = 64) String assetCode,
            Long spaceId,
            @Size(max = 120) String serialNumber,
            LocalDate purchasedOn,
            @NotBlank String status,
            @NotNull @Size(max = 1000) String notes
    ) {}

    public record UnitBatchUpdateRequest(
            @NotNull @Size(min = 1, max = 100) List<@NotNull Long> ids,
            @NotBlank String status,
            @NotNull Boolean updateSpace,
            Long spaceId
    ) {}

    public record MaintenanceRequest(
            @NotNull Instant startsAt,
            @NotNull Instant endsAt,
            @NotBlank @Size(max = 160) String reason,
            @NotNull @Size(max = 1000) String notes
    ) {}

    public record BatchMaintenanceRequest(
            @NotNull @Size(min = 1, max = 100) List<@NotNull Long> ids,
            @NotNull @Valid MaintenanceRequest maintenance
    ) {}

    public record SpaceAssignmentRequest(Long spaceId) {}

    public record ActiveRequest(@NotNull Boolean active) {}

    public record CourseRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 1000) String description,
            @NotNull @Min(10) @Max(240) Integer durationMinutes,
            @NotNull @Min(1) @Max(200) Integer defaultCapacity,
            @NotBlank @Size(max = 120) String coverKey
    ) {}

    public record CourseSessionRequest(
            @NotNull Long courseId,
            Long coachId,
            @NotNull @Future Instant startsAt,
            @NotNull @Future Instant endsAt,
            @NotNull @Min(1) @Max(200) Integer capacity,
            Long spaceId,
            @NotNull @Size(max = 20) List<@Valid ResourceRequirementRequest> resources
    ) {}

    public record ResourceRequirementRequest(
            @NotNull Long equipmentId,
            @NotNull @Min(1) @Max(100) Integer requiredUnits
    ) {}

    public record CoachAssignmentRequest(
            @NotNull Long coachId,
            @NotNull Long memberId,
            @NotNull LocalDate startsOn,
            LocalDate endsOn
    ) {}

    public record ClosedDayRequest(
            @NotNull LocalDate closedOn,
            LocalTime startsAt,
            LocalTime endsAt,
            @NotBlank @Size(max = 160) String reason
    ) {}

    public record OperationHoursRequest(
            @NotNull LocalTime opensAt,
            @NotNull LocalTime closesAt
    ) {}

    public record ForumReplyRequest(
            @NotBlank @Size(max = 2000) String content
    ) {}
}
