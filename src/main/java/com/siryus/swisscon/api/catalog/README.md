# New Catalog

## Structure

Global Catalog structure represented using `CatalogNodeEntity` class (table `global_catalog_node`). 

If more than one record with the same `snp` is present, only record with latest (biggest) `id` is taken 
into consideration while traversing the tree.

## Content

Global Catalog and Company Catalog content represented using `CatalogVariationEntity` class (table `catalog_variation`).

Global Catalog variations distinguished from Company Catalog variations by having `company_id` == 0.

Each variation consist of triplet of:
- task name
- task variation
- unit

Each variation can be active or inactive (this is mostly used in Company Catalog)

Each variation linked to **particular version** of `CatalogNodeEntity` (via `global_catalog_node_id` -> `id`)


## FlyWay Migration 

```sql
CREATE TABLE global_catalog_node
(
    id         serial,
    company_id integer   not null,

    created_by integer   not null references "user" (id),
    created    timestamp not null,

    snp        varchar(64),
    parent_snp varchar(64),

    name       varchar(1024),

    disabled   timestamp,

    primary key (id)
);

CREATE TABLE catalog_variation
(
    id                     serial,
    company_id             integer   not null,

    created_by             integer   not null references "user" (id),
    created                timestamp not null,

    snp                    varchar(64),
    global_catalog_node_id integer   not null references global_catalog_node (id),
    variation_number       integer   not null,

    active                 boolean,

    task_name              varchar(4000),
    task_variation         varchar(4000),
    check_list             varchar(4000),
    unit_id                integer references unit (id),
    price                  decimal(19, 2),

    primary key (id)
);
```
Source: [V14__new_catalog.sql](https://github.com/siryus-ag/swisscon-service-api/blob/lb/jira/SIR-1244/src/main/resources/db/migrations/common/V14__new_catalog.sql)

## Examples

#### 1. Typical Global Catalog

`global_catalog_node`
| id | company_id | snp | parent_snp | name |
| - | - | - | - | - |
| 1 | 0 | 100| | Flat Roof |
| 2 | 0 | 100.100 | 100 | Waterproofing membranes |
| 3 | 0 | 100.100.100 | 100.100 | Bitumen sheets |
| 4 | 0 | 100.100.100.100 | 100.100.100 | Undercoats |
| 5 | 0 | 100.100.100.100.100 | 100.100.100.100 | Primer Coat |


`catalog_variation`
| id | company_id | node id * |  v** | task_name | task_variation | unit_id |
| - |  - | - |  - |  - |  - | - | 
| 1 | 0 | 5 | 1 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3. | On horizontal and slightly inclined surfaces | 3 |
| 2 | 0 | 5 | 2 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3. | On vertical surfaces | 3 |

\* global_catalog_node_id

\*\* variation_number

#### 2. Renaming non-leaf node

`global_catalog_node`
| id | company_id | snp | parent_snp | name |
| - | - | - | - | - |
| 1 | 0 | 100| | Flat Roof |
| 2 | 0 | 100.100 | 100 | Waterproofing membranes |
| 3 | 0 | 100.100.100 | 100.100 | Bitumen sheets |
| 4 | 0 | 100.100.100.100 | 100.100.100 | Undercoats |
| 5 | 0 | 100.100.100.100.100 | 100.100.100.100 | Primer Coat |
| 6 | 0 | 100.100.100.100 | 100.100.100 | Under Coats |

#### 3. Adding New version of Variation

`global_catalog_node`
| id | company_id | snp | parent_snp | name |
| - | - | - | - | - |
| 1 | 0 | 100| | Flat Roof |
| 2 | 0 | 100.100 | 100 | Waterproofing membranes |
| 3 | 0 | 100.100.100 | 100.100 | Bitumen sheets |
| 4 | 0 | 100.100.100.100 | 100.100.100 | Undercoats |
| 5 | 0 | 100.100.100.100.100 | 100.100.100.100 | Primer Coat |
| 6 | 0 | 100.100.100.100.100 | 100.100.100.100 | Primer Coat |

`catalog_variation`
| id | company_id | node id * |  v** | task_name | task_variation | unit_id |
| - |  - | - |  - |  - |  - | - | 
| 3 | 0 | 6 | 1 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3. | On horizontal and slightly inclined surfaces | 3 |
| 4 | 0 | 6 | 2 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.1. | On vertical surfaces | 3 |

#### 4. Typical Company Catalog

`catalog_variation`
| id | company_id | active | node id * |  v** | task_name | task_variation | unit_id |
| - |  - | - |  - |  - |  - |  - | - | 
| 5 | 13 | 1 | 6 | 1 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3. | On horizontal and slightly inclined surfaces | 3 |
| 6 | 13 | 0 | 6 | 2 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.1. | On vertical surfaces | 3 |
| 7 | 13 | 1 | 6 | 3 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.2. | On vertical surfaces | 12 |

#### 5. Adding new version of Variation to Company Catalog

`catalog_variation`
| id | company_id | active | node id * |  v** | task_name | task_variation | unit_id |
| - |  - | - |  - |  - |  - |  - | - | 
| 5 | 13 | 1 | 6 | 1 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3. | On horizontal and slightly inclined surfaces | 3 |
| 6 | 13 | 0 | 6 | 2 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.1. | On vertical surfaces | 3 |
| 7 | 13 | 1 | 6 | 3 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.2. | On vertical surfaces | 12 |
| 8 | 13 | 1 | 6 | 2 | Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.11. | On vertical surfaces | 3 |

#### 6. Deactivating part of tree in Company Catalog


`global_catalog_node`
| id | company_id | snp | parent_snp | name | disabled |
| - | - | - | - | - | - |
| 1 | 0 | 100| | Flat Roof | null |
| 2 | 0 | 100.100 | 100 | Waterproofing membranes | null |
| 3 | 0 | 100.100.100 | 100.100 | Bitumen sheets | null |
| 4 | 0 | 100.100.100.100 | 100.100.100 | Undercoats | null |
| 5 | 0 | 100.100.100.100.100 | 100.100.100.100 | Primer Coat | null |
| 6 | 0 | 100.100.100.100.100 | 100.100.100.100 | Primer Coat | null |
| 7 | 13 | 100.100.100.100 | 100.100.100 | Undercoats | 6/6/2020 |
