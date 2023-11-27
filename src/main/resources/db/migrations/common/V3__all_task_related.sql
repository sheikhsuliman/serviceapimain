create table phase
(
    id          serial,

    title       varchar(255),
    description varchar(1024),

    primary key (id)
);

create table main_task
(
    id                 serial,

    task_number        integer not null,

    created_by         integer   not null references "user" (id),
    created            timestamp not null,

    last_modified_by   integer   not null references "user" (id),
    last_modified      timestamp not null,

    disabled           timestamp,

    location_id        integer   not null references location (id),
    specification_id   integer   not null references specification (id),
    -- Following fields added to speed up relevant task searches
    project_id         integer   not null references project (id),
    catalog_id         varchar(255)   not null references catalog_item(id),
    company_id         integer   not null references company(id),

    title              varchar(255),
    description        varchar(1024),

    status             varchar(32) default 'BACKLOG',

    start_date         timestamp not null,
    due_date           timestamp not null,
    time_budget_min    integer,

    primary key (id)
);

create table sub_task
(
    id               serial,

    created_by       integer   not null references "user" (id),
    created          timestamp not null,

    last_modified_by integer   not null references "user" (id),
    last_modified    timestamp not null,

    disabled         timestamp,

    main_task_id     integer   not null references main_task (id),

    sub_task_number  integer,

    title            varchar(255),
    description      varchar(1024),

    status             varchar(32) default 'PLANNED',

    time_budget_min    integer,

    primary key (id)
);

create table sub_task_check_list
(
    id          serial,

    created_by  integer   not null references "user" (id),
    created     timestamp not null,

    checked_by  integer references "user" (id),
    checked     timestamp,

    sub_task_id integer   not null references sub_task (id),
    title       varchar(255),

    primary key (id)
);

create table sub_task_user
(
    id          serial,

    created_by  integer   not null references "user" (id),
    created     timestamp not null,

    sub_task_id integer   not null references sub_task (id),
    user_id     integer references "user" (id),
    -- role?
    -- trade?

    primary key (id)
);

create table comment
(
    id          serial,

    created_by  integer   not null references "user" (id),
    created     timestamp not null,

    sub_task_id integer   not null references sub_task (id),
    text        varchar(2048),

    attachment_id integer not null references file(id)
);