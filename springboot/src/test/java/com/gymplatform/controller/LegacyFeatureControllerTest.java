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
                contains("gym_operation_hours"), eq(Integer.class),
                any(), any(), any(), any(), any()
        )).thenReturn(1);
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

    @Test
    void onlyCancelsCoachAppointmentsThatHaveNotEnded() {
        var jdbc = mock(JdbcTemplate.class);
        var currentUser = mock(CurrentUserService.class);
        var authentication = mock(Authentication.class);
        when(currentUser.require(authentication)).thenReturn(
                new User(1L, "member", "", "Member", "member@example.test",
                        Role.MEMBER, true, Instant.now())
        );
        when(jdbc.update(contains("UPDATE coach_appointments"), eq(3L), eq(1L))).thenReturn(1);
        var controller = new LegacyFeatureController(jdbc, currentUser);

        controller.cancelCoachAppointment(3L, authentication);

        verify(jdbc).update(contains("starts_at + INTERVAL 60 MINUTE > CURRENT_TIMESTAMP"), eq(3L), eq(1L));
    }
}
