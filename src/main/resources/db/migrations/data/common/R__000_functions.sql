-- declare
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION pw( IN clearText text ) RETURNS text
AS $function$
BEGIN
    RETURN '{bcrypt}' || crypt(clearText, gen_salt('bf', 10));
END;
$function$
    LANGUAGE plpgsql;

