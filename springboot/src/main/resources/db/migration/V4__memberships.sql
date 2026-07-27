CREATE TABLE membership_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    allow_entry BOOLEAN NOT NULL DEFAULT TRUE,
    allow_classes BOOLEAN NOT NULL DEFAULT TRUE,
    allow_equipment BOOLEAN NOT NULL DEFAULT TRUE,
    allow_personal_training BOOLEAN NOT NULL DEFAULT TRUE,
    monthly_class_limit SMALLINT UNSIGNED NULL,
    monthly_equipment_limit SMALLINT UNSIGNED NULL,
    monthly_personal_training_limit SMALLINT UNSIGNED NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE member_profiles (
    member_id BIGINT PRIMARY KEY,
    member_number VARCHAR(20) NOT NULL UNIQUE,
    credential_id CHAR(36) CHARACTER SET ascii NOT NULL UNIQUE,
    credential_version INT UNSIGNED NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_profiles_user
        FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE memberships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_memberships_member
        FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_memberships_plan
        FOREIGN KEY (plan_id) REFERENCES membership_plans(id),
    CONSTRAINT chk_membership_dates CHECK (ends_on IS NULL OR ends_on >= starts_on),
    CONSTRAINT chk_membership_status
        CHECK (status IN ('ACTIVE', 'FROZEN', 'CANCELLED', 'EXPIRED')),
    INDEX idx_memberships_member_dates (member_id, starts_on, ends_on),
    INDEX idx_memberships_status (status)
);

INSERT INTO membership_plans (name, description)
VALUES ('Legacy Unlimited', 'Unlimited access for members migrated from the modernized rebuild.');

INSERT INTO member_profiles (member_id, member_number, credential_id)
SELECT id, CONCAT('GF-', LPAD(id, 6, '0')), UUID()
FROM users
WHERE role = 'MEMBER';

INSERT INTO memberships (member_id, plan_id, starts_on, status)
SELECT u.id, p.id, DATE(u.created_at), 'ACTIVE'
FROM users u
JOIN membership_plans p ON p.name = 'Legacy Unlimited'
WHERE u.role = 'MEMBER' AND u.active = TRUE;
