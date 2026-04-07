/* Passwords are just the username in BCrypt Format*/
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'cashier.one', '$2a$12$39X.1hMFW.T9NYvItJR3oeE9GZ1u094iljB3GYUk7PhGG8VlKGlJO', 'system', 'cashier');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'cashier.two', '$2a$12$39X.1hMFW.T9NYvItJR3oeE9GZ1u094iljB3GYUk7PhGG8VlKGlJO', 'system', 'cashier');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'cashier.three', '$2a$12$39X.1hMFW.T9NYvItJR3oeE9GZ1u094iljB3GYUk7PhGG8VlKGlJO', 'system', 'cashier');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'admin.one', '$2a$12$ThwydvKWJohRuDSr/lbMUOkc63Xuge9glE4UlzZ4lwJgwhbP1zKdS', 'system', 'admin');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'admin.two', '$2a$12$ThwydvKWJohRuDSr/lbMUOkc63Xuge9glE4UlzZ4lwJgwhbP1zKdS', 'system', 'admin');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'admin.three', '$2a$12$ThwydvKWJohRuDSr/lbMUOkc63Xuge9glE4UlzZ4lwJgwhbP1zKdS', 'system', 'admin');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'system.one', '$2a$12$Qtif0DjRHEW/zR2RjC54CeF4B2S8PQVTLkAPxRzn8fygNWdXIYTOq', 'system', 'system');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'system.two', '$2a$12$Qtif0DjRHEW/zR2RjC54CeF4B2S8PQVTLkAPxRzn8fygNWdXIYTOq', 'system', 'system');
INSERT INTO account (uuid, username, password, first_name, last_name) VALUES (gen_random_uuid(), 'system.three', '$2a$12$Qtif0DjRHEW/zR2RjC54CeF4B2S8PQVTLkAPxRzn8fygNWdXIYTOq', 'system', 'system');

INSERT INTO account_role (role) VALUES ('CASHIER');
INSERT INTO account_role (role) VALUES ('ADMIN');
INSERT INTO account_role (role) VALUES ('SYSTEM');

INSERT INTO account_role_assignment (account_id, role_id) VALUES (1, 1);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (2, 1);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (3, 1);

INSERT INTO account_role_assignment (account_id, role_id) VALUES (4, 2);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (5, 2);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (6, 2);

INSERT INTO account_role_assignment (account_id, role_id) VALUES (7, 3);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (8, 3);
INSERT INTO account_role_assignment (account_id, role_id) VALUES (9, 3);