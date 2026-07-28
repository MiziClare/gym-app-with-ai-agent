package com.gymplatform.domain;

import java.time.Instant;

public record BookingView(
        Long id,
        Long sessionId,
        String courseName,
        String coachName,
        Instant startsAt,
        Instant endsAt,
        String status,
        Instant createdAt,
        Long spaceId,
        String floorName,
        String spaceName
) {
}
