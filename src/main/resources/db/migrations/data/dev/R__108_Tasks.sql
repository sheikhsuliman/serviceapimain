-- company_catalog_item
DELETE FROM company_catalog_item;
INSERT INTO company_catalog_item
    (id, version, company_variation, price, company_id, catalog_item_id, catalog_item_version)
VALUES
    (1, 0, 'First put base paint layer on', 8.50, 1, '100.100.100.100', 0);

-- specification
DELETE FROM specification;
INSERT INTO specification
    (id, variation, amount, price, location_id, project_id, company_variation_id, company_id, disabled)
VALUES
    (1, '42 cm wide', 2.00, 8.00, 2, 1, 1, 1, null);

-- main_task
DELETE FROM main_task;
INSERT INTO main_task
(id, created_by, created, last_modified_by, last_modified, disabled, task_type, task_number, location_id, specification_id, project_id, global_catalog_node_id, company_id, title, description, status, start_date, due_date, time_budget_min)
VALUES
(1, 2, '2019-12-26 14:30:18.141716', 2, '2019-12-26 14:30:18.141716', null, 'CONTRACTUAL', 1, 2, 1, 1, 6, 0, 'Planed Install Sliding Door', 'Install Sliding Door for Walk In Clothed', 'ASSIGNED', '2020-01-01 12:00:00.000000', '2020-02-01 12:00:00.000000', 120),
(2, 2, '2019-12-26 14:30:18.141716', 2, '2019-12-26 14:30:18.141716', null, 'CONTRACTUAL', 2, 2, 1, 1, 6, 0, 'In Progress Install Sliding Door', 'Install Sliding Door for Walk In Clothed', 'IN_PROGRESS', '2020-01-01 12:00:00.000000', '2020-02-01 12:00:00.000000', 120),
(3, 2, '2019-12-26 14:30:18.141716', 2, '2019-12-26 14:30:18.141716', null, 'CONTRACTUAL', 3, 2, 1, 1, 6, 0, 'Paused Install Sliding Door', 'Install Sliding Door for Walk In Clothed', 'PAUSED', '2020-01-01 12:00:00.000000', '2020-02-01 12:00:00.000000', 120),
(4, 2, '2019-12-26 14:30:18.141716', 2, '2019-12-26 14:30:18.141716', null, 'CONTRACTUAL', 4, 2, 1, 1, 6, 0, 'In Review Install Sliding Door', 'Install Sliding Door for Walk In Clothed', 'IN_REVIEW', '2020-01-01 12:00:00.000000', '2020-02-01 12:00:00.000000', 120),
(5, 2, '2019-12-26 14:30:18.141716', 2, '2019-12-26 14:30:18.141716', null, 'CONTRACTUAL', 6, 2, 1, 1, 6, 0, 'Completed Install Sliding Door', 'Install Sliding Door for Walk In Clothed', 'COMPLETED', '2020-01-01 12:00:00.000000', '2020-02-01 12:00:00.000000', 120)
;

DELETE FROM reference_based_counter;
INSERT INTO reference_based_counter (reference_type, reference_id, counter_name, last_value)
    VALUES
           ('PROJECT', 1, 'TASK-COUNTER', 6);

-- sub_task
DELETE FROM sub_task;
INSERT INTO sub_task
    (id, created_by, created, last_modified_by, last_modified, disabled, main_task_id, sub_task_number, title, description, status)
VALUES
    (1, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 1, 0, null, null, 'ASSIGNED'),
    (2, 2, '2019-12-26 14:30:27.996037', 2, '2019-12-26 14:30:27.996037', null, 1, 1, 'Install Sliding Door', 'Install Sliding Door for Walk In Clothet', 'ASSIGNED'),
    (3, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 2, 0, null, null, 'ASSIGNED'),
    (4, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 3, 0, null, null, 'ASSIGNED'),
    (5, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 4, 0, null, null, 'ASSIGNED'),
    (6, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 5, 0, null, null, 'ASSIGNED')
;

DELETE FROM sub_task_user;
INSERT INTO sub_task_user
    (created_by, created, sub_task_id, user_id)
VALUES
    -- users for sub task 1 (which come from company 1 siryus)
    (2, '2019-12-26 14:30:18.141716', 1, 33),
    (2, '2019-12-26 14:30:18.141716', 1, 34),
    (2, '2019-12-26 14:30:18.141716', 1, 35),
    (2, '2019-12-26 14:30:18.141716', 1, 36),
    (2, '2019-12-26 14:30:18.141716', 1, 37),
-- users for sub task 2 (which come from company 1 siryus)
    (2, '2019-12-26 14:30:18.141716', 2, 38),
    (2, '2019-12-26 14:30:18.141716', 2, 39),
    (2, '2019-12-26 14:30:18.141716', 2, 40),
-- users for sub task 3 (which come from company 1 siryus)
    (2, '2019-12-26 14:30:18.141716', 3, 34),
-- users for sub task 4 (which come from company 1 siryus)
    (2, '2019-12-26 14:30:18.141716', 4, 33),
-- users for sub task 5 (which come from company 1 siryus)
    (2, '2019-12-26 14:30:18.141716', 5, 35),
-- users for sub task 6 (which come from company 1 siryus)
    (2, '2019-12-26 14:30:18.141716', 5, 38)
;

DELETE FROM task_work_log;
INSERT INTO task_work_log
    (main_task_id, sub_task_id, worker_id, timestamp, event, comment)
VALUES
    (2, 3, 34, '2019-12-26 14:30:18.152397', 'START_TIMER', 'start'),
    (2, 3, 34, '2019-12-26 14:35:18.152397', 'STOP_TIMER', 'start'),
    (2, 3, 34, '2019-12-26 14:40:18.152397', 'START_TIMER', 'start'),
    (2, 1, 33, '2019-12-26 14:30:18.152397', 'START_TIMER', 'start'),
    (2, 1, 33, '2019-12-26 14:35:18.152397', 'STOP_TIMER', 'start'),
    (2, 1, 34, '2019-12-26 14:30:18.152397', 'START_TIMER', 'start'),
    (2, 1, 34, '2019-12-26 14:35:18.152397', 'STOP_TIMER', 'start')
;
