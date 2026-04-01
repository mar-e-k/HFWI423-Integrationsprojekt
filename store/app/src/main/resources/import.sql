/* Passwords are just the username in BCrypt Format*/
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'cashier', '$2a$12$39X.1hMFW.T9NYvItJR3oeE9GZ1u094iljB3GYUk7PhGG8VlKGlJO', 'system', 'cashier');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'admin', '$2a$12$ThwydvKWJohRuDSr/lbMUOkc63Xuge9glE4UlzZ4lwJgwhbP1zKdS', 'system', 'admin');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'system', '$2a$12$Qtif0DjRHEW/zR2RjC54CeF4B2S8PQVTLkAPxRzn8fygNWdXIYTOq', 'system', 'system');

INSERT INTO account_role (role) VALUES ('CASHIER');
INSERT INTO account_role (role) VALUES ('ADMIN');
INSERT INTO account_role (role) VALUES ('SYSTEM');

INSERT INTO account_role_assignment (account_id, role_id) VALUES (1, 1);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (2, 2);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (3, 3);

INSERT INTO store (city, country, street, street_number) VALUES ('Hannover', 'Deutschland', 'Hannoverische Straße', '1');
INSERT INTO store (city, country, street, street_number) VALUES ('Celle', 'Deutschland', 'Celler Straße', '2');
INSERT INTO store (city, country, street, street_number) VALUES ('Lachendorf', 'Deutschland', 'Lachendorfer Straße', '3');
INSERT INTO store (city, country, street, street_number) VALUES ('Wathlingen', 'Deutschland', 'Wathlingener Straße', '4');
INSERT INTO store (city, country, street, street_number) VALUES ('Großburgwedel', 'Deutschland', 'Großburgwedeler Straße', '5');

INSERT INTO register (store_id) VALUES (1);
INSERT INTO register (store_id) VALUES (1);
INSERT INTO register (store_id) VALUES (1);

INSERT INTO register (store_id) VALUES (2);
INSERT INTO register (store_id) VALUES (2);
INSERT INTO register (store_id) VALUES (2);

INSERT INTO register (store_id) VALUES (3);
INSERT INTO register (store_id) VALUES (3);
INSERT INTO register (store_id) VALUES (3);

INSERT INTO register (store_id) VALUES (4);
INSERT INTO register (store_id) VALUES (4);
INSERT INTO register (store_id) VALUES (4);

INSERT INTO register (store_id) VALUES (5);
INSERT INTO register (store_id) VALUES (5);
INSERT INTO register (store_id) VALUES (5);