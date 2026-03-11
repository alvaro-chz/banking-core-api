# 🏦 Cloud-Native Banking Core API

A RESTful API that simulates the operations of a core banking system. It implements a robust architecture focused on observability, stateless security (JWT), and containerized deployment.

This project manages the core logic of transactions, savings/checking accounts, beneficiaries, and a comprehensive audit log for every action performed.

---

## 🚀 Technologies & Architecture

* **Backend:** Java, Spring Boot 3, Spring Security (JWT Filters), Spring Data JPA.
* **Database:** PostgreSQL 16 (Relational, with automatic initialization scripts).
* **Integrations:** OpenExchangeRates API (for currency conversion).
* **Infrastructure & DevOps:** Docker, Docker Compose.
* **Observability (WIP, PLG Stack):** Prometheus (Metrics), Grafana (Dashboards), Loki (Log Aggregation).

---

## ⚙️ Database Structure
The persistence layer is fully modeled to comply with financial tracking regulations. It includes:
* **Core Entities:** `user`, `bank_account`, `bank_transaction`, `beneficiary`.
* **Security & Control:** `role` (ADMIN/CLIENT), `login_attempt` (brute-force prevention), `audit_log` (IP and action traceability).
* **Catalogs:** `currency` (USD, PEN, MXN), `account_type`, `transaction_type`, and `transaction_status`.

---

## 🛠️ Local Setup & Execution

The project is fully dockerized to ensure a reproducible environment without the need to install Java or PostgreSQL locally.

### 1. Clone the repository
```bash
git clone https://github.com/alvaro-chz/banking-core-api
cd banking-core-api
```

### 2. Configure Environment Variables
Create a file named .env in the root directory and add the following base configuration:

```env
# Database Configuration
DB_USER=admin
DB_PASSWORD=admin123
DB_NAME=bank_db

# Spring Security JWT
JWT_SECRET=e9912deb1f53a44779bad0feb4e8d0ba33d61bd75324c801a2087c43afef3eff

# External API (Exchange Rates)
EXCHANGE_API_URL=https://openexchangerates.org/api
EXCHANGE_API_KEY=[your_api_key_here]
```

### 3. Spin up the Infrastructure (Execution Options)
**Option A: Database Only (Recommended for Local Development)**

Spins up only the PostgreSQL container. Ideal if you want to run the Spring Boot application directly from your IDE (IntelliJ/VS Code) for debugging.
```bash
docker compose up -d bank_app_db
```
**Opción B: App + Database**

Spins up the database and builds the backend application image. Does not start the monitoring tools.
```bash
docker compose up -d bank_app_db app
```
**Opción C: Full Stack with Monitoring (High RAM Usage)**

Spins up the entire infrastructure, including the app, the database, and the PLG stack (Prometheus, Grafana, Loki).
```bash
docker compose up -d --build
```

---
## 🧪 How to test the API (Swagger UI)

The API features interactive documentation (OpenAPI/Swagger). Once the containers are running, follow these steps:

1.  **Access the interactive documentation:**
    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

2.  **Authentication:**
    Since the endpoints are protected via Spring Security, you must first register a user or log in using the public endpoint `/api/v1/auth/login`.

3.  **Get the Token:**
    Copy the `token` returned in the response body.

4.  **Authorize:**
    Click the green **"Authorize"** button at the top of the Swagger UI, paste your token, and click **Apply**.

You can now test the protected endpoints (`/transactions`, `/accounts`, etc.).

---
### 👥 Test Users
The database is initialized with ready-to-use accounts. All accounts share the same password: `password123`

| Rol | Correo / Usuario | Saldo Inicial | Notas                               |
| :--- | :--- |:--------------|:------------------------------------|
| **Admin** | `admin@banco.com` | N/A           | Access to administrative endpoints. |
| **Cliente 1** | `juan@correo.com` | 5,000 PEN     | Account: `123456789`                |
| **Cliente 2** | `maria@correo.com` | 1,000 PEN     | Account: `111222333`                |

---
## 📊 Observability & Monitoring (WIP)

The project includes real-time monitoring tools, accessible locally:

| Herramienta | Función | Acceso                                         | Credenciales |
| :--- | :--- |:-----------------------------------------------| :--- |
| **Grafana** | Visual Dashboards | [http://localhost:3000](http://localhost:3000) | `admin` / `admin` |
| **Prometheus** | Server Metrics | [http://localhost:9090](http://localhost:9090) | N/A |
| **Loki** | Centralized Logs | Port `3100` (internal)                         | N/A |

