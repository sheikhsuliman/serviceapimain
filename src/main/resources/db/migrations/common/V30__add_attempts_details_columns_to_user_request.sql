ALTER TABLE user_request
    ADD COLUMN attempts INTEGER DEFAULT 0,
    ADD COLUMN details VARCHAR(100) DEFAULT '',
    ADD COLUMN type VARCHAR(50) DEFAULT '';