package com.gymplatform.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class) == 0) {
            insertUser("admin", "Admin Demo", "admin@gym.demo", "ADMIN");
            insertUser("coach", "Maya Chen", "coach@gym.demo", "COACH");
            insertUser("member", "Alex Morgan", "member@gym.demo", "MEMBER");

            var coachId = jdbc.queryForObject(
                    "SELECT id FROM users WHERE username = 'coach'", Long.class
            );
            jdbc.update(
                    "INSERT INTO coach_profiles (user_id, bio, specialties) VALUES (?, ?, ?)",
                    coachId,
                    "Strength coach focused on sustainable progress and inclusive training.",
                    "Strength, mobility, beginner programs"
            );

            var courses = List.of(
                    new DemoCourse("Strength Foundations", "Build confidence with essential movement patterns.", 50, 12, "strength"),
                    new DemoCourse("Mobility Flow", "A low-impact class for mobility, balance, and recovery.", 45, 16, "mobility"),
                    new DemoCourse("Cardio Circuit", "Intervals combining simple cardio and bodyweight stations.", 40, 14, "cardio")
            );
            for (var course : courses) {
                jdbc.update(
                        """
                        INSERT INTO courses
                            (name, description, duration_minutes, default_capacity, cover_key)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        course.name(), course.description(), course.duration(), course.capacity(), course.coverKey()
                );
            }

            var firstMonday = Instant.now()
                    .atZone(ZoneOffset.UTC)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    .withHour(22)
                    .withMinute(0)
                    .withSecond(0)
                    .toInstant();
            var courseIds = jdbc.queryForList("SELECT id FROM courses ORDER BY id", Long.class);
            for (int day = 0; day < 3; day++) {
                for (int index = 0; index < courseIds.size(); index++) {
                    var startsAt = firstMonday.plusSeconds((day * 24L + index * 2L) * 3600);
                    jdbc.update(
                            """
                            INSERT INTO course_sessions
                                (course_id, coach_id, starts_at, ends_at, capacity)
                            VALUES (?, ?, ?, ?, ?)
                            """,
                            courseIds.get(index),
                            coachId,
                            Timestamp.from(startsAt),
                            Timestamp.from(startsAt.plusSeconds(courses.get(index).duration() * 60L)),
                            courses.get(index).capacity()
                    );
                }
            }
        }

        seedDemoMembership();
        seedDemoCoachAssignment();
        seedLegacyFeatures();
        seedGymLayout();
        seedEquipmentUnits();
        seedOperationalEquipmentState();
    }

    private void seedDemoCoachAssignment() {
        var coachIds = jdbc.queryForList(
                "SELECT id FROM users WHERE username = 'coach' AND role = 'COACH'",
                Long.class
        );
        var memberIds = jdbc.queryForList(
                "SELECT id FROM users WHERE username = 'member' AND role = 'MEMBER'",
                Long.class
        );
        if (coachIds.isEmpty() || memberIds.isEmpty()) {
            return;
        }
        jdbc.update("""
                INSERT INTO coach_member_assignments (coach_id, member_id, starts_on)
                SELECT ?, ?, CURRENT_DATE
                WHERE NOT EXISTS (
                    SELECT 1 FROM coach_member_assignments
                    WHERE coach_id = ? AND member_id = ? AND status = 'ACTIVE'
                )
                """,
                coachIds.getFirst(), memberIds.getFirst(),
                coachIds.getFirst(), memberIds.getFirst());
    }

    private void seedDemoMembership() {
        var members = jdbc.queryForList(
                "SELECT id FROM users WHERE username = 'member' AND role = 'MEMBER'",
                Long.class
        );
        if (members.isEmpty()) {
            return;
        }
        var memberId = members.getFirst();
        jdbc.update("""
                INSERT IGNORE INTO member_profiles (member_id, member_number, credential_id)
                VALUES (?, CONCAT('GF-', LPAD(?, 6, '0')), UUID())
                """, memberId, memberId);
        jdbc.update("""
                INSERT INTO memberships (member_id, plan_id, starts_on)
                SELECT ?, plan.id, CURRENT_DATE
                FROM membership_plans plan
                WHERE plan.name = 'Legacy Unlimited'
                  AND NOT EXISTS (
                      SELECT 1 FROM memberships existing WHERE existing.member_id = ?
                  )
                """, memberId, memberId);
    }

    private void seedLegacyFeatures() {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM notices", Integer.class) == 0) {
            jdbc.update("INSERT INTO notices (title, content) VALUES (?, ?)",
                    "Welcome to Gym Panel", "Check walk-in equipment availability and connect with a coach.");
            jdbc.update("INSERT INTO notices (title, content) VALUES (?, ?)",
                    "Training reminder", "Please arrive ten minutes before your scheduled session.");
        }
        for (var item : equipmentCatalogue()) {
            jdbc.update("""
                    INSERT INTO equipment
                        (name, category, description, unit_label, cover_key)
                    SELECT ?, ?, ?, ?, 'equipment'
                    WHERE NOT EXISTS (
                        SELECT 1 FROM equipment
                        WHERE name = ? AND resource_type = 'EQUIPMENT' AND status <> 'RETIRED'
                    )
                    """, item.name(), item.category(), item.description(),
                    item.unitLabel(), item.name());
            jdbc.update("""
                    UPDATE equipment
                    SET category = ?, description = ?, unit_label = ?
                    WHERE name = ? AND resource_type = 'EQUIPMENT' AND status <> 'RETIRED'
                    """, item.category(), item.description(), item.unitLabel(), item.name());
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM posts", Integer.class) == 0) {
            var memberId = jdbc.queryForObject("SELECT id FROM users WHERE username = 'member'", Long.class);
            var coachId = jdbc.queryForObject("SELECT id FROM users WHERE username = 'coach'", Long.class);
            jdbc.update("INSERT INTO posts (author_id, title, content) VALUES (?, ?, ?)",
                    coachId, "Consistency beats intensity", "Choose a plan you can repeat and build from there.");
            jdbc.update("INSERT INTO posts (author_id, title, content) VALUES (?, ?, ?)",
                    memberId, "My first week", "Mobility Flow was a great way to get started.");
        }
    }

    private void seedEquipmentUnits() {
        for (var item : equipmentCatalogue()) {
            var equipmentIds = jdbc.queryForList("""
                    SELECT id FROM equipment
                    WHERE name = ? AND resource_type = 'EQUIPMENT' AND status <> 'RETIRED'
                    ORDER BY id LIMIT 1
                    """, Long.class, item.name());
            var spaceIds = jdbc.queryForList("""
                    SELECT id FROM gym_spaces WHERE name = ? ORDER BY id LIMIT 1
                    """, Long.class, item.spaceName());
            if (equipmentIds.isEmpty() || spaceIds.isEmpty()) continue;
            var equipmentId = equipmentIds.getFirst();
            var spaceId = spaceIds.getFirst();
            jdbc.update("UPDATE equipment SET space_id = COALESCE(space_id, ?) WHERE id = ?",
                    spaceId, equipmentId);
            var existing = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM equipment_units WHERE equipment_id = ?",
                    Integer.class, equipmentId);
            for (int sequence = (existing == null ? 0 : existing) + 1;
                 sequence <= item.count(); sequence++) {
                jdbc.update("""
                        INSERT INTO equipment_units (equipment_id, asset_code, space_id)
                        VALUES (?, ?, ?)
                        """, equipmentId, "DEMO-%04d-%03d".formatted(equipmentId, sequence), spaceId);
            }
        }
    }

    private void seedOperationalEquipmentState() {
        var catalogue = equipmentCatalogue();
        for (int equipmentIndex = 0; equipmentIndex < catalogue.size(); equipmentIndex++) {
            var item = catalogue.get(equipmentIndex);
            var unitIds = jdbc.queryForList("""
                    SELECT unit.id
                    FROM equipment_units unit
                    JOIN equipment ON equipment.id = unit.equipment_id
                    WHERE equipment.name = ?
                      AND unit.asset_code LIKE 'DEMO-%'
                      AND unit.base_status <> 'RETIRED'
                    ORDER BY unit.asset_code
                    """, Long.class, item.name());
            for (int index = 0; index < unitIds.size(); index++) {
                var sequence = index + 1;
                var unitId = unitIds.get(index);
                var status = demoBaseStatus(equipmentIndex, sequence, unitIds.size());
                jdbc.update("""
                        UPDATE equipment_units
                        SET serial_number = ?,
                            purchased_on = ?,
                            base_status = ?,
                            notes = ?
                        WHERE id = ?
                          AND serial_number IS NULL
                          AND (notes IS NULL OR notes = '')
                        """,
                        "GF-%03d-%03d".formatted(equipmentIndex + 1, sequence),
                        LocalDate.now(ZoneOffset.UTC).minusMonths(6L + (equipmentIndex * 3L + sequence) % 42),
                        status,
                        demoUnitNote(status),
                        unitId);

                if (isActiveMaintenanceUnit(equipmentIndex, sequence, unitIds.size())) {
                    seedDemoMaintenance(
                            unitId,
                            -45,
                            120,
                            "Preventive safety inspection",
                            "Demo operating state: work order PM-%03d is in progress."
                                    .formatted(equipmentIndex + 1)
                    );
                } else if (isUpcomingMaintenanceUnit(equipmentIndex, sequence, unitIds.size())) {
                    seedDemoMaintenance(
                            unitId,
                            24 * 60,
                            26 * 60,
                            "Scheduled preventive service",
                            "Demo operating state: service is planned for tomorrow."
                    );
                }
            }
        }
    }

    private void seedDemoMaintenance(
            Long unitId,
            int startsInMinutes,
            int endsInMinutes,
            String reason,
            String notes
    ) {
        jdbc.update("""
                INSERT INTO equipment_maintenance (unit_id, starts_at, ends_at, reason, notes)
                SELECT ?,
                       TIMESTAMPADD(MINUTE, ?, CURRENT_TIMESTAMP),
                       TIMESTAMPADD(MINUTE, ?, CURRENT_TIMESTAMP),
                       ?, ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM equipment_maintenance
                    WHERE unit_id = ? AND ends_at > CURRENT_TIMESTAMP AND notes = ?
                )
                """,
                unitId, startsInMinutes, endsInMinutes, reason, notes,
                unitId, notes);
    }

    private String demoBaseStatus(int equipmentIndex, int sequence, int count) {
        if (count >= 3 && sequence == count && equipmentIndex % 6 == 0) {
            return "OUT_OF_SERVICE";
        }
        if (count >= 2 && sequence == 1 && equipmentIndex % 3 != 2
                && !isActiveMaintenanceUnit(equipmentIndex, sequence, count)) {
            return "IN_USE";
        }
        if (count >= 6 && sequence == 2 && equipmentIndex % 4 == 0) {
            return "IN_USE";
        }
        return "AVAILABLE";
    }

    private boolean isActiveMaintenanceUnit(int equipmentIndex, int sequence, int count) {
        if (equipmentIndex == 24 && sequence == 1) {
            return true;
        }
        var outOfServiceOffset = equipmentIndex % 6 == 0 ? 1 : 0;
        return count >= 2 && sequence == count - outOfServiceOffset && equipmentIndex % 4 == 1;
    }

    private boolean isUpcomingMaintenanceUnit(int equipmentIndex, int sequence, int count) {
        return count >= 3 && sequence == count - 1 && equipmentIndex % 7 == 3;
    }

    private String demoUnitNote(String status) {
        return switch (status) {
            case "IN_USE" -> "Demo operating state: currently in use during staffed hours.";
            case "OUT_OF_SERVICE" -> "Demo operating state: awaiting a replacement part.";
            default -> "Demo operating state: opening inspection completed with no issues.";
        };
    }

    private List<DemoEquipment> equipmentCatalogue() {
        return List.of(
                new DemoEquipment("Treadmill", "Cardio", "Commercial treadmill for walking and running.", "machines", 12, "Cardio Zone"),
                new DemoEquipment("Elliptical", "Cardio", "Low-impact full-body cardio trainer.", "machines", 6, "Cardio Zone"),
                new DemoEquipment("Stationary Bike", "Cardio", "Adjustable upright indoor bike.", "bikes", 8, "Cardio Zone"),
                new DemoEquipment("Rowing Machine", "Cardio", "Full-body low-impact conditioning.", "machines", 4, "Cardio Zone"),
                new DemoEquipment("Stair Climber", "Cardio", "Continuous stair cardio machine.", "machines", 3, "Cardio Zone"),
                new DemoEquipment("Cable Station", "Strength", "Adjustable multi-station resistance equipment.", "stations", 4, "Strength Zone"),
                new DemoEquipment("Chest Press", "Strength", "Selectorized chest press machine.", "machines", 3, "Strength Zone"),
                new DemoEquipment("Lat Pulldown", "Strength", "Selectorized back training machine.", "machines", 3, "Strength Zone"),
                new DemoEquipment("Leg Press", "Strength", "Lower-body press machine.", "machines", 3, "Strength Zone"),
                new DemoEquipment("Leg Extension / Curl", "Strength", "Lower-body extension and curl station.", "machines", 4, "Strength Zone"),
                new DemoEquipment("Power Rack", "Free Weights", "Rack for barbell strength training.", "racks", 6, "Strength Zone"),
                new DemoEquipment("Smith Machine", "Free Weights", "Guided barbell training station.", "machines", 3, "Strength Zone"),
                new DemoEquipment("Adjustable Bench", "Free Weights", "Multi-angle free-weight bench.", "benches", 10, "Strength Zone"),
                new DemoEquipment("Dumbbell Station", "Free Weights", "Full dumbbell rack and lifting position.", "stations", 4, "Strength Zone"),
                new DemoEquipment("Kettlebell Set", "Functional", "Range of kettlebells for functional training.", "sets", 4, "Studio A"),
                new DemoEquipment("Suspension Trainer", "Functional", "Bodyweight suspension training point.", "stations", 4, "Studio A"),
                new DemoEquipment("Turf / Sled Lane", "Functional", "Indoor lane for sled and agility work.", "lanes", 2, "Studio A"),
                new DemoEquipment("Group Fitness Studio", "Studios", "Flexible studio for instructor-led classes.", "studios", 1, "Studio A"),
                new DemoEquipment("Cycle Studio", "Studios", "Dedicated indoor cycling studio.", "studios", 1, "Studio A"),
                new DemoEquipment("Yoga / Mobility Studio", "Studios", "Quiet studio for yoga and mobility.", "studios", 1, "Studio A"),
                new DemoEquipment("Lap Pool Lane", "Aquatics & Recovery", "Indoor lane for lap swimming.", "lanes", 6, "Pool & Recovery"),
                new DemoEquipment("Leisure Pool", "Aquatics & Recovery", "Shallow pool for recreation and recovery.", "pools", 1, "Pool & Recovery"),
                new DemoEquipment("Hot Tub", "Aquatics & Recovery", "Shared warm-water recovery pool.", "pools", 1, "Pool & Recovery"),
                new DemoEquipment("Sauna", "Aquatics & Recovery", "Dry heat recovery room.", "rooms", 2, "Pool & Recovery"),
                new DemoEquipment("Steam Room", "Aquatics & Recovery", "Steam recovery room.", "rooms", 1, "Pool & Recovery"),
                new DemoEquipment("Gymnasium Court", "Courts", "Full multi-sport gymnasium court.", "courts", 1, "Studio A"),
                new DemoEquipment("Badminton / Pickleball Court", "Courts", "Convertible racket-sport court.", "courts", 3, "Studio A"),
                new DemoEquipment("Squash Court", "Courts", "Enclosed squash court.", "courts", 2, "Studio A")
        );
    }

    private void seedGymLayout() {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM gym_spaces", Integer.class) > 0) {
            return;
        }
        var floorId = jdbc.queryForObject(
                "SELECT id FROM gym_floors ORDER BY sort_order LIMIT 1", Long.class);
        jdbc.update("UPDATE gym_floors SET name = 'Main Floor' WHERE id = ?", floorId);
        for (var space : List.of(
                new DemoSpace("Reception", "AREA", 2, 3, 18, 14),
                new DemoSpace("Cardio Zone", "AREA", 22, 3, 36, 36),
                new DemoSpace("Strength Zone", "AREA", 60, 3, 38, 36),
                new DemoSpace("Studio A", "ROOM", 2, 43, 47, 54),
                new DemoSpace("Pool & Recovery", "ROOM", 51, 43, 23, 54),
                new DemoSpace("Locker Rooms", "ROOM", 76, 43, 22, 54)
        )) {
            jdbc.update("""
                    INSERT INTO gym_spaces
                        (floor_id, name, type, x_percent, y_percent, width_percent, height_percent)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, floorId, space.name(), space.type(), space.x(), space.y(),
                    space.width(), space.height());
        }
        assignEquipment(floorId, "Cardio Zone", "Treadmill", "Spin Bike", "Rowing Machine");
        assignEquipment(floorId, "Strength Zone", "Cable Machine");
        assignEquipment(floorId, "Pool & Recovery", "Swimming Pool");
        assignCourse(floorId, "Strength Foundations", "Strength Zone");
        assignCourse(floorId, "Mobility Flow", "Pool & Recovery");
        assignCourse(floorId, "Cardio Circuit", "Studio A");
    }

    private void assignEquipment(Long floorId, String spaceName, String... equipmentNames) {
        var placeholders = String.join(", ", java.util.Collections.nCopies(equipmentNames.length, "?"));
        var arguments = new Object[equipmentNames.length + 2];
        arguments[0] = floorId;
        arguments[1] = spaceName;
        System.arraycopy(equipmentNames, 0, arguments, 2, equipmentNames.length);
        jdbc.update("""
                UPDATE equipment
                SET space_id = (
                    SELECT id FROM gym_spaces WHERE floor_id = ? AND name = ?
                )
                WHERE name IN (%s)
                """.formatted(placeholders), arguments);
    }

    private void assignCourse(Long floorId, String courseName, String spaceName) {
        jdbc.update("""
                UPDATE course_sessions session
                JOIN courses course ON course.id = session.course_id
                SET session.space_id = (
                    SELECT id FROM gym_spaces WHERE floor_id = ? AND name = ?
                )
                WHERE course.name = ?
                """, floorId, spaceName, courseName);
    }

    private void insertUser(String username, String displayName, String email, String role) {
        jdbc.update(
                """
                INSERT INTO users (username, password_hash, display_name, email, role)
                VALUES (?, ?, ?, ?, ?)
                """,
                username,
                passwordEncoder.encode("GymDemo123!"),
                displayName,
                email,
                role
        );
    }

    private record DemoCourse(
            String name,
            String description,
            int duration,
            int capacity,
            String coverKey
    ) {
    }

    private record DemoEquipment(
            String name,
            String category,
            String description,
            String unitLabel,
            int count,
            String spaceName
    ) {}

    private record DemoSpace(String name, String type, int x, int y, int width, int height) {}
}
