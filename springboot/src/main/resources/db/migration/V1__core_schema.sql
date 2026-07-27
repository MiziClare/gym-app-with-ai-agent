CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(16) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('MEMBER', 'COACH', 'ADMIN'))
);

CREATE TABLE coach_profiles (
    user_id BIGINT PRIMARY KEY,
    bio VARCHAR(1000) NOT NULL,
    specialties VARCHAR(255) NOT NULL,
    CONSTRAINT fk_coach_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    duration_minutes SMALLINT UNSIGNED NOT NULL,
    default_capacity SMALLINT UNSIGNED NOT NULL,
    cover_key VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_course_duration CHECK (duration_minutes BETWEEN 10 AND 240),
    CONSTRAINT chk_course_capacity CHECK (default_capacity BETWEEN 1 AND 200)
);

CREATE TABLE course_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    capacity SMALLINT UNSIGNED NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_coach
        FOREIGN KEY (coach_id) REFERENCES users(id),
    CONSTRAINT chk_session_time CHECK (ends_at > starts_at),
    CONSTRAINT chk_session_capacity CHECK (capacity BETWEEN 1 AND 200),
    CONSTRAINT chk_session_status CHECK (status IN ('OPEN', 'CANCELLED', 'COMPLETED')),
    INDEX idx_sessions_starts_at (starts_at),
    INDEX idx_sessions_course (course_id),
    INDEX idx_sessions_coach (coach_id)
);

CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_session
        FOREIGN KEY (session_id) REFERENCES course_sessions(id),
    CONSTRAINT fk_bookings_member
        FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT uq_booking_session_member UNIQUE (session_id, member_id),
    CONSTRAINT chk_booking_status CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    INDEX idx_bookings_member (member_id),
    INDEX idx_bookings_session_status (session_id, status)
);
