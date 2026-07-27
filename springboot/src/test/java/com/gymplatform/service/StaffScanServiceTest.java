package com.gymplatform.service;

import com.gymplatform.domain.Role;
import com.gymplatform.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StaffScanServiceTest {
    private final MembershipPassService passService = mock(MembershipPassService.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final StaffScanService service = new StaffScanService(passService, jdbc);
    private final MembershipPassService.ScanResult member = new MembershipPassService.ScanResult(
            4L, "GF-000004", "Member", "Unlimited", "ACTIVE", null, true, true
    );

    @Test
    void permitsAssignedCoachAndAuditsDeniedUnrelatedCoach() {
        var coach = new User(
                9L, "coach", "", "Coach", "coach@example.test",
                Role.COACH, true, Instant.now()
        );
        when(passService.resolve("assigned")).thenReturn(member);
        when(passService.resolve("unrelated")).thenReturn(member);
        when(jdbc.queryForList(
                anyString(), eq(String.class),
                eq(9L), eq(4L), eq(9L), eq(4L), eq(9L), eq(4L)
        )).thenReturn(List.of("ASSIGNED_STUDENT"), List.of());

        var allowed = service.resolve("assigned", coach);
        assertEquals("ASSIGNED_STUDENT", allowed.accessScope());

        var denied = assertThrows(
                ResponseStatusException.class,
                () -> service.resolve("unrelated", coach)
        );
        assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());
        verify(jdbc, times(2)).update(
                contains("INSERT INTO staff_scan_audit"),
                eq(9L), eq(4L), anyString(), nullable(String.class), nullable(String.class)
        );
    }

    @Test
    void checksInActiveMemberOnlyOnce() {
        var admin = new User(
                1L, "admin", "", "Admin", "admin@example.test",
                Role.ADMIN, true, Instant.now()
        );
        when(passService.resolve("valid")).thenReturn(member);
        when(jdbc.queryForObject(contains("FROM member_visits"), eq(Integer.class), eq(4L)))
                .thenReturn(0);

        service.checkIn("valid", admin);

        verify(jdbc).update(contains("INSERT INTO member_visits"), eq(4L), eq(1L));
    }
}
