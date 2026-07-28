package com.gymplatform.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class EquipmentAvailabilityService {
    private final JdbcTemplate jdbc;

    public EquipmentAvailabilityService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Snapshot snapshot() {
        var items = jdbc.query("""
                SELECT equipment.id, equipment.name, equipment.category,
                       equipment.description, equipment.unit_label,
                       COUNT(unit.id) AS total_units,
                       GREATEST(0,
                           SUM(CASE
                               WHEN unit.base_status = 'AVAILABLE'
                                AND NOT EXISTS (
                                    SELECT 1 FROM equipment_maintenance maintenance
                                    WHERE maintenance.unit_id = unit.id
                                      AND maintenance.starts_at <= CURRENT_TIMESTAMP
                                      AND maintenance.ends_at > CURRENT_TIMESTAMP
                                )
                               THEN 1 ELSE 0 END)
                           - COALESCE(class_usage.required_units, 0)
                       ) AS available_units,
                       GROUP_CONCAT(DISTINCT CONCAT(floor.name, ' · ', space.name)
                           ORDER BY floor.sort_order, space.name SEPARATOR '|') AS locations,
                       MAX(unit.updated_at) AS updated_at
                FROM equipment
                LEFT JOIN equipment_units unit
                    ON unit.equipment_id = equipment.id AND unit.base_status <> 'RETIRED'
                LEFT JOIN gym_spaces space ON space.id = unit.space_id
                LEFT JOIN gym_floors floor ON floor.id = space.floor_id
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
                WHERE equipment.status <> 'RETIRED'
                  AND equipment.resource_type = 'EQUIPMENT'
                GROUP BY equipment.id, equipment.name, equipment.category,
                         equipment.description, equipment.unit_label,
                         class_usage.required_units
                ORDER BY equipment.category, equipment.name
                """, (result, row) -> {
            var total = result.getInt("total_units");
            var available = Math.min(total, result.getInt("available_units"));
            var locations = result.getString("locations");
            return new Item(
                    result.getLong("id"),
                    result.getString("name"),
                    result.getString("category"),
                    result.getString("description"),
                    result.getString("unit_label"),
                    total,
                    available,
                    availabilityStatus(total, available),
                    locations == null || locations.isBlank()
                            ? List.of()
                            : Arrays.asList(locations.split("\\|")),
                    result.getTimestamp("updated_at") == null
                            ? null
                            : result.getTimestamp("updated_at").toInstant()
            );
        });
        return new Snapshot(Instant.now(), items);
    }

    static String availabilityStatus(int total, int available) {
        if (total == 0 || available == 0) return "UNAVAILABLE";
        return available < total ? "LIMITED" : "AVAILABLE";
    }

    public record Snapshot(Instant generatedAt, List<Item> items) {}

    public record Item(
            Long id,
            String name,
            String category,
            String description,
            String unitLabel,
            int totalUnits,
            int availableUnits,
            String availabilityStatus,
            List<String> locations,
            Instant updatedAt
    ) {}
}
