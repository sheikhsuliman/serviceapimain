DELETE FROM catalog_variation;
DELETE FROM global_catalog_node;


INSERT INTO global_catalog_node ( id, created_by, created, company_id, snp, parent_snp, name )
VALUES
( 1, 1, NOW(), 0, '100', null, 'Flat roof')
     , ( 2, 1, NOW(), 0, '100.100', '100', 'Waterproofing membranes')
     , ( 3, 1, NOW(), 0, '100.100.100', '100.100', 'Bitumen sheets')
     , ( 4, 1, NOW(), 0, '100.100.100.100', '100.100.100', 'Undercoats')
     , ( 5, 1, NOW(), 0, '100.100.100.100.100', '100.100.100.100', '100.100.100.100.100') -- leaf node
     , ( 6, 1, NOW(), 0, '100.100.100.100.200', '100.100.100.100', '100.100.100.100.200')
     , ( 7, 1, NOW(), 0, '200', null, 'No roof')
;
INSERT INTO global_catalog_node ( id, created_by, created, company_id, snp, parent_snp, name, disabled )
VALUES
( 8, 1, NOW(), 1, '200', null, 'No roof', NOW())
;

INSERT INTO catalog_variation ( company_id, created_by, created, snp, global_catalog_node_id, variation_number, active, task_name, task_variation, unit_id)
VALUES
( 0, 1, NOW(), '100.100.100.100.100', 5, 1, true, 'Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3.', 'On horizontal and slightly inclined surfaces', 3 )
     , ( 0, 1, NOW(), '100.100.100.100.100', 5, 2, true, 'Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3.', 'On vertical surfaces', 3 )
     , ( 0, 1, NOW(), '100.100.100.100.200', 6, 1, true, 'Primer coat on wooden base or cement coating. Consume approx. kg/m2 0.5.', null, 3 )
;

INSERT INTO catalog_variation ( company_id, created_by, created, snp, global_catalog_node_id, variation_number, active, task_name, task_variation, unit_id, price)
VALUES
( 1, 1, NOW(), '100.100.100.100.100', 5, 1, true, 'Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3.', 'On horizontal and slightly inclined surfaces', 3, 3.62 )
     , ( 1, 1, NOW(), '100.100.100.100.100', 5, 2, false, 'Primer coat on concrete base or cement coating. Consume approx. kg/m2 0.3.', 'On vertical surfaces', 3, null )
     , ( 1, 1, NOW(), '100.100.100.100.200', 6, 2, true, 'Primer coat on wooden base or ice coating. Consume approx. kg/m2 0.13.', null, 3, 0.02 )
     , ( 1, 1, NOW(), '100.100.100.100.200', 6, 3, true, 'Primer coat on wooden base or brick coating. Consume approx. kg/m2 0.666.', null, 3, null )
;

