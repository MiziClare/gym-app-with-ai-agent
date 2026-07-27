package com.gymplatform.domain;

import java.time.Instant;

public record ActionProposal(
        String id,
        String type,
        String summary,
        Instant expiresAt
) {
}
