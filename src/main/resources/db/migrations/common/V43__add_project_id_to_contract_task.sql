ALTER TABLE contract_task
    ADD project_id INTEGER REFERENCES project (id);

UPDATE contract_task SET project_id = contract.project_id
FROM contract WHERE contract.id = contract_task.contract_id;

ALTER TABLE contract_task ALTER COLUMN project_id SET NOT NULL;
