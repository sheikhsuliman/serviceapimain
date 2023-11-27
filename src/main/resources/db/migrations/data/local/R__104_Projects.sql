DELETE FROM project;
INSERT INTO project (id, created_by, created, last_modified_by, last_modified, cached_data, city, code, country_id, description, end_date, internal_id, name, start_date, street, website, default_image, status_id, type_id, longitude, latitude, disabled) VALUES
(1, 1, NOW(), 1, NOW(), null, 'Ettingen', null, 3, null, '2021-05-06', null, 'Sylvanerring Ettingen', '2019-01-01', 'Sylvanerring', null, 970, 6, 1, null, null, null),
(2, 1, NOW(), 1, NOW(), null, 'Zunzgen', null, 3, null, '2023-01-01', null, 'Zunzgen Mühle MFH', '2018-05-01', null, null, 971, 4, 3, null, null, null),
(3, 1, NOW(), 1, NOW(), null, 'Pfeffingen', '4148', 3, null, '2022-12-31', null, 'Fix my water heater', '2019-01-01', 'Alter Kirchweg 35', null, 972, 6, 1, null, null, null);

DELETE FROM project_company;
INSERT INTO project_company (id, company, project, created_by, created, last_modified_by, last_modified) VALUES
-- siryus is part of all projects
(1, 1, 1, 1, NOW(), 1, NOW()),
(2, 1, 2, 1, NOW(), 1, NOW()),
(3, 1, 3, 1, NOW(), 1, NOW()),
-- albin borer is part of all projects
(4, 2, 1, 1, NOW(), 1, NOW()),
(5, 2, 2, 1, NOW(), 1, NOW()),
(6, 2, 3, 1, NOW(), 1, NOW()),
-- rodoni is part of project 1
(7, 3, 1, 1, NOW(), 1, NOW()),
-- jakob gutknecht is part of project 1
(8, 4, 1, 1, NOW(), 1, NOW())
-- spenglerhaus is part of project 2
-- (9, 5, 2, 1, NOW(), 1, NOW())
;

DELETE FROM project_user_role;
INSERT INTO project_user_role (id, disabled, project, project_company, user_id, role) VALUES
-- project ettingen siryus
(1, null, 1, 1, 1, 1),
(2, null, 1, 1, 31, 2),
(3, null, 1, 1, 32, 3),
(4, null, 1, 1, 33, 4),
(5, null, 1, 1, 34, 4),
(6, null, 1, 1, 35, 4),
(7, null, 1, 1, 36, 4),
(8, null, 1, 1, 37, 4),
(9, null, 1, 1, 38, 4),
(10, null, 1, 1, 39, 4),
(11, null, 1, 1, 40, 4),
-- project ettingen albin borer
(12, null, 1, 4, 2, 3),
(13, null, 1, 4, 41, 3),
(14, null, 1, 4, 42, 4),
-- project ettingen rodoni
(15, null, 1, 7, 3, 3),
(16, null, 1, 7, 43, 3),
(17, null, 1, 7, 44, 4),
-- project ettingen jakob gutknecht
(18, null, 1, 8, 4, 3),
(19, null, 1, 8, 45, 3),
(20, null, 1, 8, 46, 4),
-- project zunzgen siryus
(21, null, 2, 2, 1, 1),
(22, null, 2, 2, 31, 2),
(23, null, 2, 2, 32, 3),
(24, null, 2, 2, 33, 4),
(25, null, 2, 2, 34, 4),
(26, null, 2, 2, 35, 4),
(27, null, 2, 2, 36, 4),
(28, null, 2, 2, 37, 4),
(29, null, 2, 2, 38, 4),
(30, null, 2, 2, 39, 4),
(31, null, 2, 2, 40, 4),
-- project zunzgen albin borer
(32, null, 2, 5, 2, 3),
(33, null, 2, 5, 41, 3),
(34, null, 2, 5, 42, 4),
-- project zunzgen spenglerhaus
-- (35, null, 2, 9, 5, 3),
-- (36, null, 2, 9, 47, 4),
-- (37, null, 2, 9, 48, 4),
-- project waterheater siryus
(38, null, 3, 3, 1, 1),
(39, null, 3, 3, 31, 2),
(40, null, 3, 3, 32, 3),
(41, null, 3, 3, 33, 4),
(42, null, 3, 3, 34, 4),
(43, null, 3, 3, 35, 4),
(44, null, 3, 3, 36, 4),
(45, null, 3, 3, 37, 4),
(46, null, 3, 3, 38, 4),
(47, null, 3, 3, 39, 4),
(48, null, 3, 3, 40, 4),
-- project waterheater albin borer
(49, null, 3, 6, 2, 3),
(50, null, 3, 6, 41, 3),
(51, null, 3, 6, 42, 4);
