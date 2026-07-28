package com.gymplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminControllerTest {
    @Test
    void rejectsClosingPastDays() {
        var jdbc = mock(JdbcTemplate.class);
        var error = assertThrows(ResponseStatusException.class,
                () -> new AdminController(jdbc).createClosedDay(
                        new AdminController.ClosedDayRequest(LocalDate.now().minusDays(1), "Closed")));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(jdbc);
    }

    @Test
    void rejectsReopeningPastClosedDays() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("SELECT closed_on"), eq(LocalDate.class), eq(7L)))
                .thenReturn(List.of(LocalDate.now().minusDays(1)));

        var error = assertThrows(ResponseStatusException.class,
                () -> new AdminController(jdbc).deleteClosedDay(7L));

        assertEquals(400, error.getStatusCode().value());
        verify(jdbc, never()).update(eq("DELETE FROM gym_closed_days WHERE id = ?"), eq(7L));
    }
}