-- Trades
INSERT INTO global_catalog_node ( id, created_by, created, company_id, snp, name, disabled )
VALUES
(101, 1, NOW(), 0, '101','PREPARATION', null),
(102, 1, NOW(), 0, '102','ARCHITECT', null),
(103, 1, NOW(), 0, '103','SURVEYOR', null),
(104, 1, NOW(), 0, '104','GEOLOGIST', null),
(105, 1, NOW(), 0, '105','ENGINEER', null),
(106, 1, NOW(), 0, '106','CIVIL_ENGINEER', null),
(107, 1, NOW(), 0, '107','ELECTRICAL_ENGINEER', null),
(108, 1, NOW(), 0, '108','SANITARY_ENGINEER', null),
(109, 1, NOW(), 0, '109','PLANNERS', null),
(110, 1, NOW(), 0, '110','BUILDING_PLANNERS', null),
(111, 1, NOW(), 0, '111','ELECTRICAL_PLANNERS', null),
(112, 1, NOW(), 0, '112','SANITARY_PLANNERS', null),
(113, 1, NOW(), 0, '113','VENTILATION_PLANNER', null),
(114, 1, NOW(), 0, '114','HEATING_PLANNER', null),
(115, 1, NOW(), 0, '115','STRUCTURAL_ENGINEERS', null),
(116, 1, NOW(), 0, '116','DRAUGHTSMAN', null),
(117, 1, NOW(), 0, '117','BUILDINGS', null),
(118, 1, NOW(), 0, '118','CLEARING_DEMOLITION', null),
(119, 1, NOW(), 0, '119','SECURITY_REPAIR', null),
(120, 1, NOW(), 0, '120','EXCAVATION', null),
(121, 1, NOW(), 0, '121','STRUCTURAL_WORK_1', null),
(122, 1, NOW(), 0, '122','CIVIL_ENGINEERING', null),
(123, 1, NOW(), 0, '123','SCAFFOLDING', null),
(124, 1, NOW(), 0, '124','SITE_EQUIPMENT', null),
(125, 1, NOW(), 0, '125','BUILDER', null),
(126, 1, NOW(), 0, '126','SEWER', null),
(127, 1, NOW(), 0, '127','CONCRETE_WORK', null),
(128, 1, NOW(), 0, '128','BRICKLAYERS', null),
(129, 1, NOW(), 0, '129','METAL_CONSTRUCTION', null),
(130, 1, NOW(), 0, '130','STEEL_CONSTRUCTION', null),
(131, 1, NOW(), 0, '131','CARPENTER', null),
(132, 1, NOW(), 0, '132','STONEMASON', null),
(133, 1, NOW(), 0, '133','NATURAL_STONE', null),
(134, 1, NOW(), 0, '134','ARTIFICIAL_STONE', null),
(135, 1, NOW(), 0, '135','STRUCTURAL_WORK_2', null),
(136, 1, NOW(), 0, '136','WINDOWS', null),
(137, 1, NOW(), 0, '137','EXTERIOR_DOORS', null),
(138, 1, NOW(), 0, '138','GATES', null),
(139, 1, NOW(), 0, '139','TINSMITHS', null),
(140, 1, NOW(), 0, '140','PHOTOVOLTAICS', null),
(141, 1, NOW(), 0, '141','LIGHTNING_PROTECTION', null),
(142, 1, NOW(), 0, '142','ROOFINGS', null),
(143, 1, NOW(), 0, '143','STEEP_ROOFS', null),
(144, 1, NOW(), 0, '144','FLAT_ROOFS', null),
(145, 1, NOW(), 0, '145','FACADE_ENGINEERING', null),
(146, 1, NOW(), 0, '146','SEALS_INSULATIONS', null),
(147, 1, NOW(), 0, '147','EXTERNAL_INSULATION', null),
(148, 1, NOW(), 0, '148','SUN_PROTECTION', null),
(149, 1, NOW(), 0, '149','ROLLER_SHUTTER', null),
(150, 1, NOW(), 0, '150','BLINDS', null),
(151, 1, NOW(), 0, '151','INSTALLATIONS', null),
(152, 1, NOW(), 0, '152','ELECTRICIANS', null),
(153, 1, NOW(), 0, '153','HIGH_VOLTAGE_CURRENT', null),
(154, 1, NOW(), 0, '154','LOW_VOLTAGE', null),
(155, 1, NOW(), 0, '155','TELEMATICS', null),
(156, 1, NOW(), 0, '156','HEATERS', null),
(157, 1, NOW(), 0, '157','VENTILATION_AIR_CONDITIONING', null),
(158, 1, NOW(), 0, '158','SANITATION', null),
(159, 1, NOW(), 0, '159','ELEVATORS', null),
(160, 1, NOW(), 0, '160','BUILDING_AUTOMATION', null),
(161, 1, NOW(), 0, '161','INTERIOR_COMPLETION', null),
(162, 1, NOW(), 0, '162','PLASTERER', null),
(163, 1, NOW(), 0, '163','JOINER', null),
(164, 1, NOW(), 0, '164','KITCHEN_CONSTRUCTION', null),
(165, 1, NOW(), 0, '165','LOCKSMITHS', null),
(166, 1, NOW(), 0, '166','FLOORER', null),
(167, 1, NOW(), 0, '167','PAVER', null),
(168, 1, NOW(), 0, '168','WALL_COVERINGS', null),
(169, 1, NOW(), 0, '169','CEILING_COVERINGS', null),
(170, 1, NOW(), 0, '170','POTTERY', null),
(171, 1, NOW(), 0, '171','CHIMNEY_CONSTRUCTORS', null),
(172, 1, NOW(), 0, '172','STOVE_BUILDER', null),
(173, 1, NOW(), 0, '173','PAINTERS', null),
(174, 1, NOW(), 0, '174','COMPLETION', null),
(175, 1, NOW(), 0, '175','FIRE_PROTECTION', null),
(176, 1, NOW(), 0, '176','DRYING', null),
(177, 1, NOW(), 0, '177','CLEANING', null),
(178, 1, NOW(), 0, '178','GARDENER', null),
(179, 1, NOW(), 0, '179','INTERIOR_DOORS', null),
(180, 1, NOW(), 0, '180','INTERIOR_ARCHITECT', null),
(181, 1, NOW(), 0, '181','ACOUSTICIAN', null),
(182, 1, NOW(), 0, '182','LETTERING', null),
(183, 1, NOW(), 0, '183','FACADE_PLANNERS', null),
(184, 1, NOW(), 0, '184','GENERAL_PLANNER', null),
(185, 1, NOW(), 0, '185','GENERAL_CONTRACTOR', null),
(186, 1, NOW(), 0, '186','LIGHTS_AND_LAMPS', null),
(187, 1, NOW(), 0, '187','ICA_ENGINEER', null),
(188, 1, NOW(), 0, '188','PREFABRICATED_BUILDING', null),
(189, 1, NOW(), 0, '189','LOCKING_SYSTEMS', null),
(190, 1, NOW(), 0, '190','SPRINKLER_SYSTEMS', null),
(191, 1, NOW(), 0, '191','HVAC_ENGINEER', null),
(192, 1, NOW(), 0, '192','METALWORK_PLANNERS', null),
(193, 1, NOW(), 0, '193','IT_SOFTWARE', null)
;