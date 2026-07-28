package com.gymplatform.controller;

import com.gymplatform.service.SessionSchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Instant;
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
                        "Bike", "Cardio", "Indoor bike", "bikes", 1, 99L)));

        assertEquals(400, error.getStatusCode().value());
        verify(jdbc, never()).update(contains("INSERT INTO equipment"), any(), any(), any(), any(), any());
    }

    @Test
    void rejectsOverlappingUnitMaintenance() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(
                contains("FROM equipment_units"), eq(Long.class), eq(4L)
        )).thenReturn(List.of(4L));
        when(jdbc.queryForObject(
                contains("FROM equipment_maintenance"), eq(Integer.class),
                eq(4L), isNull(), any(Instant.class), any(Instant.class)
        )).thenReturn(1);
        var start = Instant.now().plusSeconds(3600);
        var controller = new AdminController(jdbc, mock(SessionSchedulingService.class));

        var error = assertThrows(ResponseStatusException.class,
                () -> controller.createEquipmentMaintenance(
                        4L,
                        new AdminController.MaintenanceRequest(
                                start, start.plusSeconds(3600), "Inspection", "")
                ));

        assertEquals(409, error.getStatusCode().value());
        verify(jdbc, never()).update(contains("INSERT INTO equipment_maintenance"), any(), any(), any(), any(), any());
    }

    @Test
    void coachAssignmentAlsoEstablishesVisibleConnection() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(contains("FROM users"), eq(Integer.class), anyLong(), anyString()))
                .thenReturn(1);
        when(jdbc.queryForObject(contains("FROM coach_member_assignments"), eq(Integer.class),
                anyLong(), anyLong(), isNull(), any(LocalDate.class))).thenReturn(0);
        var controller = new AdminController(jdbc, mock(SessionSchedulingService.class));

        controller.createCoachAssignment(new AdminController.CoachAssignmentRequest(
                8L, 12L, LocalDate.now(), null));

        verify(jdbc).update(contains("UPDATE coach_connection_requests"), eq(8L), eq(12L));
        verify(jdbc).update(contains("INSERT INTO coach_connection_requests"),
                eq(12L), eq(8L), eq(12L), eq(8L));
    }

    @Test
    void rejectsOpeningMissingPost() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(contains("COUNT(DISTINCT likes.user_id)"), eq(91L)))
                .thenReturn(List.of());

        var error = assertThrows(ResponseStatusException.class,
                () -> new AdminController(jdbc, mock(SessionSchedulingService.class))
                        .postDetails(91L));

        assertEquals(404, error.getStatusCode().value());
    }
}
