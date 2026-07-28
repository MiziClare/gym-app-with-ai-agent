package com.gymplatform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gym-map")
public class GymMapController {
    private final JdbcTemplate jdbc;

    public GymMapController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    GymMapResponse getMap(@RequestParam Instant from, @RequestParam Instant to) {
        validateRange(from, to);
        var generatedAt = Instant.now();
        var now = LocalTime.now();
        var closedToday = Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT NOT EXISTS (
                    SELECT 1 FROM gym_operation_hours
                    WHERE id = 1 AND opens_at <= ? AND closes_at > ?
                ) OR EXISTS (
                    SELECT 1 FROM gym_closed_days
                    WHERE closed_on = ?
                      AND (starts_at IS NULL OR (starts_at <= ? AND ends_at > ?))
                )
                """, Boolean.class, now, now, LocalDate.now(), now, now));
        var occupancy = jdbc.queryForObject(
                "SELECT COUNT(*) FROM member_visits WHERE checked_out_at IS NULL",
                Integer.class
        );

        var equipmentBySpace = new HashMap<Long, List<EquipmentView>>();
        jdbc.query("""
                SELECT equipment.id, equipment.space_id, equipment.name,
                       equipment.category, equipment.status
                FROM equipment
                WHERE equipment.space_id IS NOT NULL
                  AND equipment.resource_type = 'EQUIPMENT'
                  AND equipment.status <> 'RETIRED'
                ORDER BY equipment.name
                """, result -> {
            equipmentBySpace.computeIfAbsent(result.getLong("space_id"), ignored -> new ArrayList<>())
                .add(new EquipmentView(
                        result.getLong("id"),
                        result.getString("name"),
                        result.getString("category"),
                        result.getString("status")
                ));
        });

        var sessionsBySpace = new HashMap<Long, List<SessionActivity>>();
        jdbc.query("""
                SELECT session.id, session.space_id, course.name AS course_name,
                       coach.display_name AS coach_name, session.starts_at, session.ends_at,
                       session.capacity,
                       SUM(CASE WHEN booking.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS booked_count
                FROM course_sessions session
                JOIN courses course ON course.id = session.course_id
                LEFT JOIN users coach ON coach.id = session.coach_id
                LEFT JOIN bookings booking ON booking.session_id = session.id
                WHERE session.space_id IS NOT NULL
                  AND session.status = 'OPEN'
                  AND session.starts_at < ?
                  AND session.ends_at > ?
                GROUP BY session.id, session.space_id, course.name, coach.display_name,
                         session.starts_at, session.ends_at, session.capacity
                ORDER BY session.starts_at
                """, result -> {
            sessionsBySpace.computeIfAbsent(result.getLong("space_id"), ignored -> new ArrayList<>())
                .add(new SessionActivity(
                        result.getLong("id"),
                        result.getString("course_name"),
                        result.getString("coach_name"),
                        result.getTimestamp("starts_at").toInstant(),
                        result.getTimestamp("ends_at").toInstant(),
                        result.getInt("capacity"),
                        result.getInt("booked_count")
                ));
        }, to, from);

        var spacesByFloor = new HashMap<Long, List<SpaceView>>();
        jdbc.query("""
                SELECT id, floor_id, name, type, x_percent, y_percent,
                       width_percent, height_percent
                FROM gym_spaces
                ORDER BY CASE WHEN type = 'AREA' THEN 0 ELSE 1 END, id
                """, result -> {
            var id = result.getLong("id");
            var equipment = equipmentBySpace.getOrDefault(id, List.of());
            var timeline = sessionsBySpace.getOrDefault(id, List.of());
            var status = statusFor(closedToday, generatedAt, timeline, equipment);
            var current = timeline.stream()
                    .filter(item -> !item.startsAt().isAfter(generatedAt) && item.endsAt().isAfter(generatedAt))
                    .findFirst().orElse(null);
            spacesByFloor.computeIfAbsent(result.getLong("floor_id"), ignored -> new ArrayList<>())
                    .add(new SpaceView(
                            id,
                            result.getString("name"),
                            result.getString("type"),
                            result.getBigDecimal("x_percent"),
                            result.getBigDecimal("y_percent"),
                            result.getBigDecimal("width_percent"),
                            result.getBigDecimal("height_percent"),
                            status,
                            current,
                            equipment,
                            timeline
                    ));
        });

        var floors = jdbc.query("""
                SELECT id, name, sort_order FROM gym_floors ORDER BY sort_order
                """, (result, row) -> new FloorView(
                result.getLong("id"),
                result.getString("name"),
                result.getInt("sort_order"),
                spacesByFloor.getOrDefault(result.getLong("id"), List.of())
        ));
        return new GymMapResponse(generatedAt, occupancy == null ? 0 : occupancy, closedToday, floors);
    }

    static void validateRange(Instant from, Instant to) {
        if (!to.isAfter(from) || Duration.between(from, to).compareTo(Duration.ofDays(8)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gym map range must be within 8 days");
        }
    }

    static String statusFor(
            boolean closed,
            Instant now,
            List<SessionActivity> sessions,
            List<EquipmentView> equipment
    ) {
        if (closed) return "CLOSED";
        if (sessions.stream().anyMatch(item ->
                !item.startsAt().isAfter(now) && item.endsAt().isAfter(now))) return "IN_USE";
        if (equipment.stream().anyMatch(item -> "MAINTENANCE".equals(item.status()))) return "LIMITED_EQUIPMENT";
        return "AVAILABLE";
    }

    public record GymMapResponse(
            Instant generatedAt,
            int currentGymOccupancy,
            boolean closedToday,
            List<FloorView> floors
    ) {}

    public record FloorView(Long id, String name, int sortOrder, List<SpaceView> spaces) {}

    public record SpaceView(
            Long id,
            String name,
            String type,
            BigDecimal x,
            BigDecimal y,
            BigDecimal width,
            BigDecimal height,
            String status,
            SessionActivity currentActivity,
            List<EquipmentView> equipment,
            List<SessionActivity> timeline
    ) {}

    public record EquipmentView(Long id, String name, String category, String status) {}

    public record SessionActivity(
            Long id,
            String courseName,
            String coachName,
            Instant startsAt,
            Instant endsAt,
            int capacity,
            int bookedCount
    ) {}
}
