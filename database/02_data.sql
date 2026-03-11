-- ROLES
INSERT INTO role (id, name) VALUES (1, 'ADMIN');
INSERT INTO role (id, name) VALUES (2, 'CLIENT');

-- MONEDAS
INSERT INTO currency (id, code, name) VALUES (1, 'USD', 'Dólares Americanos');
INSERT INTO currency (id, code, name) VALUES (2, 'PEN', 'Soles Peruanos');
INSERT INTO currency (id, code, name) VALUES (3, 'MXN', 'Pesos Mexicanos');

-- TIPOS DE CUENTA
INSERT INTO account_type (id, name) VALUES (1, 'AHORROS');
INSERT INTO account_type (id, name) VALUES (2, 'CORRIENTE');

-- ESTADOS DE TRANSACCION
INSERT INTO transaction_status (id, name) VALUES (1, 'PENDING');
INSERT INTO transaction_status (id, name) VALUES (2, 'SUCCESS');
INSERT INTO transaction_status (id, name) VALUES (3, 'FAILED');

-- TIPOS DE TRANSACCION
INSERT INTO transaction_type (id, name) VALUES (1, 'TRANSFERENCIA');
INSERT INTO transaction_type (id, name) VALUES (2, 'DEPOSITO');
INSERT INTO transaction_type (id, name) VALUES (3, 'RETIRO');
INSERT INTO transaction_type (id, name) VALUES (4, 'PAGO_SERVICIO');
INSERT INTO transaction_type (id, name) VALUES (5, 'PAGO_INTERESES');

-- ==========================================
-- USUARIOS DE PRUEBA
-- La contraseña para todos es: password123
-- ==========================================

-- 1. USUARIO ADMIN
INSERT INTO "user" (id, role_id, name, last_name1, last_name2, document_id, email, password, phone_number)
VALUES (
           1,
           1,
           'Admin', 'System', 'Superuser',
           '00000000',
           'admin@banco.com',
           '$2a$10$nAhwxRMddlbFStxm8UHS7.TJuCWyFZia3aKhLU8rmUgvBG97a8SYG',
           '999999999'
       );
INSERT INTO login_attempt (id, user_id) VALUES (1, 1);
INSERT INTO bank_account (id, user_id, account_type_id, account_number, currency_id, current_balance)
VALUES (1, 1, 2, '001-0000000001', 2, 10000.0000);

-- 2. CLIENTE 1
INSERT INTO "user" (id, role_id, name, last_name1, last_name2, document_id, email, password, phone_number)
VALUES (
           2,
           2,
           'Juan', 'Perez', 'Gomez',
           '11111111',
           'juan@correo.com',
           '$2a$10$nAhwxRMddlbFStxm8UHS7.TJuCWyFZia3aKhLU8rmUgvBG97a8SYG',
           '987654321'
       );
INSERT INTO login_attempt (id, user_id) VALUES (2, 2);
INSERT INTO bank_account (id, user_id, account_type_id, account_number, currency_id, current_balance)
VALUES (2, 2, 2, '123456789', 2, 5000.0000);

-- 3. CLIENTE 2
INSERT INTO "user" (id, role_id, name, last_name1, last_name2, document_id, email, password, phone_number)
VALUES (
           3,
           2,
           'Maria', 'Lopez', 'Diaz',
           '22222222',
           'maria@correo.com',
           '$2a$10$nAhwxRMddlbFStxm8UHS7.TJuCWyFZia3aKhLU8rmUgvBG97a8SYG',
           '912345678'
       );
INSERT INTO login_attempt (id, user_id) VALUES (3, 3);
INSERT INTO bank_account (id, user_id, account_type_id, account_number, currency_id, current_balance)
VALUES (3, 3, 2, '111222333', 2, 1000.0000);