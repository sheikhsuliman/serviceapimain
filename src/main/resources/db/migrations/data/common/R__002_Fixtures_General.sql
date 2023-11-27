DELETE FROM currency;
INSERT INTO currency (id, name) VALUES
('CAD', 'CANADIAN_DOLLAR'),
('CHF', 'SWISS_FRANC'),
('CNY', 'YUAN'),
('EUR', 'EURO'),
('GBP', 'BRITISH_POUND'),
('JPY', 'JAPANESE_YEN'),
('USD', 'US_DOLLAR');

DELETE FROM company_legal_type;
INSERT INTO company_legal_type (id, name) VALUES (1, 'LTD'),
(2, 'LLC'),
(3, 'SOLE_ENTERPRISE'),
(4, 'LIMITED_PARTNERSHIP'),
(5, 'COOPERATIVE'),
(6, 'AG')
;

DELETE FROM company_size;
INSERT INTO company_size (id, label, max_workers, min_workers) VALUES
(1, 'JUST_YOU', 1, 1),
(2, '2_9', 9, 2),
(3, '10_99', 99, 10),
(4, '100_499', 499, 100),
(5, '500_AND_MORE', 10000, 500);

DELETE FROM gender;
INSERT INTO gender (id, name) VALUES
(1, 'MALE'),
(2, 'FEMALE'),
(3, 'UNDISCLOSED');

DELETE FROM title;
INSERT INTO title (id, name) VALUES
(1, 'MR'),
(2, 'MS'),
(3, 'MRS');

DELETE FROM language;
INSERT INTO language (id, name) VALUES
('de_CH', 'GERMAN_SWISS'),
('de_AT', 'GERMAN_AUSTRIAN'),
('fr_CH', 'FRENCH_SWISS'),
('de_DE', 'GERMAN'),
('en_US', 'ENGLISH'),
('es_ES', 'SPANISH'),
('zh_TW', 'CHINESE');

DELETE FROM location_type;
INSERT INTO location_type (id, name, parent) VALUES
(1, 'BUILDING', null),
(2, 'STORY_FLOOR', 1),
(3, 'UNIT', 2),
(4, 'ROOM', 3),
(5, 'SURFACE', 4),
(6, 'INSTALLATION', 5),
(7, 'OTHER', 6);

DELETE FROM location_sub_type;
INSERT INTO location_sub_type (id, name, main_type) VALUES
(1, 'RESIDENTIAL', 1),
(2, 'BUSINESS', 1),
(3, 'INDUSTRY', 1),
(4, 'PARKING', 1),
(5, 'BASEMENT', 2),
(6, 'GROUND_FLOOR', 2),
(7, 'ROOF', 2),
(8, 'APARTMENT', 3),
(9, 'OFFICE', 3),
(10, 'BATHROOM', 4),
(11, 'KITCHEN', 4),
(12, 'LIVING_ROOM', 4),
(13, 'BEDROOM', 4),
(14, 'WORKSPACE', 4),
(15, 'CEILING', 5),
(16, 'WALL', 5),
(17, 'FLOOR', 5);

DELETE FROM location_status;
INSERT INTO location_status (id, name) VALUES
(1, 'PLANNED'),
(2, 'RUNNING'),
(3, 'PAUSED'),
(4, 'BLOCKED'),
(5, 'COMPLETED');

DELETE FROM project_status;
INSERT INTO project_status (id, name) VALUES
(1, 'PRE_PROJECT'),
(2, 'BUILDING_PERMIT_PHASE'),
(3, 'PLANNING_PHASE'),
(4, 'STARTED'),
(5, 'BIDDING_PHASE'),
(6, 'CONSTRUCTION_PHASE'),
(7, 'APPROVED'),
(8, 'GUARANTEE_CLAIM');

DELETE FROM project_type;
INSERT INTO project_type (id, name) VALUES
(1, 'PRIVATES'),
(2, 'SINGLE_FAMILY_HOUSE'),
(3, 'APARTMENT_BUILDING'),
(4, 'RESIDENTIAL_CONSTRUCTION'),
(5, 'OLD_BUILDING'),
(6, 'NEW_BUILDING'),
(7, 'PUBLIC_CONSTRUCTION'),
(8, 'INDUSTRY'),
(9, 'OTHER');

DELETE FROM surface_unit;
INSERT INTO surface_unit (id, name) VALUES
(1, 'm2'),
(2, 'foot2');


