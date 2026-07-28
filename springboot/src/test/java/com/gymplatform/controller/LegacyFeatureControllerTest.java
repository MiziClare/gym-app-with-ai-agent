package com.gymplatform.controller;

import com.gymplatform.domain.Role;
import com.gymplatform.domain.User;
import com.gymplatform.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LegacyFeatureControllerTest {
    @Test
    void rejectsAnEquipmentReservationOutsideTheAllowedDurations() {
        var jdbc = mock(JdbcTemplate.class);
        var currentUser = mock(CurrentUserService.class);
        var authentication = mock(Authentication.class);
        when(currentUser.require(authentication)).thenReturn(
                new User(1L, "member", "", "Member", "member@example.test",
                        Role.MEMBER, true, Instant.now())
        );
        var startsAt = Instant.now().plusSeconds(7200);
        var invalidRange = assertThrows(ResponseStatusException.class,
                () -> new LegacyFeatureController(jdbc, currentUser)
                        .reserveEquipment(new LegacyFeatureController.EquipmentReservationRequest(
                                1L, startsAt, startsAt.minusSeconds(60)), authentication));
        var tooLong = assertThrows(ResponseStatusException.class,
                () -> new LegacyFeatureController(jdbc, currentUser)
                        .reserveEquipment(new LegacyFeatureController.EquipmentReservationRequest(
                                1L, startsAt, startsAt.plusSeconds(90 * 60)), authentication));

        assertEquals(400, invalidRange.getStatusCode().value());
        assertEquals(400, tooLong.getStatusCode().value());
        verifyNoInteractions(jdbc);
    }

    @Test
    void rejectsOverlappingEquipmentForTheSameMember() {
        var jdbc = mock(JdbcTemplate.class);
        var currentUser = mock(CurrentUserService.class);
        var authentication = mock(Authentication.class);
        when(currentUser.require(authentication)).thenReturn(
                new User(1L, "member", "", "Member", "member@example.test",
                        Role.MEMBER, true, Instant.now())
        );
        when(jdbc.queryForList(
                contains("SELECT id FROM equipment"),
                eq(Long.class), eq(2L)
        )).thenReturn(List.of(2L));
        when(jdbc.queryForObject(
                contains("WHERE member_id = ?"),
                eq(Integer.class), eq(1L), any(Instant.class), any(Instant.class)
        )).thenReturn(1);
        var startsAt = Instant.now().plusSeconds(7200);

        var error = assertThrows(ResponseStatusException.class,
                () -> new LegacyFeatureController(jdbc, currentUser)
                        .reserveEquipment(new LegacyFeatureController.EquipmentReservationRequest(
                                2L, startsAt, startsAt.plusSeconds(30 * 60)), authentication));

        assertEquals(409, error.getStatusCode().value());
        verify(jdbc).queryForObject(
                contains("SELECT id FROM users"),
                eq(Long.class), eq(1L)
        );
    }

    @Test
    void rejectsCoachAppointmentOutsideCoachAvailability() {
        var jdbc = mock(JdbcTemplate.class);
        var currentUser = mock(CurrentUserService.class);
        var authentication = mock(Authentication.class);
        when(currentUser.require(authentication)).thenReturn(
                new User(1L, "member", "", "Member", "member@example.test",
                        Role.MEMBER, true, Instant.now())
        );
        when(jdbc.queryForList(
                contains("SELECT id FROM users"),
                eq(Long.class), eq(2L)
        )).thenReturn(List.of(2L));
        when(jdbc.queryForObject(
                contains("FROM coach_availability"),
                eq(Integer.class), eq(2L), anyInt(), any(), any()
        )).thenReturn(0);
        var startsAt = Instant.now().plusSeconds(7200);

        var error = assertThrows(ResponseStatusException.class,
                () -> new LegacyFeatureController(jdbc, currentUser)
                        .requestCoachAppointment(new LegacyFeatureController.CoachAppointmentRequest(
                                2L, startsAt, "Strength"), authentication));

        assertEquals(409, error.getStatusCode().value());
        verify(jdbc, never()).update(contains("INSERT INTO coach_appointments"), any(), any(), any(), any());
    }
}
