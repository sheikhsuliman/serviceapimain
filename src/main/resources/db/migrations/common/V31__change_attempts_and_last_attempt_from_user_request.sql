ALTER TABLE user_request
    DROP COLUMN attempts;

ALTER TABLE user_request
    RENAME COLUMN last_request TO requested_at;