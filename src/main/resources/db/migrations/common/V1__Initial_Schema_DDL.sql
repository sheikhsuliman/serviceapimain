create sequence hibernate_sequence start with 100 increment by 1;

create table language
(
  id   varchar(255) not null,
  name varchar(255) not null unique ,
  primary key (id)
);

create table gender
(
  id   integer      not null,
  name varchar(255) not null,
  primary key (id)
);

create table country
(
  id                 serial,
  code               varchar(15),
  name               varchar(255),
  type               varchar(15) check (type in ('COUNTRY', 'SUBDIVISION', 'MUNICIPALITY')),
  nationality        varchar(255),
  parent_id          integer references country(id),
  default_language   varchar(5) references language(id),
  path               varchar(255),
  primary key (id)
);

create table volume_units
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table currency
(
  id   varchar(255) not null,
  name varchar(255),
  primary key (id)
);

create table trade
(
  id         serial,
  name       varchar(255),
  selectable boolean,
  parent_id  integer references trade(id),
  primary key (id)
);

create table unit
(
  id              serial,
  is_density      boolean,
  fixed_price     boolean,
  is_force        boolean,
  frequently_used boolean,
  is_illuminance  boolean,
  length          boolean,
  name            varchar(255),
  order_index     integer,
  is_power        boolean,
  is_quantity     boolean,
  surface         boolean,
  symbol          varchar(255),
  is_time         boolean,
  volume          boolean,
  is_weight       boolean,
  base_unit       integer references unit(id),
  primary key (id)
);

create table surface_unit
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table file
(
  id               serial,
  created_by       integer,
  created          timestamp not null,
  last_modified_by integer,
  last_modified    timestamp not null,
  disabled         timestamp,
  filename         varchar(255),
  length           bigint,
  mime_type        varchar(255),
  url              varchar(255),
  url_medium       varchar(255),
  url_small        varchar(255),
  reference_id     integer,
  reference_type   varchar(20) not null,
  primary key (id)
);

create table "user"
(
  id                         serial,
  created_by_id              integer,
  created_date               timestamp,
  last_modified_by_id        integer,
  last_modified_date         timestamp,
  version                    bigint,
  credentials_updated_millis bigint       not null,
  email                      varchar(250) not null,
  new_email                  varchar(250),
  password                   varchar(255) not null,
  about_me                   varchar(4000),
  accept_sc_terms            boolean,
  birth_date                 date,
  confirmed_date             date,
  confirmed_user             varchar(255),
  disabled                   timestamp,
  given_name                 varchar(255),
  id_card_url                varchar(255),
  mobile                     varchar(255),
  name                       varchar(255),
  phone                      varchar(255),
  picture_id                 integer references file(id),
  receive_messages           boolean,
  sc_verified                date,
  ssn                        varchar(255),
  surname                    varchar(255),
  title                      varchar(255),
  user_pool                  varchar(255),
  user_name                  varchar(255),
  website_url                varchar(255),
  country_of_residence_id    integer references country(id),
  gender                     integer references gender(id),
  nationality_id             integer references country(id),
  pref_lang                  varchar(255) references language(id),
  primary key (id)
);

create table company_legal_type
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table company_size
(
  id          serial,
  label       varchar(255),
  max_workers integer,
  min_workers integer,
  primary key (id)
);

create table company_type
(
  id         serial,
  coco       boolean,
  name       varchar(255),
  selectable boolean,
  sort       integer,
  parent_id  integer references company_type(id),
  primary key (id)
);

create table company
(
  id                    serial,
  created_by            integer,
  created               timestamp not null,
  disabled              timestamp,
  last_modified_by      integer,
  last_modified         timestamp not null,
  address1              varchar(255),
  address2              varchar(255),
  city                  varchar(255),
  confirmed_date        date,
  company_email         varchar(255),
  fax                   varchar(255),
  fiscal_id             varchar(255),
  iban                  varchar(255),
  latitude              varchar(255),
  longitude             varchar(255),
  name                  varchar(255),
  phone                 varchar(255),
  picture_id            integer references file(id),
  plz                   varchar(255),
  register_scan_doc_url varchar(255),
  register_url          varchar(255),
  description           varchar(4000),
  vat                   varchar(255),
  web_url               varchar(255),
  confirmed_user        integer references "user"(id),
  country_id            integer references country(id),
  creation_user         integer references "user"(id),
  currency_id           varchar(255) references currency(id),
  company_legal_type_id integer references company_legal_type(id),
  company_size_id       integer references company_size(id),
  company_type_id       integer   not null references company_type(id),
  founded               varchar(15),
  certification         varchar(127),
  annual_turnover       varchar(63),
  apprentice_training   boolean,
  special_skills        varchar(2048),
  remarks               varchar(255),
  primary key (id)
);

create table role
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table permission
(
  id            serial,
  name          varchar(255),
  primary key (id)
);

create table role_permission
(
  id                serial,
  role_id           integer references role(id),
  permission_id     integer references permission(id),
  primary key (id)
);

create table project_status
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table project_type
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table project
(
  id               serial,
  created_by       integer,
  created          timestamp not null,
  last_modified_by integer,
  last_modified    timestamp not null,
  disabled         timestamp,
  cached_data      varchar(255),
  city             varchar(255),
  code             varchar(255),
  country_id       integer references country(id),
  description      varchar(1024),
  end_date         timestamp,
  internal_id      varchar(255),
  latitude         varchar(255),
  longitude        varchar(255),
  name             varchar(255),
  start_date       timestamp,
  street           varchar(255),
  website          varchar(255),
  default_image    integer references file(id),
  status_id        integer references project_status(id),
  type_id          integer references project_type(id),
  primary key (id)
);

