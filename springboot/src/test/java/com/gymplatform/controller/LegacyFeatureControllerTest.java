package com.gymplatform.controller;

import com.gymplatform.domain.Role;
import com.gymplatform.domain.User;
import com.gymplatform.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

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
}
