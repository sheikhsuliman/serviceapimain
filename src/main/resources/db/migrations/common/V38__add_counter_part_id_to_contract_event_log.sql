ALTER TABLE contract_event_log
    ADD COLUMN project_id INTEGER NOT NULL REFERENCES project(id),
    ADD COLUMN counter_part_id INTEGER REFERENCES company (id)
;