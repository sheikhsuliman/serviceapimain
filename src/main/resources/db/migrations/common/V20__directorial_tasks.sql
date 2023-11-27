ALTER TABLE main_task
    ALTER COLUMN specification_id DROP NOT NULL ,
    ADD COLUMN materials_and_machines VARCHAR(1024)
;

CREATE TABLE main_task_attachment (
    id serial,
    main_task integer not null references main_task(id),
    file_id integer not null references file(id)
);
