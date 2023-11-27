-- > 0 = to prevent deletion of master user with id 0
DELETE FROM "user" where id > 0;
INSERT INTO "user" (id, created_by_id, created_date, last_modified_by_id, last_modified_date, version, credentials_updated_millis, email, new_email, password, about_me, accept_sc_terms, birth_date, confirmed_date, confirmed_user, disabled, given_name, id_card_url, gender, name, phone, picture_id, receive_messages, sc_verified, ssn, surname, mobile_country_code, mobile, title, user_name, user_pool, website_url, country_of_residence_id, nationality_id, pref_lang) VALUES
(1, 1, NOW(), 1, NOW(), 0, 0, 'test@siryus.com', NULL, pw('test'), NULL, NULL, '1980-09-18', NULL, NULL, NULL, 'firstname', NULL, 1, NULL, NULL, 3, NULL, NULL, '756.5966.1145.03', 'lastname', 41, '799999999', NULL, NULL, NULL, NULL, 3, 3, 'de_CH'),
(2, 1, NOW(), 1, NOW(), 0, 0, 'test2@siryus.com', NULL, pw('test 2'), NULL, NULL, NULL, NULL, NULL, NULL, 'firstname 2', NULL, 1, NULL, NULL, 3, NULL, NULL, '756.2029.9897.26', 'lastname 2', 41, '799999998', NULL, NULL, NULL, NULL, 3, 3, 'de_CH'),
(3, 1, NOW(), 1, NOW(), 0, 0, 'test3@siryus.com', NULL, pw('test 3'), NULL, NULL, NULL, NULL, NULL, NULL, 'firstname 3', NULL, 1, NULL, NULL, 3, NULL, NULL, '756.2029.9897.24', 'lastname 3', 41, '799999997', NULL, NULL, NULL, NULL, 3, 3, 'de_CH'),
(4, 1, NOW(), 1, NOW(), 0, 0, 'test4@siryus.com', NULL, pw('test 4'), NULL, NULL, NULL, NULL, NULL, NULL, 'firstname 4', NULL, 1, NULL, NULL, 3, NULL, NULL, '756.2029.9897.22', 'lastname 4', 41, '799999997', NULL, NULL, NULL, NULL, 3, 3, 'de_CH');

