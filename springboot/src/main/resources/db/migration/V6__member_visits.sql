CREATE TABLE member_visits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    checked_in_by BIGINT NOT NULL,
    checked_out_by BIGINT NULL,
    checked_in_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checked_out_at TIMESTAMP NULL,
    CONSTRAINT fk_member_visits_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT fk_member_visits_check_in_staff
        FOREIGN KEY (checked_in_by) REFERENCES users(id),
    CONSTRAINT fk_member_visits_check_out_staff
        FOREIGN KEY (checked_out_by) REFERENCES users(id),
    CONSTRAINT chk_member_visit_time
        CHECK (checked_out_at IS NULL OR checked_out_at >= checked_in_at),
    INDEX idx_member_visits_member_time (member_id, checked_in_at),
    INDEX idx_member_visits_open (member_id, checked_out_at)
);
