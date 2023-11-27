ALTER TABLE user_token
    DROP CONSTRAINT IF EXISTS user_token_token_type_check,
    ALTER COLUMN token_hash TYPE VARCHAR(64)
;
