package com.gymplatform.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;

public final class GymOperations {
    private GymOperations() {}

    public static void requireOpen(JdbcTemplate jdbc, Instant startsAt, Instant endsAt) {
        var zone = ZoneId.systemDefault();
        var start = startsAt.atZone(zone);
        var end = endsAt.atZone(zone);
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking must be within one operating day");
        }
        var open = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM gym_operation_hours
                WHERE id = 1 AND opens_at <= ? AND closes_at >= ?
                  AND NOT EXISTS (
                    SELECT 1 FROM gym_closed_days
                    WHERE closed_on = ?
                      AND (starts_at IS NULL OR (starts_at < ? AND ends_at > ?))
                  )
                """, Integer.class, start.toLocalTime(), end.toLocalTime(), start.toLocalDate(),
                end.toLocalTime(), start.toLocalTime());
        if (open == null || open == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Gym is closed at that time");
        }
    }
}
