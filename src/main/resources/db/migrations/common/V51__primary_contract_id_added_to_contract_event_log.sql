ALTER TABLE contract_event_log
    ADD COLUMN primary_contract_id INT REFERENCES contract(id)
;
ALTER TABLE contract_comment
    ADD COLUMN primary_contract_id INT REFERENCES contract(id)
;

UPDATE contract_event_log SET primary_contract_id = contract_id WHERE primary_contract_id is null;
UPDATE contract_comment SET primary_contract_id = contract_id WHERE primary_contract_id is null;
UPDATE contract_task SET primary_contract_id = contract_id WHERE primary_contract_id is null;
