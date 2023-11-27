create table task_work_log
(
    id              serial,

    main_task_id    integer not null references main_task (id),
    sub_task_id     integer references sub_task (id),

    worker_id       integer not null references "user" (id),

    timestamp       timestamp not null,
    event           varchar(32) not null,
    comment         varchar(1024),
    latitude        varchar(255),
    longitude       varchar(255),

    modified_by_work_log_id integer references task_work_log (id),
    modify_to_worklog_id integer references task_work_log (id),

    primary key (id)
);

create table task_work_log_attachment (
    id serial,
    task_work_log_id integer not null references task_work_log(id),
    file_id integer not null references file(id)
);

