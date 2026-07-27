CREATE TABLE assistant_actions (
    id CHAR(36) PRIMARY KEY,
    member_id BIGINT NOT NULL,
    action_type VARCHAR(16) NOT NULL,
    session_id BIGINT NULL,
    booking_id BIGINT NULL,
    summary VARCHAR(500) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMP NULL,
    CONSTRAINT fk_assistant_actions_member
        FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_assistant_actions_session
        FOREIGN KEY (session_id) REFERENCES course_sessions(id),
    CONSTRAINT fk_assistant_actions_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT chk_assistant_action_type CHECK (action_type IN ('BOOK', 'CANCEL')),
    CONSTRAINT chk_assistant_action_status CHECK (status IN ('PENDING', 'EXECUTED', 'EXPIRED')),
    INDEX idx_assistant_actions_member_status (member_id, status),
    INDEX idx_assistant_actions_expires (expires_at)
);
