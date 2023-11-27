CREATE TABLE task_link (
    id                 serial,

    created_by         integer   not null references "user" (id),
    created            timestamp not null,

    disabled_by        integer references "user" (id),
    disabled           timestamp,

    project_id         integer not null references project(id),

    link_type          varchar(64),

    src_task_id        integer not null references main_task(id),
    dst_task_id        integer not null references main_task(id),

    primary key (id)
);