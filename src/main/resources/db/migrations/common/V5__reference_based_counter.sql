CREATE  TABLE reference_based_counter (
    id serial primary key,

    reference_type varchar(64) not null ,
    reference_id integer not null,
    counter_name varchar(64) not null,

    last_value integer not null default 0
);

CREATE INDEX reference_based_counter_reference_type_id_counter_name ON reference_based_counter(reference_type, reference_id, counter_name);