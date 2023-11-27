DELETE FROM catalog_item;

INSERT INTO catalog_item (id, parent_id, is_leaf, name, description, unit_id, is_material, disabled, version) VALUES
    ('100', null, false, 'Spenglerarbeiten', null, null, false, null, 0),
    ('100.100', '100', false, 'Flachdach', null, null, false, null, 0),
    ('100.100.100', '100.100', false, 'Vorarbeiten', null, null, false, null, 0),
    ('100.100.100.100', '100.100.100', false, 'Baustelleneinrichtungen und Transporte', null, null, false, null, 0),
    ('100.100.100.100.100', '100.100.100.100', true, 'Zu- und Abtransport', null, 3, false, null, 0),
    ('100.100.100.100.200', '100.100.100.100', true, 'Abdeck- und Reinigungsmassnahmen.', null, 3, false, null, 0),
    ('100.100.100.100.300', '100.100.100.100', true, 'Absperren/Schutzmassnahmen', null, 30, false, null, 0),
    ('100.100.100.100.400', '100.100.100.100', true, 'Elektroanschluss. Stromkosten bauseits.', null, 30, false, null, 0),
    ('100.100.100.200', '100.100.100', false, 'Verschiedenes', null, null, false, null, 0),
    ('100.100.100.200.100', '100.100.100.200', true, 'Pflanztröge, Mobiliar und dgl. Werden bauseits entfernt und wieder aufgestellt.', null, 30, false, null, 0),
    ('100.100.100.200.200', '100.100.100.200', true, 'Pflanztröge, Mobiliar und dgl. Entfernen und wieder aufstellen nach Aufwand.', null, 30, false, null, 0),
    ('100.100.200', '100.100', false, 'Dampfsperren', null, null, false, null, 0),
    ('100.100.200.300', '100.100.200', false, 'Voranstriche und Zwischenlagen', null, null, false, null, 0),
    ('100.100.200.300.100', '100.100.200.300', true, 'Auf horizontale und schwach geneigte Flächen', null, 3, false, null, 0),
    ('100.100.200.300.101', '100.100.200.300', true, 'Auf stark geneigte bzw. vertikale Flächen', null, 3, false, null, 0),
    ('100.100.300', '100.100', false, 'Dämmschichten', null, null, false, null, 0),
    ('100.100.300.400', '100.100.300', false, 'Trittschalldämmschichten', null, null, false, null, 0),
    ('100.100.300.400.100', '100.100.300.400', true, 'Trittschalldämmschichten verlegen', null, 3, false, null, 0);
