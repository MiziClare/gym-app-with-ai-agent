package com.gymplatform.domain;

import java.time.Instant;

public record SessionView(
        Long id,
        Long courseId,
        String courseName,
        Long coachId,
        String coachName,
        Instant startsAt,
        Instant endsAt,
        int capacity,
        int bookedCount,
        String status
) {
}