DELETE FROM unit;
INSERT INTO unit (id, name, symbol, order_index, base_unit, frequently_used, length, surface, volume, is_force, is_power, is_time, is_weight, is_illuminance, is_density, is_quantity, fixed_price) VALUES
(1, 'Micrometers', 'µm', 1010, null, false, true, false, false, false, false, false, false, false, false, false, false),
(2, 'Meter', 'm', 1050, null, true, true, false, false, false, false, false, false, false, false, false, false),
(5, 'Centimetre', 'cm', 1030, null, true, true, false, false, false, false, false, false, false, false, false, false),
(8, 'Decimetre', 'dm', 1040, null, false, true, false, false, false, false, false, false, false, false, false, false),
(9, 'Square decimetre', 'dm2', 1140, null, false, false, true, false, false, false, false, false, false, false, false, false),
(10, 'Cubic decimetre', 'dm3', 1240, null, false, false, false, true, false, false, false, false, false, false, false, false),
(11, 'Millimeter', 'mm', 1020, null, true, true, false, false, false, false, false, false, false, false, false, false),
(14, 'Kilogram', 'kg', 2030, null, true, false, false, false, false, false, false, true, false, false, false, false),
(15, 'Newton', 'N', 5020, null, true, false, false, false, true, false, false, false, false, false, false, false),
(16, 'Kilonewton', 'kN', 5030, null, true, false, false, false, true, false, false, false, false, false, false, false),
(17, 'Meganewton', 'MN', 5040, null, false, false, false, false, true, false, false, false, false, false, false, false),
(18, 'Kilowatts', 'kW', 4030, null, true, false, false, false, false, true, false, false, false, false, false, false),
(19, 'Watt', 'W', 4020, null, true, false, false, false, false, true, false, false, false, false, false, false),
(20, 'Electrical output in kilowatts', 'kWel', 4060, null, false, false, false, false, false, true, false, false, false, false, false, false),
(21, 'Thermal output in kilowatts', 'kWth', 4070, null, false, false, false, false, false, true, false, false, false, false, false, false),
(22, 'Kilowatt peak', 'kWp', 4080, null, false, false, false, false, false, true, false, false, false, false, false, false),
(23, 'Metric ton', 't', 2040, null, true, false, false, false, false, false, false, true, false, false, false, false),
(24, 'Litres', 'l', 1240, null, true, false, false, true, false, false, false, false, false, false, false, false),
(25, 'Lux', 'lx', 6000, null, true, false, false, false, false, false, false, false, true, false, false, false),
(26, 'Pieces', 'St', 10, null, true, false, false, false, false, false, false, false, false, false, true, false),
(27, 'Hours', 'h', 3030, null, true, false, false, false, false, false, true, false, false, false, false, false),
(28, 'Minutes', 'min', 3020, null, true, false, false, false, false, false, true, false, false, false, false, false),
(29, 'Seconds', 's', 3010, null, true, false, false, false, false, false, true, false, false, false, false, false),
(30, 'Fixed price', 'FP', 20, null, true, false, false, false, false, false, false, false, false, false, false, true),
(31, 'Days', 'd', 3040, null, true, false, false, false, false, false, true, false, false, false, false, false),
(32, 'Proctor density', 'DPr', 7000, null, false, false, false, false, false, false, false, false, false, true, false, false),
(33, 'Linear metre', 'lm', 1051, null, false, true, false, false, false, false, false, false, false, false, false, false),
(34, 'Gram', 'g', 2020, null, false, false, false, false, false, false, false, true, false, false, false, false),
(3, 'Square metre', 'm2', 1150, 2, true, false, true, false, false, false, false, false, false, false, false, false),
(4, 'Cubic meters', 'm3', 1250, 2, true, false, false, true, false, false, false, false, false, false, false, false),
(6, 'Square centimetre', 'cm2', 1130, 5, true, false, true, false, false, false, false, false, false, false, false, false),
(7, 'Cubic centimetre', 'cm3', 1230, 5, true, false, false, true, false, false, false, false, false, false, false, false),
(12, 'Square millimetre', 'mm2', 1120, 11, true, false, true, false, false, false, false, false, false, false, false, false),
(13, 'Cubic millimetre', 'mm3', 1220, 11, true, false, false, true, false, false, false, false, false, false, false, false),
(35, 'Unit price', 'UP', 30, null, true, false, false, false, false, false, false, false, false, false, true, false),
(36, 'Percent', '%o', 8010, null, false, false, false, false, false, false, false, false, false, false, false, false),
(37, 'Percent', '%', 8020, null, false, false, false, false, false, false, false, false, false, false, false, false),
(38, 'Joules', 'J', 9010, null, false, false, false, false, false, false, false, false, false, false, false, false),
(39, 'Kilojoules', 'kJ', 9020, null, false, false, false, false, false, false, false, false, false, false, false, false),
(40, 'Hectolitres', 'hl', 1245, null, false, false, false, true, false, false, false, false, false, false, false, false),
(41, 'Weeks', 'Wo', 3050, null, false, false, false, false, false, false, true, false, false, false, false, false),
(42, 'Months', 'Mt', 3060, null, false, false, false, false, false, false, true, false, false, false, false, false),
(43, 'Pairs', 'Pr', 40, null, false, false, false, false, false, false, false, false, false, false, true, false),
(44, 'Bags', 'S', 50, null, false, false, false, false, false, false, false, false, false, false, true, false),
(45, 'Buckets', 'Kb', 60, null, false, false, false, false, false, false, false, false, false, false, true, false),
(46, 'Wheelbarrows', 'Kr', 70, null, false, false, false, false, false, false, false, false, false, false, true, false),
(47, 'Rolls', 'Ro', 80, null, false, false, false, false, false, false, false, false, false, false, true, false);


DELETE FROM volume_units;
INSERT INTO volume_units (id, name) VALUES (1, 'm3');
