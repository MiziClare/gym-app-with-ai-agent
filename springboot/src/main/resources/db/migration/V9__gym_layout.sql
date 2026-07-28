CREATE TABLE gym_floors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    sort_order SMALLINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_gym_floor_name UNIQUE (name),
    CONSTRAINT uq_gym_floor_order UNIQUE (sort_order)
);

CREATE TABLE gym_spaces (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    floor_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    type VARCHAR(8) NOT NULL,
    x_percent DECIMAL(5,2) NOT NULL,
    y_percent DECIMAL(5,2) NOT NULL,
    width_percent DECIMAL(5,2) NOT NULL,
    height_percent DECIMAL(5,2) NOT NULL,
    legacy_equipment_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gym_spaces_floor
        FOREIGN KEY (floor_id) REFERENCES gym_floors(id) ON DELETE CASCADE,
    CONSTRAINT uq_gym_space_name UNIQUE (floor_id, name),
    CONSTRAINT chk_gym_space_type CHECK (type IN ('ROOM', 'AREA')),
    CONSTRAINT chk_gym_space_geometry CHECK (
        x_percent >= 0 AND y_percent >= 0
        AND width_percent >= 3 AND height_percent >= 3
        AND x_percent + width_percent <= 100
        AND y_percent + height_percent <= 100
    ),
    INDEX idx_gym_spaces_floor (floor_id)
);

INSERT INTO gym_floors (name, sort_order) VALUES ('Floor 1', 0);

INSERT INTO gym_spaces
    (floor_id, name, type, x_percent, y_percent, width_percent, height_percent, legacy_equipment_id)
SELECT floor.id,
       CASE WHEN equipment.name_count > 1
            THEN CONCAT(LEFT(equipment.name, 65), ' #', equipment.id)
            ELSE equipment.name END,
       'ROOM',
       MOD((equipment.id - 1) * 12, 72),
       MOD(FLOOR((equipment.id - 1) / 6) * 16, 80),
       24, 14, equipment.id
FROM (
    SELECT item.*, COUNT(*) OVER (PARTITION BY item.name) AS name_count
    FROM equipment item
    WHERE item.resource_type = 'ROOM'
) equipment
JOIN gym_floors floor ON floor.sort_order = 0
;

ALTER TABLE equipment
    ADD COLUMN space_id BIGINT NULL,
    ADD CONSTRAINT fk_equipment_space
        FOREIGN KEY (space_id) REFERENCES gym_spaces(id) ON DELETE SET NULL,
    ADD INDEX idx_equipment_space (space_id);

ALTER TABLE course_sessions
    ADD COLUMN space_id BIGINT NULL,
    ADD CONSTRAINT fk_course_sessions_space
        FOREIGN KEY (space_id) REFERENCES gym_spaces(id) ON DELETE SET NULL,
    ADD INDEX idx_course_sessions_space_time (space_id, starts_at, ends_at);

UPDATE course_sessions session
JOIN (
    SELECT requirement.session_id, MIN(space.id) AS space_id
    FROM course_session_resources requirement
    JOIN gym_spaces space ON space.legacy_equipment_id = requirement.equipment_id
    GROUP BY requirement.session_id
) migrated ON migrated.session_id = session.id
SET session.space_id = migrated.space_id;

DELETE requirement
FROM course_session_resources requirement
JOIN equipment ON equipment.id = requirement.equipment_id
WHERE equipment.resource_type = 'ROOM';

UPDATE equipment SET status = 'RETIRED' WHERE resource_type = 'ROOM';

ALTER TABLE gym_spaces DROP COLUMN legacy_equipment_id;
