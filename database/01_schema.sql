-- 1. CREACIÓN DE SECUENCIAS (PARA BATCH) --

CREATE SEQUENCE user_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE account_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE transaction_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE audit_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE beneficiary_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE login_attempt_seq START WITH 100 INCREMENT BY 50;

-- 2. TABLAS DE CATALOGO --

CREATE TABLE role (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE currency (
    id INT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE transaction_type (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE transaction_status (
    id INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE account_type (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 3. TABLAS PRINCIPALES --

CREATE TABLE "user" (
    id INT PRIMARY KEY,
    role_id INT NOT NULL REFERENCES role(id),
    name VARCHAR(100) NOT NULL,
    last_name1 VARCHAR(100) NOT NULL,
    last_name2 VARCHAR(100),
    document_id VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    state BOOLEAN DEFAULT TRUE, -- TRUE: Activo, FALSE: Bloqueado
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login_attempt (
    id INT PRIMARY KEY,
    user_id INT UNIQUE REFERENCES "user"(id),
    attempts INT DEFAULT 0,
    last_attempt TIMESTAMP,
    is_blocked BOOLEAN DEFAULT FALSE
);

CREATE TABLE audit_log (
    id INT PRIMARY KEY,
    user_id INT REFERENCES "user"(id),
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bank_account (
    id INT PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id),
    account_type_id INT NOT NULL REFERENCES account_type(id),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    currency_id INT NOT NULL REFERENCES currency(id),
    current_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    state BOOLEAN DEFAULT TRUE
);

CREATE TABLE beneficiary (
    id INT PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id),
    alias VARCHAR(100),
    account_number VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) DEFAULT 'BankDemo',
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE bank_transaction (
    id INT PRIMARY KEY,
    source_account_id INT REFERENCES bank_account(id),
    target_account_id INT REFERENCES bank_account(id),
    transaction_type_id INT NOT NULL REFERENCES transaction_type(id),
    amount DECIMAL(19,4) NOT NULL,
    currency_id INT NOT NULL REFERENCES currency(id),
    transaction_status_id INT NOT NULL REFERENCES transaction_status(id),
    reference_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);