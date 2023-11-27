ALTER TABLE company_catalog_item
    DROP unit_id,
    DROP disabled,
    ADD last_modified_by    integer,
    ADD last_modified       timestamp,
    ADD check_list          varchar(4000)
;

ALTER TABLE company_catalog_item
    RENAME COLUMN company_details TO company_variation
;
