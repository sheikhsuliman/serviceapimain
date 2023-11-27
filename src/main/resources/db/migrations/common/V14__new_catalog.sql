CREATE TABLE global_catalog_node
(
    id         serial,
    company_id integer   not null,

    created_by integer   not null references "user" (id),
    created    timestamp not null,

    snp        varchar(64),
    parent_snp varchar(64),

    name       varchar(1024),

    disabled   timestamp,

    primary key (id)
);

CREATE INDEX global_catalog_node_parent_snp_id ON global_catalog_node (parent_snp, id);
CREATE INDEX global_catalog_node_snp_id ON global_catalog_node (snp, id);

CREATE TABLE catalog_variation
(
    id                     serial,
    company_id             integer   not null,

    created_by             integer   not null references "user" (id),
    created                timestamp not null,

    snp                    varchar(64),
    global_catalog_node_id integer   not null references global_catalog_node (id),
    variation_number       integer   not null,

    active                 boolean,

    task_name              varchar(4000),
    task_variation         varchar(4000),
    check_list             varchar(4000),
    unit_id                integer references unit (id),
    price                  decimal(19, 2),

    primary key (id)
);
-- We will probably need to create some indexes... but it is not clear what kind.