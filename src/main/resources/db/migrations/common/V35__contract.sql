CREATE TABLE contract
(
    id               SERIAL,

    project_id       INTEGER   NOT NULL REFERENCES project (id),
    contract_number  INTEGER   NOT NULL,

    created_by       INTEGER   NOT NULL REFERENCES "user" (id),
    created          TIMESTAMP NOT NULL,
    last_modified_by INTEGER REFERENCES "user" (id),
    last_modified    TIMESTAMP NOT NULL,
    disabled         TIMESTAMP,

    name             VARCHAR(1024) NOT NULL CHECK ( name <> '' ),
    description      VARCHAR(4000),

    contractor_id    INTEGER REFERENCES company (id),
    customer_id      INTEGER REFERENCES company (id),

    deadline         TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE TABLE contract_event_log
(
    id             SERIAL,
    contract_id    INTEGER   NOT NULL REFERENCES contract (id),

    created_by     INTEGER   NOT NULL REFERENCES "user" (id),
    created        TIMESTAMP NOT NULL,

    event          VARCHAR(256),
    contract_state VARCHAR(256),

    PRIMARY KEY (id)
);

CREATE TABLE contract_task
(
    id               SERIAL,
    contract_id      INTEGER   NOT NULL REFERENCES contract (id),

    created_by       INTEGER   NOT NULL REFERENCES "user" (id),
    created          TIMESTAMP NOT NULL,
    last_modified_by INTEGER REFERENCES "user" (id),
    last_modified    TIMESTAMP NOT NULL,
    disabled         TIMESTAMP,

    task_id          INTEGER   NOT NULL REFERENCES main_task (id),
    price            DECIMAL(19, 2),

    PRIMARY KEY (id)
);

CREATE TABLE contract_comment
(
    id               SERIAL,
    contract_id      INTEGER   NOT NULL REFERENCES contract (id),

    created_by       INTEGER   NOT NULL REFERENCES "user" (id),
    created          TIMESTAMP NOT NULL,
    disabled         TIMESTAMP,
    last_modified_by INTEGER REFERENCES "user" (id),
    last_modified    TIMESTAMP NOT NULL,

    text             VARCHAR(2048),
    attachment_id    INTEGER REFERENCES file (id)
);