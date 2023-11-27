alter table company_catalog_item
    drop constraint company_catalog_item_catalog_item_id_fkey
;

alter table company_catalog_item_material
    drop constraint company_catalog_item_material_material_catalog_item_id_fkey
;

alter table main_task
    drop constraint main_task_catalog_id_fkey
;

alter table catalog_item
    drop constraint catalog_item_parent_id_fkey,
    drop constraint catalog_item_pkey
;

alter table catalog_item
    add version int default 0 not null
    , add constraint catalog_item_pk
        primary key (id, version)
;

alter table company_catalog_item
    add version int default 0 not null,
    add active boolean default true,
    add catalog_item_version int default 0 not null,
    add constraint company_catalog_item_catalog_item_id_fkey
        foreign key (catalog_item_id, catalog_item_version) references catalog_item (id, version)
;

alter table company_catalog_item_material
    add material_catalog_item_version int  default 0 not null,

    add constraint company_catalog_item_material_material_catalog_item_id_fkey
        foreign key (material_catalog_item_id, material_catalog_item_version) references catalog_item(id, version)
;

alter table main_task
    add catalog_item_version int default 0 not null,
    add constraint versioned_catalog_item foreign key (catalog_id, catalog_item_version) references catalog_item(id, version)
;