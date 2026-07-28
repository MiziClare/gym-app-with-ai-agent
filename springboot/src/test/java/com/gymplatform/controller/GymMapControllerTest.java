package com.gymplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GymMapControllerTest {
    private static final Instant NOW = Instant.parse("2026-07-28T16:00:00Z");
    private static final GymMapController.SessionActivity CURRENT =
            new GymMapController.SessionActivity(
                    1L, "Yoga", "Coach", NOW.minusSeconds(60), NOW.plusSeconds(60), 20, 8
            );
    private static final GymMapController.EquipmentView LIMITED =
            new GymMapController.EquipmentView(1L, "Bike", "Cardio", 4, 2, "LIMITED");

    @Test
    void appliesDocumentedStatusPriority() {
        assertEquals("CLOSED", GymMapController.statusFor(true, NOW, List.of(CURRENT), List.of(LIMITED)));
        assertEquals("IN_USE", GymMapController.statusFor(false, NOW, List.of(CURRENT), List.of(LIMITED)));
        assertEquals("LIMITED_EQUIPMENT", GymMapController.statusFor(false, NOW, List.of(), List.of(LIMITED)));
        assertEquals("AVAILABLE", GymMapController.statusFor(false, NOW, List.of(), List.of()));
    }

    @Test
    void rejectsRangesLongerThanEightDays() {
        var error = assertThrows(ResponseStatusException.class,
                () -> GymMapController.validateRange(NOW, NOW.plus(8, ChronoUnit.DAYS).plusSeconds(1)));
        assertEquals(400, error.getStatusCode().value());
    }
}
