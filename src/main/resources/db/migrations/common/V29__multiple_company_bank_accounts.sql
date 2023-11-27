CREATE TABLE bank_account
(
    id               SERIAL,
    created_by       INTEGER      NOT NULL REFERENCES "user" (id),
    created          TIMESTAMP    NOT NULL,
    last_modified_by INTEGER      NOT NULL REFERENCES "user" (id),
    last_modified    TIMESTAMP    NOT NULL,
    disabled         TIMESTAMP,
    company_id       INTEGER      NOT NULL REFERENCES company (id),
    bank_name        VARCHAR(255),
    iban             VARCHAR(255) NOT NULL,
    bic              VARCHAR(255),
    beneficiary_name VARCHAR(255) NOT NULL,
    currency_id      VARCHAR(255) NOT NULL REFERENCES currency (id),
    primary key (id)
);

ALTER TABLE company
    DROP COLUMN bank_name,
    DROP COLUMN iban,
    DROP COLUMN bic,
    DROP COLUMN beneficiary_name,
    DROP COLUMN currency_id;
