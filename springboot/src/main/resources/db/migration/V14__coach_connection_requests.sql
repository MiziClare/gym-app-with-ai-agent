CREATE TABLE coach_connection_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    member_read_at TIMESTAMP NULL,
    CONSTRAINT fk_coach_connection_member
        FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_coach_connection_coach
        FOREIGN KEY (coach_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_coach_connection_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED')),
    INDEX idx_coach_connection_member (member_id, status, created_at),
    INDEX idx_coach_connection_coach (coach_id, status, created_at)
);
