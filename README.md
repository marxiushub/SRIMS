# 🎿 SRIMS — Sports & Recreation Inventory Management System

A full-stack web application for managing, renting, and tracking sports equipment. Built as a university group project at TU Wien (SE PR Group Phase, SS 2026).

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend](#backend)
  - [Frontend](#frontend)
- [Environment Variables](#-environment-variables)
- [Architecture Overview](#-architecture-overview)
- [Email Service](#-email-service)
- [Concurrency & Locking](#-concurrency--locking)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Running Tests](#-running-tests)

---

## ✨ Features

- **Equipment Management** — Track skis, snowboards, helmets, boots and poles with barcode IDs and rental status
- **Reservation System** — Create, update, and cancel reservations with equipment availability validation
- **Customer Profiles** — Manage multiple rental profiles per customer with skill-level matching
- **Barcode Scanning** — Scan equipment barcodes for fast pick-up and return processing
- **Role-Based Access Control** — Separate interfaces and permissions for customers and staff
- **Automated Email Notifications** — Reservation confirmations, overdue reminders, pick-up alerts, and password resets
- **Scheduled Background Jobs** — Automatic daily processing of overdue and upcoming reservations
- **Statistics Dashboard** — Visual overview of rental activity and equipment utilization

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4.x, Spring Security, Spring Data JPA |
| Database | H2 (file-based, persistent) |
| ORM / Mapping | Hibernate, MapStruct |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Email | Spring Mail + Brevo SMTP relay + Thymeleaf HTML templates |
| Frontend | Angular 21, TypeScript, Bootstrap 5, RxJS |
| Barcode | ZXing (`@zxing/ngx-scanner`), JsBarcode |
| Auth | JWT (JJWT) |
| Build | Maven (backend), npm / Angular CLI (frontend) |
| CI/CD | GitLab CI |
| Containerisation | Google Jib (Docker image build) |

---

## 📁 Project Structure

```
.
├── backend/          # Spring Boot application
│   ├── src/main/java/...
│   │   ├── config/       # Security, CORS, Email configuration
│   │   ├── endpoint/     # REST controllers & DTOs
│   │   ├── entity/       # JPA entities (Equipment, Reservation, User…)
│   │   ├── repository/   # Spring Data repositories
│   │   ├── service/      # Business logic
│   │   └── job/          # Scheduled background jobs
│   └── src/main/resources/
│       ├── application.yml
│       └── templates/    # Thymeleaf HTML email templates
├── frontend/         # Angular application
│   └── src/app/
│       ├── components/   # UI components
│       ├── services/     # HTTP services
│       ├── guards/       # Route guards
│       └── dtos/         # TypeScript data models
└── database/         # H2 database file
```

---

## 🚀 Getting Started

### Prerequisites

- Java 25+
- Maven 3.9+
- Node.js 22+ & npm
- Angular CLI (`npm install -g @angular/cli`)

### Backend

```bash
cd backend

# Run with default settings (no test data)
mvn spring-boot:run

# Run with auto-generated seed data
mvn spring-boot:run -Dspring-boot.run.profiles=generateData

# Run without sending real emails (uses a fake/logging email service)
mvn spring-boot:run -Dspring-boot.run.profiles=no-email

# Combine profiles
mvn spring-boot:run -Dspring-boot.run.profiles=generateData,no-email
```

The backend starts on **http://localhost:8080**.  
Swagger UI is available at **http://localhost:8080/swagger-ui.html**.  
Actuator health endpoint is on port **8081**.

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server (with proxy to backend at localhost:8080)
npm start
# → http://localhost:4200
```

> **Tip:** The Angular dev server proxies `/api` requests to `http://localhost:8080` via `proxy.conf.json`. Ensure `"proxyConfig": "proxy.conf.json"` is set in `angular.json` under `serve.options`.

---

## 🔐 Environment Variables

| Variable | Description | Required |
|---|---|---|
| `BREVO_SMTP_PASSWORD` | SMTP relay password for the Brevo email API | Yes (production) |

For local development, use the `no-email` Spring profile to skip real email delivery entirely.

---

## 🏗 Architecture Overview

```
Browser (Angular 21)
      │
      │  HTTP (REST/JSON)
      ▼
Spring Boot Backend (port 8080)
      │
      ├── Spring Security + JWT
      ├── REST Endpoints  (/api/v1/...)
      ├── Service Layer   (business logic, validation)
      ├── Repository Layer (Spring Data JPA + pessimistic locking)
      │
      └── H2 Database (file: ./database/db)

Background Jobs (Spring Scheduler)
      ├── Daily 22:00 → Overdue reminder emails
      └── Daily 08:00 → Pick-up reminder emails
```

---

## 📧 Email Service

Emails are sent via the **Brevo SMTP relay** (`smtp-relay.brevo.com:587`) using **JavaMailSender** and styled with **Thymeleaf HTML templates**.

| Email | Trigger |
|---|---|
| `welcome-email` | New customer account created |
| `reservation-created-email` | Reservation confirmed |
| `pick-up-reminder-email` | Reservation starts within 2 days |
| `overdue-email` | Equipment not returned past due date |
| `password-reset-email` | Staff initiates a password reset |

**Spring profile behaviour:**
- `@Profile("!test & !no-email")` — `EmailServiceImpl` sends real emails via Brevo
- `test` / `no-email` profiles — `FakeEmailService` logs to console only, no network calls

---

## 🔒 Concurrency & Locking

To prevent race conditions (e.g. two users booking the same equipment simultaneously), the system uses **pessimistic write locking** (`PESSIMISTIC_WRITE`) on all critical read operations in `ReservationRepository` and `EquipmentRepository`.

Lock acquisition order is consistent throughout the codebase (`Reservation` → `Equipment`) to prevent deadlocks.

Background jobs that process overdue and pick-up reminders also hold pessimistic locks during their read–update cycle to prevent lost updates from concurrent user modifications.

---

## ⚙️ CI/CD Pipeline

Defined in `.gitlab-ci.yml`:

| Stage | Job | Description |
|---|---|---|
| `test` | `test-backend` | `mvn clean install` — Checkstyle, unit & integration tests |
| `test` | `test-frontend` | `npm ci`, ESLint, Angular production build |
| `test` | `gitinspector` | Contributor statistics report |
| `build` | `build-image` | Google Jib Docker image pushed to GitLab registry |
| `deploy` | `deploy` | Rolling deployment via reset.inso-world.com API |

Build and deploy stages run on the **`master`** branch only.

---

## 🧪 Running Tests

### Backend

```bash
cd backend

# Run all tests
mvn clean test -Dmaven.gitcommitid.skip=true -Dgit.commit.id.skip=true

# Checkstyle only
mvn checkstyle:check
```

JaCoCo code coverage reports are generated in `target/site/jacoco/` after each test run.

### Frontend

```bash
cd frontend

# Lint
npm run lint

# Unit tests (requires Chrome/Chromium)
npm test

# Headless (CI environments)
npm test -- --watch=false --browsers=ChromeHeadless
```

---

