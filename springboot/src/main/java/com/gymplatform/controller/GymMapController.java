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
                SELECT equipment.id, unit.space_id, equipment.name, equipment.category,
                       COUNT(unit.id) AS total_units,
                       GREATEST(0,
                           SUM(CASE WHEN unit.base_status = 'AVAILABLE'
                                 AND maintenance.id IS NULL THEN 1 ELSE 0 END)
                           - CASE WHEN equipment.space_id = unit.space_id
                                  THEN COALESCE(class_usage.required_units, 0) ELSE 0 END
                       ) AS available_units
                FROM equipment
                JOIN equipment_units unit
                  ON unit.equipment_id = equipment.id AND unit.base_status <> 'RETIRED'
                LEFT JOIN equipment_maintenance maintenance
                  ON maintenance.unit_id = unit.id
                 AND maintenance.starts_at <= CURRENT_TIMESTAMP
                 AND maintenance.ends_at > CURRENT_TIMESTAMP
                LEFT JOIN (
                    SELECT requirement.equipment_id,
                           SUM(requirement.required_units) AS required_units
                    FROM course_session_resources requirement
                    JOIN course_sessions session ON session.id = requirement.session_id
                    WHERE session.status = 'OPEN'
                      AND session.starts_at <= CURRENT_TIMESTAMP
                      AND session.ends_at > CURRENT_TIMESTAMP
                    GROUP BY requirement.equipment_id
                ) class_usage ON class_usage.equipment_id = equipment.id
                WHERE unit.space_id IS NOT NULL
                  AND equipment.resource_type = 'EQUIPMENT'
                  AND equipment.status <> 'RETIRED'
                GROUP BY equipment.id, unit.space_id, equipment.name, equipment.category,
                         equipment.space_id, class_usage.required_units
                ORDER BY equipment.name
                """, result -> {
            var total = result.getInt("total_units");
            var available = Math.min(total, result.getInt("available_units"));
            equipmentBySpace.computeIfAbsent(result.getLong("space_id"), ignored -> new ArrayList<>())
                .add(new EquipmentView(
                        result.getLong("id"),
                        result.getString("name"),
                        result.getString("category"),
                        total,
                        available,
                        available == 0 ? "UNAVAILABLE" : available < total ? "LIMITED" : "AVAILABLE"
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
        if (equipment.stream().anyMatch(item -> item.availableUnits() < item.totalUnits())) return "LIMITED_EQUIPMENT";
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

    public record EquipmentView(
            Long id,
            String name,
            String category,
            int totalUnits,
            int availableUnits,
            String availabilityStatus
    ) {}

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
