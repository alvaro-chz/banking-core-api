-- Roles
INSERT INTO role (name) VALUES ('ADMIN'), ('CLIENT');

-- Monedas
INSERT INTO currency (code, name) VALUES ('PEN', 'Soles'), ('USD', 'Dólares');

-- Tipos de Cuenta
INSERT INTO account_type (name) VALUES ('AHORROS'), ('CORRIENTE');

-- Estados de Transacción
INSERT INTO transaction_status (name) VALUES ('PENDING'), ('SUCCESS'), ('FAILED');

-- Tipos de Transacción
INSERT INTO transaction_type (name) VALUES ('TRANSFERENCIA'), ('DEPOSITO'), ('RETIRO'), ('PAGO_SERVICIO');