package com.gymplatform.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionSchedulingServiceTest {
    @Test
    void rejectsAnOverlappingExclusiveResource() {
        var jdbc = mock(JdbcTemplate.class);
        var service = new SessionSchedulingService(jdbc);
        var start = Instant.now().plusSeconds(3600);
        var request = new SessionSchedulingService.ScheduleRequest(
                2L, null, start, start.plusSeconds(3600), 12, List.of(8L)
        );
        when(jdbc.queryForList(
                contains("FROM courses"), eq(Long.class), eq(2L)
        )).thenReturn(List.of(2L));
        when(jdbc.queryForObject(
                contains("gym_closed_days"), eq(Integer.class), any()
        )).thenReturn(0);
        when(jdbc.queryForList(
                contains("FROM equipment"), eq(Long.class), eq(8L)
        )).thenReturn(List.of(8L));
        when(jdbc.queryForObject(
                contains("course_session_resources"), eq(Integer.class),
                eq(8L), any(Instant.class), any(Instant.class),
                eq(8L), any(Instant.class), any(Instant.class)
        )).thenReturn(1);

        var error = assertThrows(ResponseStatusException.class, () -> service.schedule(request));

        assertEquals(409, error.getStatusCode().value());
        verify(jdbc, never()).update(contains("INSERT INTO course_sessions"), any(), any(), any(), any(), any());
    }
}
