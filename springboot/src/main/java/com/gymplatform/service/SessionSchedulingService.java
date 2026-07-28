package com.gymplatform.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.TreeSet;

@Service
public class SessionSchedulingService {
    private final JdbcTemplate jdbc;

    public SessionSchedulingService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public Long schedule(ScheduleRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        if (jdbc.queryForList(
                "SELECT id FROM courses WHERE id = ? AND active = TRUE FOR UPDATE",
                Long.class, request.courseId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        var closed = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gym_closed_days WHERE closed_on = ?",
                Integer.class,
                request.startsAt().atZone(ZoneId.systemDefault()).toLocalDate());
        if (closed != null && closed > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gym is closed on that date");
        }
        if (request.coachId() != null) {
            requireAvailableCoach(request);
        }
        if (request.spaceId() != null) {
            requireAvailableSpace(request.spaceId(), request.startsAt(), request.endsAt());
        }

        var resources = new TreeSet<>(request.resourceIds());
        if (resources.size() != request.resourceIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources cannot be repeated");
        }
        for (var resourceId : resources) {
            requireAvailableResource(resourceId, request.startsAt(), request.endsAt());
        }

        jdbc.update("""
                INSERT INTO course_sessions
                    (course_id, coach_id, starts_at, ends_at, capacity)
                VALUES (?, ?, ?, ?, ?)
                """, request.courseId(), request.coachId(), request.startsAt(),
                request.endsAt(), request.capacity());
        var sessionId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        for (var resourceId : resources) {
            jdbc.update("""
                    INSERT INTO course_session_resources (session_id, equipment_id)
                    VALUES (?, ?)
                    """, sessionId, resourceId);
        }
        return sessionId;
    }

    private void requireAvailableCoach(ScheduleRequest request) {
        if (jdbc.queryForList(
                "SELECT id FROM users WHERE id = ? AND role = 'COACH' AND active = TRUE FOR UPDATE",
                Long.class, request.coachId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found");
        }
        var zone = ZoneId.systemDefault();
        var startsOn = request.startsAt().atZone(zone);
        var endsOn = request.endsAt().atZone(zone);
        if (!startsOn.toLocalDate().equals(endsOn.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coached sessions cannot span multiple days");
        }
        var availability = jdbc.queryForObject("""
                SELECT COUNT(*) FROM coach_availability
                WHERE coach_id = ? AND day_of_week = ?
                  AND starts_at <= ? AND ends_at >= ?
                """, Integer.class, request.coachId(), startsOn.getDayOfWeek().getValue(),
                startsOn.toLocalTime(), endsOn.toLocalTime());
        if (availability == null || availability == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coach is not available at that time");
        }
        var conflicts = jdbc.queryForObject("""
                SELECT (
                    SELECT COUNT(*) FROM course_sessions
                    WHERE coach_id = ? AND status = 'OPEN'
                      AND starts_at < ? AND ends_at > ?
                ) + (
                    SELECT COUNT(*) FROM coach_appointments
                    WHERE coach_id = ? AND status IN ('PENDING', 'CONFIRMED')
                      AND starts_at < ? AND starts_at + INTERVAL 60 MINUTE > ?
                )
                """, Integer.class,
                request.coachId(), request.endsAt(), request.startsAt(),
                request.coachId(), request.endsAt(), request.startsAt());
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coach is already booked for that time");
        }
    }

    private void requireAvailableResource(Long resourceId, Instant startsAt, Instant endsAt) {
        if (jdbc.queryForList("""
                SELECT id FROM equipment
                WHERE id = ? AND status = 'AVAILABLE' AND resource_type = 'EQUIPMENT'
                FOR UPDATE
                """, Long.class, resourceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource is not available");
        }
        var conflicts = jdbc.queryForObject("""
                SELECT (
                    SELECT COUNT(*)
                    FROM course_session_resources requirement
                    JOIN course_sessions session ON session.id = requirement.session_id
                    WHERE requirement.equipment_id = ? AND session.status = 'OPEN'
                      AND session.starts_at < ? AND session.ends_at > ?
                ) + (
                    SELECT COUNT(*) FROM equipment_reservations
                    WHERE equipment_id = ? AND status = 'CONFIRMED'
                      AND starts_at < ? AND ends_at > ?
                )
                """, Integer.class,
                resourceId, endsAt, startsAt,
                resourceId, endsAt, startsAt);
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resource is already booked for that time");
        }
    }

    private void requireAvailableSpace(Long spaceId, Instant startsAt, Instant endsAt) {
        if (jdbc.queryForList(
                "SELECT id FROM gym_spaces WHERE id = ? FOR UPDATE",
                Long.class, spaceId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Space not found");
        }
        var conflicts = jdbc.queryForObject("""
                SELECT COUNT(*) FROM course_sessions
                WHERE space_id = ? AND status = 'OPEN'
                  AND starts_at < ? AND ends_at > ?
                """, Integer.class, spaceId, endsAt, startsAt);
        if (conflicts != null && conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Space is already booked for that time");
        }
    }

    public record ScheduleRequest(
            Long courseId,
            Long coachId,
            Instant startsAt,
            Instant endsAt,
            int capacity,
            Long spaceId,
            List<Long> resourceIds
    ) {}
}
