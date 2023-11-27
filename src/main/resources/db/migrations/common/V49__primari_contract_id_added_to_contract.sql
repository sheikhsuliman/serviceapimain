ALTER TABLE contract
    ADD COLUMN primary_contract_id INT REFERENCES contract(id)
;