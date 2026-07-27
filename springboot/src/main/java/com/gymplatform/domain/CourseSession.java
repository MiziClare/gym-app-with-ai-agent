package com.gymplatform.domain;

import java.time.Instant;

public record CourseSession(
        Long id,
        Long courseId,
        Long coachId,
        Instant startsAt,
        Instant endsAt,
        int capacity,
        String status
) {
}
