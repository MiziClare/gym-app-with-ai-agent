CREATE TABLE coach_member_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coach_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_coach_assignments_coach
        FOREIGN KEY (coach_id) REFERENCES users(id),
    CONSTRAINT fk_coach_assignments_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT chk_coach_assignment_dates CHECK (ends_on IS NULL OR ends_on >= starts_on),
    CONSTRAINT chk_coach_assignment_status CHECK (status IN ('ACTIVE', 'ENDED')),
    INDEX idx_coach_assignments_coach_dates (coach_id, starts_on, ends_on),
    INDEX idx_coach_assignments_member (member_id, status)
);

CREATE TABLE staff_scan_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    member_id BIGINT NULL,
    outcome VARCHAR(16) NOT NULL,
    access_scope VARCHAR(32) NULL,
    reason VARCHAR(255) NULL,
    scanned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_scan_audit_staff
        FOREIGN KEY (staff_id) REFERENCES users(id),
    CONSTRAINT fk_scan_audit_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT chk_scan_audit_outcome CHECK (outcome IN ('APPROVED', 'DENIED')),
    INDEX idx_scan_audit_staff_time (staff_id, scanned_at),
    INDEX idx_scan_audit_member_time (member_id, scanned_at)
);
