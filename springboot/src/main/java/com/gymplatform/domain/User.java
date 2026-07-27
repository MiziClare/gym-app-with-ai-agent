package com.gymplatform.domain;

import java.time.Instant;

public record User(
        Long id,
        String username,
        String passwordHash,
        String displayName,
        String email,
        Role role,
        boolean active,
        Instant createdAt
) {
}