create table catalog_item
(
    id          varchar(255), -- this is SNP
    parent_id   varchar(255) references catalog_item(id),
    is_leaf     boolean not null,
    name        varchar(1024) not null,
    description varchar(4000),
    unit_id     integer references unit (id),
    is_material boolean not null,
    disabled    timestamp,
    primary key (id)
);

create table company_catalog_item
(
    id                  serial,
    company_details     varchar(4000),
    unit_id             integer references unit (id),
    price               decimal(19, 2),
    company_id          integer not null references company(id),
    catalog_item_id     varchar(255) references catalog_item(id),
    disabled            timestamp,
    primary key (id)
);

create table company_catalog_item_material
(
    id                          serial,
    company_catalog_item_id     integer references company_catalog_item(id),
    material_catalog_item_id    varchar(255) references catalog_item(id),
    primary key (id)
);

create table company_trade
(
  id      serial,
  company integer not null references company(id),
  trade   integer not null references trade(id),
  primary key (id)
);

create table company_user_role
(
  id           serial,
  confirmed    timestamp,
  confirmed_by varchar(255),
  company      integer not null references company(id),
  role         integer not null references role(id),
  user_id      integer not null references "user"(id),
  primary key (id)
);

create table company_region (
  id      serial,
  company integer not null references company(id),
  country integer not null references country(id),
  primary key (id)
);

create table favorite
(
  id                serial,
  reference_id      integer not null,
  reference_type    varchar(20) not null,
  user_id           integer not null references "user"(id),
  primary key (id)
);

create table location_status
(
  id   serial,
  name varchar(255),
  primary key (id)
);

create table location_type
(
  id     serial,
  name   varchar(255),
  parent integer references location_type(id),
  primary key (id)
);

create table location_sub_type
(
  id        serial,
  name      varchar(255),
  main_type integer not null references location_type(id),
  primary key (id)
);

create table location
(
  id               serial,
  created_by       integer,
  created          timestamp not null,
  last_modified_by integer,
  last_modified    timestamp not null,
  disabled         timestamp,
  description      varchar(1024),
  end_date         timestamp,
  height           decimal(19, 2),
  length           decimal(19, 2),
  name             varchar(255),
  order_index      integer,
  starred          boolean,
  start_date       timestamp,
  surface          decimal(19, 2),
  volume           decimal(19, 2),
  width            decimal(19, 2),
  default_image    integer references file(id),
  default_plan     integer references file(id),
  parent           integer references location(id),
  project          integer   not null references project(id),
  status_id        integer references location_status(id),
  sub_type         integer references location_sub_type(id),
  surface_unit     integer references surface_unit(id),
  type_id          integer references location_type(id),
  unit             integer references unit(id),
  volume_unit      integer references volume_units(id),
  primary key (id)
);

create table location_trade
(
  id               serial,
  created_by       integer,
  created          timestamp not null,
  last_modified_by integer,
  last_modified    timestamp not null,
  disabled         timestamp,
  location         integer   not null references location(id),
  trade            integer   not null references trade(id),
  primary key (id)
);

create table location_tree
(
  id               serial,
  lft              integer not null,
  rgt              integer not null,
  parent_id        integer,
  location_id      integer not null references location(id),
  primary key (id)
);

create table project_company
(
  id                    serial,
  created_by            integer,
  created               timestamp not null,
  last_modified_by      integer,
  last_modified         timestamp not null,
  disabled              timestamp,
  company               integer   not null references company(id),
  project               integer   not null references project(id),
  primary key (id)
);

CREATE TABLE project_company_trade
(
  id                    serial,
  project_company       INTEGER NOT NULL,
  trade                 INTEGER NOT NULL,
  PRIMARY KEY (id)
);

create table project_user_role
(
  id                            serial,
  disabled                      timestamp,
  project                       integer not null references project(id),
  role                          integer not null references role(id),
  user_id                       integer not null references "user"(id),
  project_company               int not null default 1 references project_company(id),
  primary key (id)
);

create table specification
(
    id                          serial,
    variation                   varchar (4000),
    amount                      decimal(19, 2) not null,
    price                       decimal(19, 2),
    location_id                 integer not null references location(id),
    project_id                  integer not null references project(id),
    company_catalog_item_id     integer references company_catalog_item(id),
    company_id                  integer references company(id),
    disabled                    timestamp,
    primary key (id)
);

create table usr_role
(
  user_id integer not null references "user"(id),
  role    varchar(255)
);

CREATE TABLE _update (
  id serial,
  type VARCHAR(255) NOT NULL check(type in('COMMENT', 'TASK_PROBLEM_REPORT')) ,
  project_id INTEGER NOT NULL references project(id),
  content VARCHAR(2048) NOT NULL ,
  latitude VARCHAR(255) ,
  longitude VARCHAR(255) ,
  created TIMESTAMP NOT NULL ,
  created_by INTEGER NOT NULL ,
  last_modified TIMESTAMP NOT NULL ,
  last_modified_by INTEGER NOT NULL ,
  PRIMARY KEY (id)
);

CREATE TABLE update_reference (
  id serial,
  update_id INT NOT NULL references _update(id),
  foreign_id INT NOT NULL ,
  foreign_type VARCHAR(255) NOT NULL check (foreign_type in ('RECEIVER', 'PROJECT', 'TASK', 'ARTICLE', 'LOCATION', 'MEDIA_FILE', 'USER', 'COMPANY', 'GROUP', 'TRADE')),
  type VARCHAR (255) NOT NULL check (type in ('MAIN', 'LINK')) ,
  PRIMARY KEY (id)
);
