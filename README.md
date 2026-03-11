# 🏦 Cloud-Native Banking Core API

API RESTful que simula las operaciones de un sistema bancario. Implementa una arquitectura robusta orientada a la observabilidad, seguridad stateless (JWT), y despliegue contenerizado.

Este proyecto gestiona la lógica central de transacciones, cuentas de ahorro/corrientes, beneficiarios y un registro de auditoría completo para cada acción realizada.

---

## 🚀 Tecnologías y Arquitectura

* **Backend:** Java, Spring Boot 3, Spring Security (Filtros JWT), Spring Data JPA.
* **Base de Datos:** PostgreSQL 16 (Relacional, con scripts de inicialización automáticos).
* **Integraciones:** OpenExchangeRates API (para conversión de divisas).
* **Infraestructura & DevOps:** Docker, Docker Compose.
* **Observabilidad (WIP, Stack PLG):** Prometheus (Métricas), Grafana (Dashboards), Loki (Agregación de Logs).

---

## ⚙️ Estructura de la Base de Datos
La persistencia está completamente modelada para cumplir con normativas de seguimiento financiero. Incluye:
* **Entidades Core:** `user`, `bank_account`, `bank_transaction`, `beneficiary`.
* **Seguridad y Control:** `role` (ADMIN/CLIENT), `login_attempt` (prevención de fuerza bruta), `audit_log` (trazabilidad de IP y acciones).
* **Catálogos:** `currency` (USD, PEN, MXN), `transaction_type` y `transaction_status`.

---

## 🛠️ Instalación y Ejecución Local

El proyecto está dockerizado para garantizar un entorno reproducible sin necesidad de instalar Java o PostgreSQL localmente.

### 1. Clonar el repositorio
```bash
git clone https://github.com/alvaro-chz/banking-core-api
cd banking-core-api
```

### 2. Configurar Variables de Envorno
Crea un archivo llamado `.env` en la raíz del proyecto y añade la siguiente configuración base:

```env
# Database Configuration
DB_USER=admin
DB_PASSWORD=admin123
DB_NAME=bank_db

# Spring Security JWT (Ejemplo)
JWT_SECRET=e9912deb1f53a44779bad0feb4e8d0ba33d61bd75324c801a2087c43afef3eff

# External API (Exchange Rates)
EXCHANGE_API_URL=https://openexchangerates.org/api
EXCHANGE_API_KEY=[key_de_la_api]
```

### 3. Levantar la Infraestructura (Opciones de ejecución)

**Opción A: Solo Base de Datos (Recomendado para Desarrollo Local)**

Levanta únicamente el contenedor de PostgreSQL. Ideal si deseas ejecutar la aplicación de Spring Boot directamente desde tu IDE (IntelliJ/VS Code) para hacer depuración.
```bash
docker compose up -d bank_app_db
```
**Opción B: App + Base de Datos**

Levanta la base de datos y construye la imagen de la aplicación backend. No inicia las herramientas de monitoreo.
```bash
docker compose up -d bank_app_db app
```
**Opción C: Stack Completo con Monitoreo**

Levanta toda la infraestructura, incluyendo la aplicación, la base de datos y el stack PLG (Prometheus, Grafana, Loki). No se recomienda si quieres evitar un alto consumo de RAM.
```bash
docker compose up -d --build
```

---
## 🧪 Cómo probar la API (Swagger UI)

La API cuenta con documentación interactiva (**OpenAPI/Swagger**). Una vez que los contenedores estén corriendo, sigue estos pasos:

1.  **Ingresa a la documentación interactiva:**
    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

2.  **Autenticación:**
    Como las rutas están protegidas mediante **Spring Security**, primero debes registrar un usuario o iniciar sesión en el endpoint público `/api/v1/auth/login`.

3.  **Obtener Token:**
    Copia el `token` devuelto en la respuesta.

4.  **Autorizar:**
    Haz clic en el botón verde **"Authorize"** en la parte superior de Swagger, pega el token y haz clic en **Apply**.

Con ello ya puedes probar los endpoints protegidos (`/transactions`, `/accounts`, etc.).

---
### 👥 Usuarios de Prueba

La base de datos se inicializa con cuentas listas para usar. Todos comparten la contraseña: `password123`

| Rol | Correo / Usuario | Saldo Inicial | Notas                               |
| :--- | :--- |:--------------|:------------------------------------|
| **Admin** | `admin@banco.com` | N/A           | Acceso a endpoints administrativos. |
| **Cliente 1** | `juan@correo.com` | 5,000 PEN     | Cuenta: `123456789`                 |
| **Cliente 2** | `maria@correo.com` | 1,000 PEN     | Cuenta: `111222333`                 |

---
## 📊 Observabilidad y Monitoreo (WIP)

El proyecto incluye herramientas de monitoreo en tiempo real, accesibles localmente:

| Herramienta | Función | Acceso | Credenciales |
| :--- | :--- | :--- | :--- |
| **Grafana** | Dashboards Visuales | [http://localhost:3000](http://localhost:3000) | `admin` / `admin` |
| **Prometheus** | Métricas del Servidor | [http://localhost:9090](http://localhost:9090) | N/A |
| **Loki** | Logs Centralizados | Puerto `3100` (interno) | N/A |

