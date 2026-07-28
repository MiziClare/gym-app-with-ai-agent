package com.gymplatform.controller;

import com.gymplatform.service.SessionSchedulingService;
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
                () -> new AdminController(jdbc, mock(SessionSchedulingService.class)).createClosedDay(
                        new AdminController.ClosedDayRequest(
                                LocalDate.now().minusDays(1), null, null, "Closed")));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(jdbc);
    }

    @Test
    void rejectsReopeningPastClosedDays() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("SELECT closed_on"), eq(LocalDate.class), eq(7L)))
                .thenReturn(List.of(LocalDate.now().minusDays(1)));

        var error = assertThrows(ResponseStatusException.class,
                () -> new AdminController(jdbc, mock(SessionSchedulingService.class)).deleteClosedDay(7L));

        assertEquals(400, error.getStatusCode().value());
        verify(jdbc, never()).update(eq("DELETE FROM gym_closed_days WHERE id = ?"), eq(7L));
    }

    @Test
    void rejectsUnknownEquipmentLocation() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("FROM gym_spaces"), eq(Long.class), eq(99L)))
                .thenReturn(List.of());
        var controller = new AdminController(jdbc, mock(SessionSchedulingService.class));

        var error = assertThrows(ResponseStatusException.class, () -> controller.createEquipment(
                new AdminController.EquipmentRequest(
                        "Bike", "Cardio", "Indoor bike", "spin-bike", 99L)));

        assertEquals(400, error.getStatusCode().value());
        verify(jdbc, never()).update(contains("INSERT INTO equipment"), any(), any(), any(), any(), any());
    }
}
