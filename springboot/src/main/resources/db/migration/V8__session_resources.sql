ALTER TABLE equipment
    ADD COLUMN resource_type VARCHAR(16) NOT NULL DEFAULT 'EQUIPMENT',
    ADD CONSTRAINT chk_equipment_resource_type
        CHECK (resource_type IN ('EQUIPMENT', 'ROOM'));

ALTER TABLE course_sessions
    MODIFY coach_id BIGINT NULL;

CREATE TABLE course_session_resources (
    session_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    PRIMARY KEY (session_id, equipment_id),
    CONSTRAINT fk_session_resources_session
        FOREIGN KEY (session_id) REFERENCES course_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_resources_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    INDEX idx_session_resources_equipment (equipment_id)
);
