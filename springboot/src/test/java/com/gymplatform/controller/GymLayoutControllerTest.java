package com.gymplatform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.jdbc.core.JdbcTemplate;

class GymLayoutControllerTest {
    @Test
    void rejectsSpacesOutsideTheFloor() {
        var space = new GymLayoutController.SpaceRequest(
                null, "Studio", "ROOM",
                decimal(80), decimal(10), decimal(25), decimal(30)
        );
        var request = new GymLayoutController.LayoutRequest(List.of(
                new GymLayoutController.FloorRequest(null, "Floor 1", List.of(space))
        ));

        var error = assertThrows(ResponseStatusException.class,
                () -> GymLayoutController.validateLayout(request));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void acceptsValidRoomsAndAreas() {
        var request = new GymLayoutController.LayoutRequest(List.of(
                new GymLayoutController.FloorRequest(1L, "Floor 1", List.of(
                        new GymLayoutController.SpaceRequest(
                                2L, "Studio", "ROOM",
                                decimal(5), decimal(5), decimal(40), decimal(35)),
                        new GymLayoutController.SpaceRequest(
                                null, "Stretching", "AREA",
                                decimal(50), decimal(10), decimal(30), decimal(20))
                ))
        ));

        GymLayoutController.validateLayout(request);
    }

    @Test
    void rejectsDeletingLinkedSpaces() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(
                org.mockito.ArgumentMatchers.contains("FROM equipment"),
                eq(Integer.class), eq(7L), eq(7L), eq(7L)
        )).thenReturn(2);
        var controller = new GymLayoutController(jdbc);

        var error = assertThrows(ResponseStatusException.class,
                () -> controller.requireUnlinkedDeletions(Set.of(7L), Set.of()));

        assertEquals(409, error.getStatusCode().value());
    }

    private static BigDecimal decimal(int value) {
        return BigDecimal.valueOf(value);
    }
}
