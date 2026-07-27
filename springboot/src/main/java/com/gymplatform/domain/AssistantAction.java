package com.gymplatform.domain;

import java.time.Instant;

public record AssistantAction(
        String id,
        Long memberId,
        String actionType,
        Long sessionId,
        Long bookingId,
        String summary,
        String status,
        Instant expiresAt
) {
}
