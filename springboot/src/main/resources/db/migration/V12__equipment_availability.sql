ALTER TABLE equipment
    ADD COLUMN unit_label VARCHAR(32) NOT NULL DEFAULT 'units' AFTER description;

CREATE TABLE equipment_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    asset_code VARCHAR(64) NOT NULL,
    space_id BIGINT NULL,
    serial_number VARCHAR(120) NULL,
    purchased_on DATE NULL,
    base_status VARCHAR(24) NOT NULL DEFAULT 'AVAILABLE',
    notes VARCHAR(1000) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_units_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_equipment_units_space
        FOREIGN KEY (space_id) REFERENCES gym_spaces(id),
    CONSTRAINT uq_equipment_units_asset_code UNIQUE (asset_code),
    CONSTRAINT chk_equipment_units_status
        CHECK (base_status IN ('AVAILABLE', 'IN_USE', 'OUT_OF_SERVICE', 'RETIRED')),
    INDEX idx_equipment_units_equipment (equipment_id, base_status),
    INDEX idx_equipment_units_space (space_id)
);

INSERT INTO equipment_units (equipment_id, asset_code, space_id, base_status)
SELECT id, CONCAT('EQ-', LPAD(id, 4, '0'), '-001'), space_id,
       CASE WHEN status = 'MAINTENANCE' THEN 'OUT_OF_SERVICE' ELSE 'AVAILABLE' END
FROM equipment
WHERE status <> 'RETIRED' AND resource_type = 'EQUIPMENT';

UPDATE equipment SET status = 'AVAILABLE'
WHERE status = 'MAINTENANCE' AND resource_type = 'EQUIPMENT';

CREATE TABLE equipment_maintenance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    unit_id BIGINT NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    reason VARCHAR(160) NOT NULL,
    notes VARCHAR(1000) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_maintenance_unit
        FOREIGN KEY (unit_id) REFERENCES equipment_units(id),
    CONSTRAINT chk_equipment_maintenance_time CHECK (ends_at > starts_at),
    INDEX idx_equipment_maintenance_unit_time (unit_id, starts_at, ends_at)
);

ALTER TABLE course_session_resources
    ADD COLUMN required_units SMALLINT UNSIGNED NOT NULL DEFAULT 1;

DROP TABLE equipment_reservations;
