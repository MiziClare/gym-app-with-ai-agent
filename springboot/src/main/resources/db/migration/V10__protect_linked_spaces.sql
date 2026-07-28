ALTER TABLE equipment
    DROP FOREIGN KEY fk_equipment_space;

ALTER TABLE equipment
    ADD CONSTRAINT fk_equipment_space
        FOREIGN KEY (space_id) REFERENCES gym_spaces(id);

ALTER TABLE course_sessions
    DROP FOREIGN KEY fk_course_sessions_space;

ALTER TABLE course_sessions
    ADD CONSTRAINT fk_course_sessions_space
        FOREIGN KEY (space_id) REFERENCES gym_spaces(id);
