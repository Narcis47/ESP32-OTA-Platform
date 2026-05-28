# 🔌 ESP32 OTA Platform

A web platform for remotely managing and reprogramming ESP32 microcontrollers over WiFi.
Register your ESP32, write code in a browser-based editor, and deploy it wirelessly — no USB cable needed.

> ⚠️ **Work in Progress** — actively under development.

---

## 💡 Concept

Most IoT platforms let you monitor data. This one lets you **reprogram your devices remotely**.

1. Register an account → receive a unique **API Token**
2. Flash the base firmware to your ESP32 (includes your token + WiFi credentials)
3. ESP32 connects, registers itself with hardware specs, and starts listening
4. Write code in the browser editor → deploy OTA to your board in seconds

---

## ✨ Features

- 🔐 JWT Authentication + Email Verification
- 🔑 Unique API Token per user (regenerable)
- 📡 Automatic board registration with full hardware specs
- 🖥️ Live Serial Monitor in the browser (like Arduino IDE)
- ✏️ Monaco-based code editor (same engine as VS Code)
- 🚀 OTA (Over-The-Air) code deployment via Arduino CLI
- ✅ Flash size validation before upload
- 📋 Program history per board
- 🛡️ Rate limiting, board fingerprinting, disposable email blocking
- 📵 Max 3 boards per user

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL |
| Database Mapper | Spring Data JDBC |
| Security | Spring Security + JWT + BCrypt |
| Rate Limiting | Bucket4j |
| Email | Spring Mail (Gmail SMTP) |
| Compiler | Arduino CLI (via ProcessBuilder) |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Frontend | HTML, CSS, JavaScript |
| Code Editor | Monaco Editor |
| Microcontroller | ESP32 |
| Firmware | Arduino IDE (C++) |
| Build Tool | Maven |

---

## 📁 Project Structure

```
ESP32-OTA-Platform/
├── src/main/java/com/narcis/esp32ota/
│   ├── controller/
│   │   ├── UserController.java       ← /api/users
│   │   ├── BoardController.java      ← /api/boards
│   │   ├── ProgramController.java    ← /api/programs
│   │   └── LogController.java        ← /api/log
│   ├── service/
│   │   ├── UserService.java
│   │   ├── BoardService.java
│   │   ├── ProgramService.java
│   │   └── LogService.java           ← in-memory logs
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── BoardRepository.java
│   │   └── ProgramRepository.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Board.java
│   │   └── Program.java
│   ├── JwtService.java
│   ├── JwtFilter.java
│   ├── RateLimitFilter.java
│   ├── SecurityConfig.java
│   └── Esp32otaApplication.java
├── esp32/
│   └── base_firmware.ino             ← Base firmware for ESP32
├── frontend/                         ← Coming soon
└── src/main/resources/
    └── application.properties
```

---

