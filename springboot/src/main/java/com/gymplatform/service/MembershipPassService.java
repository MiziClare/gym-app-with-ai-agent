package com.gymplatform.service;

import com.gymplatform.domain.User;
import com.gymplatform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class MembershipPassService {
    private static final long PASS_LIFETIME_SECONDS = 90;
    private static final long REFRESH_AFTER_SECONDS = 30;

    private final JdbcTemplate jdbc;
    private final UserMapper userMapper;
    private final MembershipService membershipService;
    private final byte[] secret;

    public MembershipPassService(
            JdbcTemplate jdbc,
            UserMapper userMapper,
            MembershipService membershipService,
            @Value("${app.membership-pass-secret:}") String configuredSecret,
            @Value("${app.demo-data}") boolean demoData
    ) {
        this.jdbc = jdbc;
        this.userMapper = userMapper;
        this.membershipService = membershipService;
        if (configuredSecret.isBlank() && !demoData) {
            throw new IllegalStateException("MEMBERSHIP_PASS_SECRET must contain at least 32 characters");
        }
        if (configuredSecret.isBlank()) {
            this.secret = new byte[32];
            new SecureRandom().nextBytes(this.secret);
        } else {
            this.secret = configuredSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (secret.length < 32) {
            throw new IllegalStateException("MEMBERSHIP_PASS_SECRET must contain at least 32 characters");
        }
    }

    public PassResponse issue(User member) {
        var credentials = jdbc.query("""
                SELECT credential_id, credential_version
                FROM member_profiles
                WHERE member_id = ?
                """, (result, row) -> new Credential(
                result.getString("credential_id"),
                result.getInt("credential_version")
        ), member.id());
        if (credentials.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member profile not found");
        }
        return issue(credentials.getFirst(), Instant.now());
    }

    public ScanResult resolve(String token) {
        var claims = verify(token, Instant.now());
        var memberIds = jdbc.queryForList("""
                SELECT member_id
                FROM member_profiles
                WHERE credential_id = ? AND credential_version = ?
                """, Long.class, claims.credentialId(), claims.credentialVersion());
        if (memberIds.isEmpty()) {
            throw invalidPass();
        }
        var member = userMapper.findById(memberIds.getFirst());
        if (member == null) {
            throw invalidPass();
        }
        var membership = membershipService.getFor(member);
        return new ScanResult(
                member.id(),
                membership.memberNumber(),
                membership.displayName(),
                membership.planName(),
                membership.status(),
                membership.endsOn(),
                membership.active(),
                membership.allowEntry()
        );
    }

    PassResponse issue(Credential credential, Instant issuedAt) {
        var expiresAt = issuedAt.plusSeconds(PASS_LIFETIME_SECONDS);
        var payload = "%s|%d|%d|%s".formatted(
                credential.id(),
                credential.version(),
                expiresAt.getEpochSecond(),
                UUID.randomUUID()
        );
        var encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return new PassResponse(
                encodedPayload + "." + encode(sign(encodedPayload)),
                expiresAt,
                issuedAt.plusSeconds(REFRESH_AFTER_SECONDS)
        );
    }

    PassClaims verify(String token, Instant now) {
        try {
            var parts = token.split("\\.", -1);
            if (parts.length != 2) {
                throw invalidPass();
            }
            var payload = Base64.getUrlDecoder().decode(parts[0]);
            var signature = Base64.getUrlDecoder().decode(parts[1]);
            if (!parts[0].equals(encode(payload))
                    || !parts[1].equals(encode(signature))
                    || !MessageDigest.isEqual(signature, sign(parts[0]))) {
                throw invalidPass();
            }
            var claims = new String(
                    payload,
                    StandardCharsets.UTF_8
            ).split("\\|", -1);
            if (claims.length != 4) {
                throw invalidPass();
            }
            UUID.fromString(claims[0]);
            UUID.fromString(claims[3]);
            var result = new PassClaims(
                    claims[0],
                    Integer.parseInt(claims[1]),
                    Instant.ofEpochSecond(Long.parseLong(claims[2]))
            );
            if (!result.expiresAt().isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.GONE, "Membership pass has expired");
            }
            return result;
        } catch (IllegalArgumentException error) {
            throw invalidPass();
        }
    }

    private byte[] sign(String payload) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            throw new IllegalStateException("Unable to sign membership pass", error);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private ResponseStatusException invalidPass() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid membership pass");
    }

    record Credential(String id, int version) {}

    record PassClaims(String credentialId, int credentialVersion, Instant expiresAt) {}

    public record PassResponse(String token, Instant expiresAt, Instant refreshAt) {}

    public record ScanResult(
            Long memberId,
            String memberNumber,
            String displayName,
            String planName,
            String status,
            java.time.LocalDate endsOn,
            boolean active,
            boolean allowEntry
    ) {}
}
