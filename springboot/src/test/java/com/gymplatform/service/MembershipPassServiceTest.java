package com.gymplatform.service;

import com.gymplatform.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MembershipPassServiceTest {
    private final MembershipPassService service = new MembershipPassService(
            mock(JdbcTemplate.class),
            mock(UserMapper.class),
            mock(MembershipService.class),
            "test-membership-pass-secret-with-more-than-32-characters",
            false
    );

    @Test
    void signsRejectsTamperingAndExpiresDynamicPasses() {
        var issuedAt = Instant.parse("2026-07-27T00:00:00Z");
        var pass = service.issue(
                new MembershipPassService.Credential(
                        "f3bd034f-d35f-44ab-b944-6be24661fd2e",
                        3
                ),
                issuedAt
        );

        var claims = service.verify(pass.token(), issuedAt.plusSeconds(89));
        assertEquals("f3bd034f-d35f-44ab-b944-6be24661fd2e", claims.credentialId());
        assertEquals(3, claims.credentialVersion());

        var tampered = pass.token().substring(0, pass.token().length() - 1)
                + (pass.token().endsWith("A") ? "B" : "A");
        var invalid = assertThrows(
                ResponseStatusException.class,
                () -> service.verify(tampered, issuedAt)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, invalid.getStatusCode());

        var expired = assertThrows(
                ResponseStatusException.class,
                () -> service.verify(pass.token(), issuedAt.plusSeconds(90))
        );
        assertEquals(HttpStatus.GONE, expired.getStatusCode());
    }
}
