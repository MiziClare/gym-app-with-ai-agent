package com.gymplatform.controller;

import jakarta.validation.Valid;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final JdbcTemplate jdbc;

    public AdminController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/overview")
    Map<String, Object> overview() {
        return Map.of(
                "memberCount", count("SELECT COUNT(*) FROM users WHERE role = 'MEMBER'"),
                "coachCount", count("SELECT COUNT(*) FROM users WHERE role = 'COACH'"),
                "courseCount", count("SELECT COUNT(*) FROM courses WHERE active = TRUE"),
                "bookingCount", count("SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED'"),
                "currentOccupancy", count("SELECT COUNT(*) FROM member_visits WHERE checked_out_at IS NULL"),
                "equipmentCount", count("SELECT COUNT(*) FROM equipment WHERE status <> 'RETIRED'"),
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
                        SELECT status, COUNT(*) AS total
                        FROM equipment
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
    void createEquipment(@Valid @RequestBody EquipmentRequest body) {
        jdbc.update("""
                INSERT INTO equipment (name, category, description, cover_key)
                VALUES (?, ?, ?, ?)
                """, body.name().trim(), body.category().trim(),
                body.description().trim(), body.coverKey().trim());
    }

    @PatchMapping("/equipment/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateEquipmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentStatusRequest body
    ) {
        if (!Set.of("AVAILABLE", "MAINTENANCE", "RETIRED").contains(body.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported equipment status");
        }
        if (jdbc.update("UPDATE equipment SET status = ? WHERE id = ?", body.status(), id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found");
        }
    }

    @GetMapping("/equipment-reservations")
    List<Map<String, Object>> equipmentReservations() {
        return jdbc.queryForList("""
                SELECT r.id, e.name AS equipmentName, u.display_name AS memberName,
                       r.starts_at AS startsAt, r.ends_at AS endsAt, r.status
                FROM equipment_reservations r
                JOIN equipment e ON e.id = r.equipment_id
                JOIN users u ON u.id = r.member_id
                ORDER BY r.starts_at DESC
                """);
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
                JOIN users coach ON coach.id = s.coach_id
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

    public record NoticeRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 1000) String content
    ) {}

    public record EquipmentRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 1000) String description,
            @NotBlank @Size(max = 120) String coverKey
    ) {}

    public record EquipmentStatusRequest(@NotBlank String status) {}

    public record ActiveRequest(@NotNull Boolean active) {}

    public record CourseRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 1000) String description,
            @NotNull @Min(10) @Max(240) Integer durationMinutes,
            @NotNull @Min(1) @Max(200) Integer defaultCapacity,
            @NotBlank @Size(max = 120) String coverKey
    ) {}

    public record CoachAssignmentRequest(
            @NotNull Long coachId,
            @NotNull Long memberId,
            @NotNull LocalDate startsOn,
            LocalDate endsOn
    ) {}
}
