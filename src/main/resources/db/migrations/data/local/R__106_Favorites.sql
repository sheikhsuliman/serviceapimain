DELETE FROM favorite;

INSERT INTO favorite (id, user_id, reference_type, reference_id) VALUES
(1, 1, 'PROJECT', 1),
(2, 1, 'COMPANY', 5),
(3, 1, 'USER', 2),
(4, 1, 'LOCATION', 3);
