package com.gymplatform.service;

import com.gymplatform.domain.Role;
import com.gymplatform.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return authorize(token, staff, true);
    }

    @Transactional
    public void checkIn(String token, User staff) {
        var member = authorize(token, staff, false);
        if (!member.active() || !member.allowEntry()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership does not allow gym entry");
        }
        lockMember(member.memberId());
        if (hasOpenVisit(member.memberId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member is already checked in");
        }
        jdbc.update("""
                INSERT INTO member_visits (member_id, checked_in_by)
                VALUES (?, ?)
                """, member.memberId(), staff.id());
    }

    @Transactional
    public void checkOut(String token, User staff) {
        var member = authorize(token, staff, false);
        lockMember(member.memberId());
        if (jdbc.update("""
                UPDATE member_visits
                SET checked_out_at = CURRENT_TIMESTAMP, checked_out_by = ?
                WHERE member_id = ? AND checked_out_at IS NULL
                """, staff.id(), member.memberId()) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member is not checked in");
        }
    }

    private StaffScanResult authorize(String token, User staff, boolean recordAudit) {
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
            if (recordAudit) {
                audit(staff.id(), memberId, "APPROVED", scope, null);
            }
            return new StaffScanResult(
                    member.memberId(),
                    member.memberNumber(),
                    member.displayName(),
                    member.planName(),
                    member.status(),
                    member.endsOn(),
                    member.active(),
                    member.allowEntry(),
                    scope,
                    hasOpenVisit(member.memberId())
            );
        } catch (ResponseStatusException error) {
            if (recordAudit) {
                audit(staff.id(), memberId, "DENIED", null, error.getReason());
            }
            throw error;
        }
    }

    private void lockMember(Long memberId) {
        jdbc.queryForObject(
                "SELECT member_id FROM member_profiles WHERE member_id = ? FOR UPDATE",
                Long.class,
                memberId
        );
    }

    private boolean hasOpenVisit(Long memberId) {
        var count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM member_visits
                WHERE member_id = ? AND checked_out_at IS NULL
                """, Integer.class, memberId);
        return count != null && count > 0;
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
            boolean allowEntry,
            String accessScope,
            boolean checkedIn
    ) {}
}
