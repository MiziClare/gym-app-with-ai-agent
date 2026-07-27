package com.gymplatform.service;

import com.gymplatform.domain.Role;
import com.gymplatform.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffScanService {
    private final MembershipPassService membershipPassService;
    private final JdbcTemplate jdbc;

    public StaffScanService(MembershipPassService membershipPassService, JdbcTemplate jdbc) {
        this.membershipPassService = membershipPassService;
        this.jdbc = jdbc;
    }

    public StaffScanResult resolve(String token, User staff) {
        Long memberId = null;
        try {
            var member = membershipPassService.resolve(token);
            memberId = member.memberId();
            var scope = staff.role() == Role.ADMIN
                    ? "ADMIN"
                    : coachAccessScope(staff.id(), member.memberId());
            if (scope == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "This member is not assigned to this coach or attending a current session"
                );
            }
            audit(staff.id(), memberId, "APPROVED", scope, null);
            return new StaffScanResult(
                    member.memberId(),
                    member.memberNumber(),
                    member.displayName(),
                    member.planName(),
                    member.status(),
                    member.endsOn(),
                    member.active(),
                    scope
            );
        } catch (ResponseStatusException error) {
            audit(staff.id(), memberId, "DENIED", null, error.getReason());
            throw error;
        }
    }

    private String coachAccessScope(Long coachId, Long memberId) {
        var scopes = jdbc.queryForList("""
                SELECT CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM coach_member_assignments assignment
                        WHERE assignment.coach_id = ?
                          AND assignment.member_id = ?
                          AND assignment.status = 'ACTIVE'
                          AND assignment.starts_on <= CURRENT_DATE
                          AND (assignment.ends_on IS NULL OR assignment.ends_on >= CURRENT_DATE)
                    ) THEN 'ASSIGNED_STUDENT'
                    WHEN EXISTS (
                        SELECT 1
                        FROM bookings booking
                        JOIN course_sessions session ON session.id = booking.session_id
                        WHERE session.coach_id = ?
                          AND booking.member_id = ?
                          AND booking.status = 'CONFIRMED'
                          AND session.status = 'OPEN'
                          AND session.starts_at <= CURRENT_TIMESTAMP + INTERVAL 30 MINUTE
                          AND session.ends_at >= CURRENT_TIMESTAMP
                    ) OR EXISTS (
                        SELECT 1
                        FROM coach_appointments appointment
                        WHERE appointment.coach_id = ?
                          AND appointment.member_id = ?
                          AND appointment.status = 'CONFIRMED'
                          AND appointment.starts_at <= CURRENT_TIMESTAMP + INTERVAL 30 MINUTE
                          AND appointment.starts_at + INTERVAL 60 MINUTE >= CURRENT_TIMESTAMP
                    ) THEN 'CURRENT_SESSION'
                    ELSE NULL
                END
                """, String.class,
                coachId, memberId,
                coachId, memberId,
                coachId, memberId);
        return scopes.isEmpty() ? null : scopes.getFirst();
    }

    private void audit(
            Long staffId,
            Long memberId,
            String outcome,
            String scope,
            String reason
    ) {
        jdbc.update("""
                INSERT INTO staff_scan_audit
                    (staff_id, member_id, outcome, access_scope, reason)
                VALUES (?, ?, ?, ?, ?)
                """, staffId, memberId, outcome, scope, reason);
    }

    public record StaffScanResult(
            Long memberId,
            String memberNumber,
            String displayName,
            String planName,
            String status,
            java.time.LocalDate endsOn,
            boolean active,
            String accessScope
    ) {}
}
