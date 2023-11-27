create table entity_tree_node
(
    id          serial,
    root_id     integer,
    parent_id   integer,
    left_edge   integer     not null,
    right_edge  integer     not null,

    entity_type varchar(20) not null,
    entity_id   integer,

    primary key (id)
);

create table entity_tree_root
(
    id                   integer UNIQUE,
    owner_reference_type varchar(20) not null,
    owner_reference_id   integer     not null,

    primary key (id)
);


create index entity_tree_node_root_and_left_edge on entity_tree_node (root_id, left_edge);
create index entity_tree_node_root_and_right_edge on entity_tree_node (root_id, right_edge);

create index entity_tree_root_owner_reference on entity_tree_root (owner_reference_type, owner_reference_id);
