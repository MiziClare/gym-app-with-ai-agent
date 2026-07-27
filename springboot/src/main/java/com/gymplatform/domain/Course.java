package com.gymplatform.domain;

public record Course(
        Long id,
        String name,
        String description,
        int durationMinutes,
        int defaultCapacity,
        String coverKey,
        boolean active
) {
}
