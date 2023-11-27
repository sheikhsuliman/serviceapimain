-- > 0 = to prevent deletion of master user with id 0
DELETE FROM "user" where id > 0;
INSERT INTO "user" (id, created_by_id, created_date, last_modified_by_id, last_modified_date, version, credentials_updated_millis, email, new_email, password, about_me, accept_sc_terms, birth_date, confirmed_date, confirmed_user, disabled, given_name, id_card_url, gender, name, phone, picture_id, receive_messages, sc_verified, ssn, surname, mobile_country_code, mobile, title, user_name, user_pool, website_url, country_of_residence_id, nationality_id, pref_lang) VALUES
(1, 1, NOW(), 1, NOW(), 0, 0, 'test@siryus.com', NULL, pw('test'), NULL, NULL, '1980-09-18', NULL, NULL, NULL, 'firstname', NULL, 1, NULL, '+41 79 999 99 99', 3, NULL, NULL, '756.5966.1145.03', 'lastname', 41, '799999999', NULL, NULL, NULL, NULL, 3, 3, 'de_CH'),
(2, 1, NOW(), 1, NOW(), 0, 0, 'test2@siryus.com', NULL, pw('test 2'), NULL, NULL, NULL, NULL, NULL, NULL, 'firstname 2', NULL, 1, NULL, '+41 79 999 99 98', 3, NULL, NULL, '756.2029.9897.26', 'lastname 2', 41, '799999998', NULL, NULL, NULL, NULL, 3, 3, 'de_CH'),
(3, 1, NOW(), 1, NOW(), 0, 0, 'test3@siryus.com', NULL, pw('test 3'), NULL, NULL, NULL, NULL, NULL, NULL, 'firstname 3', NULL, 1, NULL, '+41 79 999 99 97', 3, NULL, NULL, '756.2029.9897.24', 'lastname 3', 41, '799999997', NULL, NULL, NULL, NULL, 3, 3, 'de_CH'),
(4, 1, NOW(), 1, NOW(), 0, 0, 'test4@siryus.com', NULL, pw('test 4'), NULL, NULL, NULL, NULL, NULL, NULL, 'firstname 4', NULL, 1, NULL, '+41 79 999 99 96', 3, NULL, NULL, '756.2029.9897.22', 'lastname 4', 41, '799999996', NULL, NULL, NULL, NULL, 3, 3, 'de_CH');

-- > 0 = to prevent deletion of master user role with id 0
DELETE FROM company_user_role where id > 0;
INSERT INTO company_user_role (id, confirmed, confirmed_by, user_id, company, role) VALUES
(1, null, null, 1, 1, 101),
(2, null, null, 2, 1, 103),
(3, null, null, 3, 2, 101),
(4, null, null, 4, 3, 101);

-- > 0 = to prevent deletion of master company with id 0
DELETE FROM company where id > 0;
INSERT INTO company (id, created_by, created, last_modified_by, last_modified, address1, address2, city, confirmed_date, disabled, company_email, fax, fiscal_id, name, phone, picture_id, plz, register_scan_doc_url, register_url, description, vat, web_url, confirmed_user, country_id, creation_user, company_legal_type_id, company_size_id, longitude, latitude) VALUES
(1, 1, NOW(), 1, NOW(), 'Teststrasse 1', 'Postfach Test', 'Test-City', NOW(), null, 'contact@siryus.com', '+41 79 999 99 98', null, 'Test Company', '+41 99 999 99 99', 4, '9999', null, 'https://scan_register_do', 'description 1', 'CHE-999.999.9999 MWST', 'www.test.com', 1, 3, 1, 1, null, null, null),
(2, 1, NOW(), 1, NOW(), 'Teststrasse 2', 'Postfach Test 2', 'Test-City 2', NOW(), null, 'contact2@siryus.com', '+41 79 999 99 96', null, 'Test Company 2', '+41 99 999 99 97', 4, '9998', null, 'https://scan_register_do', 'description 2', 'CHE-999.999.9998 MWST', 'www.test2.com', 1, 3, 1, 1, null, null, null),
(3, 1, NOW(), 1, NOW(), 'Teststrasse 3', 'Postfach Test 3', 'Test-City 3', NOW(), null, 'contact3@siryus.com', '+41 79 999 99 94', null, 'Test Company 3', '+41 99 999 99 95', 4, '9998', null, 'https://scan_register_do', 'description 3', 'CHE-999.999.9997 MWST', 'www.test3.com', 1, 3, 1, 1, null, null, null);

DELETE FROM company_trade;
INSERT INTO company_trade(id, trade, company) VALUES
(1,1,1),
(2,2,1),
(3,3,1),
(4,73,2),
(5,73,3)
;

DELETE FROM file;
INSERT INTO file (id, reference_type, reference_id, filename, created_by, created, last_modified_by, last_modified, disabled, length, mime_type, url, url_medium, url_small, system) VALUES
(1,'LOCATION',1,'testfile_location_1',1,'2019-05-03 15:18:22.349000000',1,'2019-05-03 15:18:22.349000000',null,2138663,'image/jpeg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/1/url','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/1/url_800x800','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/1/url_128x128', false),
(2,'PROJECT',1,'title-ettingen',1,'2019-05-03 15:18:22.349000000',1,'2019-05-03 15:18:22.349000000',null,2255599,'image/jpeg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/970/title.jpg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/970/title_800x800.png','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/970/title_128x128.png', false),
(3,'USER',1,'user_1_profile_picture',1,'2019-05-03 15:18:22.349000000',1,'2019-05-03 15:18:22.349000000',null,2255599,'image/jpeg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/973/profile_picture.jpg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/973/profile_picture_800x800.png','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/973/profile_picture_128x128.png', false),
(4,'COMPANY',1,'company_picture',1,'2019-05-03 15:18:22.349000000',1,'2019-05-03 15:18:22.349000000',null,2255599,'image/jpeg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/980/company_1.jpg','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/980/company_1_800x800.png','https://${aws_namecard_bucket}.s3.eu-central-1.amazonaws.com/File/980/company_1_128x128.png', false);

