CREATE TABLE notices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
    cover_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_equipment_status CHECK (status IN ('AVAILABLE', 'MAINTENANCE', 'RETIRED'))
);

CREATE TABLE equipment_reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_reservation_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_equipment_reservation_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT chk_equipment_reservation_time CHECK (ends_at > starts_at),
    CONSTRAINT chk_equipment_reservation_status
        CHECK (status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED')),
    INDEX idx_equipment_reservation_member (member_id),
    INDEX idx_equipment_reservation_time (equipment_id, starts_at, ends_at)
);

CREATE TABLE coach_appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coach_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    note VARCHAR(500) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coach_appointment_coach
        FOREIGN KEY (coach_id) REFERENCES users(id),
    CONSTRAINT fk_coach_appointment_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT chk_coach_appointment_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    INDEX idx_coach_appointment_member (member_id),
    INDEX idx_coach_appointment_coach (coach_id, starts_at)
);

CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_posts_created_at (created_at)
);

CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    CONSTRAINT fk_chat_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_recipient
        FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_thread (sender_id, recipient_id, created_at),
    INDEX idx_chat_recipient_read (recipient_id, read_at)
);
