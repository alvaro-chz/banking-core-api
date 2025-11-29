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
INSERT INTO transaction_type (id, name) VALUES (5, 'PAGO_INTERESES'); -- Vital para tu Batch