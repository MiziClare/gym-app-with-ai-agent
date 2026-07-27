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
                    "Welcome to Gym Panel", "Book classes, reserve equipment and connect with a coach.");
            jdbc.update("INSERT INTO notices (title, content) VALUES (?, ?)",
                    "Training reminder", "Please arrive ten minutes before your scheduled session.");
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM equipment", Integer.class) == 0) {
            for (var item : List.of(
                    new DemoEquipment("Treadmill", "Cardio", "Commercial treadmill for indoor running.", "treadmill"),
                    new DemoEquipment("Spin Bike", "Cardio", "Adjustable bike for interval training.", "spin-bike"),
                    new DemoEquipment("Rowing Machine", "Cardio", "Full-body low-impact conditioning.", "rowing"),
                    new DemoEquipment("Cable Machine", "Strength", "Multi-station resistance equipment.", "cable"),
                    new DemoEquipment("Swimming Pool", "Aquatics", "Indoor pool for lap and recovery sessions.", "pool")
            )) {
                jdbc.update("""
                        INSERT INTO equipment (name, category, description, cover_key)
                        VALUES (?, ?, ?, ?)
                        """, item.name(), item.category(), item.description(), item.coverKey());
            }
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

    private record DemoEquipment(String name, String category, String description, String coverKey) {}
}