DELETE FROM project;
INSERT INTO project (id, created_by, created, last_modified_by, last_modified, cached_data, city, code, country_id, description, end_date, internal_id, name, start_date, street, website, default_image, status_id, type_id, longitude, latitude, disabled) VALUES
(1, 1, NOW(), 1, NOW(), null, 'city', null, 1, null, '2030-01-01', null, 'project name', '2000-01-01', 'street', null, 2, 1, 1, null, null, null),
(2, 1, NOW(), 1, NOW(), null, 'city', null, 1, null, '2030-01-01', null, 'test_project', '2000-01-01', 'street', null, 2, 1, 1, null, null, null);

DELETE FROM location;
INSERT INTO location (id, created_by, created, last_modified_by, last_modified, disabled, description, end_date, name, order_index, starred, start_date, surface, volume, default_image, default_plan, parent, project, status_id, sub_type, surface_unit, type_id, volume_unit, length, width, height, unit) VALUES
(1, 1, '2019-05-03 15:18:22.189000000', 1, '2019-05-03 15:18:22.189000000', null, null, null, 'test location', 1, false, null, null, null, null, null, null, 1, null, null, null, 1, null, null, null, null, null ),
(2, 1, '2019-05-03 15:18:22.189000000', 1, '2019-05-03 15:18:22.189000000', '2019-05-03 15:18:22.189000000', null, null, 'test location', 1, false, null, null, null, null, null, null, 1, null, null, null, 1, null, null, null, null, null ),
(3, 1, '2019-05-03 15:18:22.189000000', 1, '2019-05-03 15:18:22.189000000', null, null, null, 'test location', 1, false, null, null, null, null, null, null, 2, null, null, null, 1, null, null, null, null, null );

DELETE FROM entity_tree_root;
INSERT INTO entity_tree_root(id, owner_reference_type, owner_reference_id) VALUES
(1, 'PROJECT', 1),
(2, 'PROJECT', 2);

DELETE FROM entity_tree_node;
INSERT INTO entity_tree_node(id, root_id, parent_id, left_edge, right_edge, entity_type, entity_id) VALUES
(1,1,null, 1, 2, 'LOCATION', 1),
(2,2,null, 1, 2, 'LOCATION', 3);

DELETE FROM project_company;
INSERT INTO project_company (id, created_by, created, last_modified_by, last_modified, company, project) VALUES
(1, 1, NOW(), 1, NOW(), 1, 1),
(2, 1, NOW(), 1, NOW(), 2, 1),
(3, 1, NOW(), 1, NOW(), 3, 2);

DELETE FROM project_user_role;
INSERT INTO project_user_role (id, project, role, user_id) VALUES
(1, 1, 1, 1),
(2, 1, 4, 2),
(3, 1, 3, 3),
(4, 2, 1, 4);

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
INSERT INTO reference_based_counter
    (reference_type, reference_id, counter_name, last_value)
VALUES
    ('PROJECT', 1, 'TASK-COUNTER', 6);

-- sub_task
DELETE FROM sub_task;
INSERT INTO sub_task
(id, created_by, created, last_modified_by, last_modified, disabled, main_task_id, sub_task_number, title, description)
VALUES
(1, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 1, 0, null, null),
(2, 2, '2019-12-26 14:30:27.996037', 2, '2019-12-26 14:30:27.996037', null, 1, 1, 'Install Sliding Door', 'Install Sliding Door for Walk In Clothet'),
(3, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 2, 0, null, null),
(4, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 3, 0, null, null),
(5, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 4, 0, null, null),
(6, 2, '2019-12-26 14:30:18.152397', 2, '2019-12-26 14:30:18.152397', null, 5, 0, null, null)
;

DELETE FROM sub_task_user;
INSERT INTO sub_task_user
(id, created_by, created, sub_task_id, user_id)
VALUES
(1, 2, '2019-12-26 14:30:18.141716', 1, 32),
(2, 2, '2019-12-26 14:30:18.141716', 1, 31)
;

-- DELETE FROM catalog_item;
-- INSERT INTO catalog_item
-- (id,parent_id, is_leaf, name,description, is_material)
-- VALUES
-- ('120567', '12056', true, 'Painting', 'Put color on the wall', false),
-- ('120569', '12056', true, 'Wallpaper', 'Wallpaper as base for paint', false),
-- ('39280', '99345', true, 'Paint 241', 'Water resistant', true),
-- ('39281', '99345', true, 'Brush 21', 'Hard quast 2mm pins', true),
-- ('39282', '12056', true, 'Glue', 'Glue for Wallpaper', true),
-- ('99345', null, false, 'Painting Supplies', 'Things for painting', false),
-- ('90111', null, false, 'Cladding', 'Cladding work', false),
-- ('12056', '90111', false, 'Wall covering', 'Wall cladding work', false)
-- ;
