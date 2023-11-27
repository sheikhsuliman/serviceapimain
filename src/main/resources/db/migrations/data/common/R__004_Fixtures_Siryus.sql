INSERT INTO "user" (id, created_by_id, created_date, last_modified_by_id, last_modified_date, version, credentials_updated_millis, email, new_email, password, about_me, accept_sc_terms, birth_date, confirmed_date, confirmed_user, disabled, given_name, id_card_url, gender, name, phone, picture_id, receive_messages, sc_verified, ssn, surname, mobile_country_code, mobile, title, user_name, user_pool, website_url, country_of_residence_id, nationality_id, pref_lang) VALUES
(0, 0, NOW(), 0, NOW(), 0, 0, 'david.henkel@dt-systems.net', NULL, pw('cocoTest6'), NULL, NULL, NULL, NULL, NULL, NULL, 'David', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'Henkel', null, NULL, NULL, NULL, NULL, NULL, 3, 3, 'de_DE')
    ON CONFLICT DO NOTHING;

INSERT INTO company (id, created_by, created, last_modified_by, last_modified, address1, address2, city, confirmed_date, disabled, company_email, fax, fiscal_id, name, phone, picture_id, plz, register_scan_doc_url, register_url, description, vat, web_url, confirmed_user, country_id, creation_user, company_legal_type_id, company_size_id, longitude, latitude) VALUES
(0, 0, NOW(), 0, NOW(), 'Alter Kirchweg 35', null, 'Pfeffingen', NOW(), null, 'contact@siryus.com', null, null, 'Siryus AG', null, null, '4148', null, 'https://scan_register_do', 'Siryus is a fast growing startup from Switzerland. Our vision is to build software that shapes the future. We are changing the way people think about work', null, 'www.siryus.com', null, 3, 0, 1, 3, null, null)
    ON CONFLICT DO NOTHING;

INSERT INTO company_user_role (id, confirmed, confirmed_by, user_id, company, role) VALUES
(0, null, null, 0, 0, 100)
    ON CONFLICT DO NOTHING;
