ALTER TABLE  specification
    DROP COLUMN company_catalog_item_id,
    ADD COLUMN company_variation_id integer references catalog_variation(id)
;

ALTER TABLE main_task
    DROP COLUMN catalog_id,
    DROP COLUMN catalog_item_version,
    ADD COLUMN global_catalog_node_id integer references global_catalog_node(id),
    ADD COLUMN task_type VARCHAR(64)
;