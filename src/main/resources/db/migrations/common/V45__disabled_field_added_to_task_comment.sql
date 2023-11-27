ALTER TABLE comment
    ADD COLUMN last_modified_by   integer references "user" (id),
    ADD COLUMN last_modified      timestamp,
    ADD COLUMN disabled timestamp
;

UPDATE comment SET last_modified_by = created_by, last_modified = created;

ALTER TABLE comment
    ALTER COLUMN last_modified_by SET not null ,
    ALTER COLUMN last_modified SET not null
;