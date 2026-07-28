package com.gymplatform.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymOperationsTest {
    @Test
    void rejectsAClosedTimeRange() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(), any(), any(), any(), any()))
                .thenReturn(0);

        var error = assertThrows(ResponseStatusException.class, () -> GymOperations.requireOpen(
                jdbc, Instant.parse("2026-08-03T16:00:00Z"), Instant.parse("2026-08-03T17:00:00Z")));

        assertEquals(409, error.getStatusCode().value());
    }
}
