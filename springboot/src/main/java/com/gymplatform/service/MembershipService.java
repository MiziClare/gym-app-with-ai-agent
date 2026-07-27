package com.gymplatform.service;

import com.gymplatform.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class MembershipService {
    private final JdbcTemplate jdbc;

    public MembershipService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void createProfile(Long memberId) {
        jdbc.update("""
                INSERT INTO member_profiles (member_id, member_number, credential_id)
                VALUES (?, CONCAT('GF-', LPAD(?, 6, '0')), ?)
                """, memberId, memberId, UUID.randomUUID().toString());
    }

    public MembershipView getFor(User member) {
        var memberships = jdbc.query("""
                SELECT profile.member_number, plan.name AS plan_name,
                       membership.starts_on, membership.ends_on,
                       CASE
                           WHEN membership.id IS NULL THEN 'NONE'
                           WHEN membership.status <> 'ACTIVE' THEN membership.status
                           WHEN membership.starts_on > CURRENT_DATE THEN 'UPCOMING'
                           WHEN membership.ends_on IS NOT NULL
                                AND membership.ends_on < CURRENT_DATE THEN 'EXPIRED'
                           ELSE 'ACTIVE'
                       END AS effective_status,
                       plan.allow_entry, plan.allow_classes, plan.allow_equipment,
                       plan.allow_personal_training, plan.monthly_class_limit,
                       plan.monthly_equipment_limit, plan.monthly_personal_training_limit
                FROM member_profiles profile
                LEFT JOIN memberships membership ON membership.id = (
                    SELECT candidate.id
                    FROM memberships candidate
                    WHERE candidate.member_id = profile.member_id
                    ORDER BY candidate.starts_on DESC, candidate.id DESC
                    LIMIT 1
                )
                LEFT JOIN membership_plans plan ON plan.id = membership.plan_id
                WHERE profile.member_id = ?
                """, (result, row) -> {
            var status = result.getString("effective_status");
            return new MembershipView(
                    result.getString("member_number"),
                    member.displayName(),
                    member.email(),
                    member.active(),
                    member.active() && "ACTIVE".equals(status),
                    result.getString("plan_name"),
                    status,
                    result.getObject("starts_on", LocalDate.class),
                    result.getObject("ends_on", LocalDate.class),
                    result.getBoolean("allow_entry"),
                    result.getBoolean("allow_classes"),
                    result.getBoolean("allow_equipment"),
                    result.getBoolean("allow_personal_training"),
                    result.getObject("monthly_class_limit", Integer.class),
                    result.getObject("monthly_equipment_limit", Integer.class),
                    result.getObject("monthly_personal_training_limit", Integer.class)
            );
        }, member.id());

        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member profile not found");
        }
        return memberships.getFirst();
    }

    public record MembershipView(
            String memberNumber,
            String displayName,
            String email,
            boolean accountActive,
            boolean active,
            String planName,
            String status,
            LocalDate startsOn,
            LocalDate endsOn,
            boolean allowEntry,
            boolean allowClasses,
            boolean allowEquipment,
            boolean allowPersonalTraining,
            Integer monthlyClassLimit,
            Integer monthlyEquipmentLimit,
            Integer monthlyPersonalTrainingLimit
    ) {}
}