## 🗄️ Database Schema

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    api_token VARCHAR(255) UNIQUE NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE boards (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    chip_model VARCHAR(50),
    chip_revision INTEGER,
    cpu_freq_mhz INTEGER,
    flash_size INTEGER,
    heap_size INTEGER,
    mac_address VARCHAR(50) UNIQUE,
    status VARCHAR(20) DEFAULT 'OFFLINE',
    last_seen TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE programs (
    id SERIAL PRIMARY KEY,
    board_id INTEGER REFERENCES boards(id),
    user_id INTEGER REFERENCES users(id),
    name VARCHAR(100),
    code TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 🔌 API Endpoints

### Users
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/users/register` | — | Register + receive API token |
| POST | `/api/users/login` | — | Login → JWT token |
| GET | `/api/users/verify?token=` | — | Email verification |
| POST | `/api/users/token/regenerate` | JWT | Regenerate API token |

### Boards
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/boards/register` | X-API-TOKEN | ESP32 registers itself |
| GET | `/api/boards` | JWT | Get all boards for user |
| GET | `/api/boards/{id}` | JWT | Get board details |
| POST | `/api/boards/{id}/heartbeat` | X-API-TOKEN | ESP32 heartbeat |
| DELETE | `/api/boards/{id}` | JWT | Remove board |

### Programs
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/programs/upload` | JWT | Upload new program |
| GET | `/api/programs/pending/{boardId}` | X-API-TOKEN | ESP32 polls for pending |
| PUT | `/api/programs/{id}/status` | X-API-TOKEN | ESP32 updates status |
| GET | `/api/programs/user` | JWT | All programs for user |
| DELETE | `/api/programs/{id}` | JWT | Delete program |

### Logs
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/log/{boardId}` | X-API-TOKEN | ESP32 sends log |
| GET | `/api/log/{boardId}` | JWT | Frontend reads logs |
| DELETE | `/api/log/{boardId}` | JWT | Clear logs |

---

## 📖 API Documentation

Swagger UI available at:
```
http://localhost:8083/swagger-ui/index.html
```

---

## 🔒 Security

- JWT Authentication on all protected endpoints
- Email verification with disposable email blocking
- API Token authentication for ESP32 communication
- Board fingerprinting (MAC address validation)
- Rate limiting via Bucket4j
- Max 3 boards per user
- Input sanitization on uploaded C++ code (coming soon)

---

## 📡 ESP32 Base Firmware

The base firmware handles:
- WiFi connection (auto-scan for open networks as fallback)
- Board self-registration with full hardware specs on startup
- `LOG()` function — sends logs to backend instead of Serial Monitor
- Polling for pending programs every 30 seconds
- OTA update execution
- Status reporting after update

```cpp
// Configuration — set before flashing
const String API_TOKEN  = "your_api_token_here";
const String WIFI_SSID  = "your_wifi_name";
const String WIFI_PASS  = "your_wifi_password";
const String BOARD_NAME = "my-esp32";
const String SERVER_URL = "https://otaplatform.serveousercontent.com";
```

---

## 🚀 Setup & Installation

### Prerequisites
- Java 21
- PostgreSQL
- Maven

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Narcis47/ESP32-OTA-Platform.git
cd ESP32-OTA-Platform
```

**2. Create the PostgreSQL database**
```sql
CREATE DATABASE esp32ota;
```

**3. Run the schema** (copy SQL from above)

**4. Set environment variables**
```
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password
```

**5. Run the backend**
```bash
./mvnw spring-boot:run
```

API runs at `http://localhost:8083`
Swagger UI at `http://localhost:8083/swagger-ui/index.html`

---

## 🚀 Roadmap

### ✅ Phase 1 — Backend Foundation
- [x] Spring Boot project setup
- [x] Database schema
- [x] User registration + JWT
- [x] API Token generation
- [x] Rate limiting
- [x] Max 3 boards per user
- [x] Disposable email blocking

### ✅ Phase 2 — Board Management
- [x] Board registration endpoint
- [x] Hardware specs storage
- [x] ONLINE/OFFLINE status tracking
- [x] Board fingerprinting (MAC address)
- [x] Heartbeat endpoint

### ✅ Phase 3 — Logging System
- [x] In-memory log service
- [x] POST/GET/DELETE log endpoints

### ✅ Phase 4 — Program Management
- [x] Program CRUD
- [x] Pending program polling
- [x] Status updates

### 🔄 Phase 5 — Arduino CLI Integration
- [ ] Arduino CLI installation
- [ ] Compilation via ProcessBuilder
- [ ] Flash size validation
- [ ] .bin file serving

### 🔄 Phase 6 — ESP32 Base Firmware
- [ ] WiFi connection + auto-scan
- [ ] Board self-registration
- [ ] LOG() function
- [ ] Program polling
- [ ] OTA update

### 🔄 Phase 7 — Frontend
- [ ] Login / Register
- [ ] Dashboard with board status
- [ ] Live Serial Monitor
- [ ] Monaco code editor
- [ ] OTA upload + progress

### 🔄 Phase 8 — Email Verification
- [ ] Gmail SMTP integration
- [ ] Verification email on register
- [ ] Re-enable email check on board register

### 🔄 Phase 9 — Deploy
- [ ] Serveo tunnel port 8083
- [ ] Demo video

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Narcis** — [@Narcis47](https://github.com/Narcis47)
