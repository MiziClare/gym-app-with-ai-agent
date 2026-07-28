package com.gymplatform.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentAvailabilityServiceTest {
    @Test
    void classifiesAggregateAvailability() {
        assertEquals("AVAILABLE", EquipmentAvailabilityService.availabilityStatus(8, 8));
        assertEquals("LIMITED", EquipmentAvailabilityService.availabilityStatus(8, 3));
        assertEquals("UNAVAILABLE", EquipmentAvailabilityService.availabilityStatus(8, 0));
        assertEquals("UNAVAILABLE", EquipmentAvailabilityService.availabilityStatus(0, 0));
    }
}
