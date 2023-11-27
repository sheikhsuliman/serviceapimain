ALTER TABLE contract_task
    ADD COLUMN negated_contract_task_id INTEGER REFERENCES contract_task(id),
    ADD COLUMN primary_contract_id INTEGER REFERENCES contract(id)
;

UPDATE contract_task SET primary_contract_id = contract_id WHERE primary_contract_id is null;