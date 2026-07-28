ALTER TABLE gym_closed_days
    DROP INDEX uq_gym_closed_day,
    ADD COLUMN starts_at TIME NULL AFTER closed_on,
    ADD COLUMN ends_at TIME NULL AFTER starts_at,
    ADD CONSTRAINT chk_gym_closed_day_time
        CHECK (
            (starts_at IS NULL AND ends_at IS NULL)
            OR (starts_at IS NOT NULL AND ends_at IS NOT NULL AND ends_at > starts_at)
        ),
    ADD INDEX idx_gym_closed_day_date_time (closed_on, starts_at, ends_at);

CREATE TABLE gym_operation_hours (
    id TINYINT PRIMARY KEY,
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    CONSTRAINT chk_gym_operation_hours_id CHECK (id = 1),
    CONSTRAINT chk_gym_operation_hours_time CHECK (closes_at > opens_at)
);

INSERT INTO gym_operation_hours (id, opens_at, closes_at)
VALUES (1, '06:00:00', '22:00:00');
