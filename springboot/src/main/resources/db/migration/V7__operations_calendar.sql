CREATE TABLE gym_closed_days (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    closed_on DATE NOT NULL,
    reason VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_gym_closed_day UNIQUE (closed_on)
);

CREATE TABLE coach_availability (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coach_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL,
    starts_at TIME NOT NULL,
    ends_at TIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coach_availability_coach
        FOREIGN KEY (coach_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_coach_availability_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_coach_availability_time CHECK (ends_at > starts_at),
    INDEX idx_coach_availability_coach_day (coach_id, day_of_week)
);
