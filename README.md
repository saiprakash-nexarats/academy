# Nexarats Academy — Enterprise Education Platform

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-2.1.1-6DB33F)](https://spring.io/projects/spring-modulith)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.8-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.4-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![React 19](https://img.shields.io/badge/React-19.0-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-8.3-646CFF?logo=vite&logoColor=white)](https://vitejs.dev/)
[![Flyway](https://img.shields.io/badge/Flyway-V1--V10-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Tests](https://img.shields.io/badge/Backend%20Tests-81%2F81%20Passing-brightgreen?logo=apachemaven)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Roadmap-Phase%206%20Completed-2563EB)](./docs/academy-roadmap/11_COMPLETE_70_PHASE_ROADMAP.md)

**Nexarats Academy** is an enterprise-grade education and learning management platform designed for universities, enterprise workforce upskilling, and multi-tenant SaaS academies. Built across a disciplined **70-phase engineering roadmap**, the platform delivers resilient identity governance, modular curriculum architectures, Redis-accelerated catalogs, concurrent seat capacity controls, payment gateway abstractions, and reactive user interfaces.

---

## Table of Contents
- [1. Executive Architectural Overview](#1-executive-architectural-overview)
- [2. Monorepo Structure](#2-monorepo-structure)
- [3. Completed Phases (0–5)](#3-completed-phases-05)
- [4. Core Technology Stack](#4-core-technology-stack)
- [5. Database & Schema Migrations (Flyway V1–V9)](#5-database--schema-migrations-flyway-v1v9)
- [6. API Specifications & Endpoints](#6-api-specifications--endpoints)
- [7. Local Development Quickstart](#7-local-development-quickstart)
- [8. Automated Testing & Verification](#8-automated-testing--verification)
- [9. Security, IAM & Financial Safety Architecture](#9-security-iam--financial-safety-architecture)
- [10. Roadmap Snapshot (Phases 0–69)](#10-roadmap-snapshot-phases-069)
- [11. Documentation Directory](#11-documentation-directory)

---

## 1. Executive Architectural Overview

```text
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    CLIENT PLATFORMS                                     │
│   React 19 + TypeScript Web Portal (apps/web)  │  Flutter + Dart Mobile (Phase 53+)     │
└───────────────────────────────────────────┬─────────────────────────────────────────────┘
                                            │ HTTP / JSON / Problem Details (RFC 7807)
                                            ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    NEXARATS CORE BACKEND (Spring Boot 4.1.1 + Modulith)                 │
│                                                                                         │
│   ┌───────────────────────────┐  ┌───────────────────────────┐  ┌─────────────────────┐ │
│   │   Identity & Auth Module  │  │ Academic & Catalog Module │  │ Admissions Module   │ │
│   │   - Argon2id Hash Engine  │  │ - Programs & Courses APIs │  │ - Application Engine│ │
│   │   - RS256 JWT Tokenissuer │  │ - Curriculum Hierarchies  │  │ - Review & Decisions│ │
│   │   - HttpOnly Cookie Store │  │ - Semantic Versioning     │  │ - Offer Acceptance  │ │
│   └─────────────┬─────────────┘  └────────────┬──────────────┘  └──────────┬──────────┘ │
│                 │                             │                            │            │
│                 ▼                             ▼                            ▼            │
│   ┌───────────────────────────┐  ┌───────────────────────────┐  ┌─────────────────────┐ │
│   │  Tenant Context Resolver  │  │ Redis Cache Service       │  │ Cohort & Seats      │ │
│   │  - Strict Server Scoping  │  │ - catalog:list:*          │  │ - Pessimistic Lock  │ │
│   │  - Global vs Tenant Data  │  │ - catalog:course:*        │  │ - Seat Reservation  │ │
│   └───────────────────────────┘  └───────────────────────────┘  └──────────┬──────────┘ │
│                                                                            │            │
│                 ┌──────────────────────────────────────────────────────────┘            │
│                 ▼                                                                       │
│   ┌───────────────────────────┐                                                         │
│   │ Finance & Billing Module  │  ◄── PaymentCompletedEvent (Decoupled Domain Event)     │
│   │ - Server Tuition Pricing  │                                                         │
│   │ - Stripe & Razorpay SDK   │                                                         │
│   │ - Webhook Replay Defense  │                                                         │
│   │ - Immutable Paid Invoices │                                                         │
│   └───────────────────────────┘                                                         │
└──────────────────────────┬─────────────────────────────────────────┬────────────────────┘
                           │ JDBC / HikariPool                       │ Jedis / Lettuce
                           ▼                                         ▼
┌──────────────────────────────────────────────────┐  ┌───────────────────────────────────┐
│           PostgreSQL 16.8 Database               │  │        Redis 7.4 In-Memory        │
│  - Flyway Migrations V1 through V9               │  │  - Low-latency Catalog Cache      │
│  - RLS Policies & Strict Database Constraints    │  │  - Rate Limiting Token Buckets    │
│  - Pessimistic Row Locking for Seat Capacity     │  │                                   │
└──────────────────────────────────────────────────┘  └───────────────────────────────────┘
```

---

## 2. Monorepo Structure

The repository is managed via **Turborepo** and **pnpm workspaces**:

```text
academy/
├── apps/
│   ├── web/                     # React 19 + TypeScript + Vite web portal (@nexarats/web)
│   └── mobile/                  # Flutter + Dart cross-platform mobile workspace (Phase 53+)
│
├── services/
│   ├── core-backend/            # Java 21 LTS + Spring Boot 4.1.1 + Spring Modulith 2.1.1
│   └── ai-subsystem/            # Python + FastAPI RAG tutor placeholder (Phase 46+)
│
├── packages/
│   ├── api-contracts/           # Shared TypeScript interfaces & OpenAPI 3.1 YAML schemas
│   └── ui-tokens/               # Shared brand design tokens, CSS variables, typography
│
├── infrastructure/
│   ├── docker/                  # Docker Compose for PostgreSQL 16.8, Redis 7.4, MinIO
│   └── scripts/                 # Platform development & health check scripts (.ps1, .sh)
│
├── docs/
│   └── academy-roadmap/         # Authoritative 70-phase blueprints, audits, and reports
│
├── .github/
│   └── workflows/ci.yml         # GitHub Actions CI for Web, Backend, PostgreSQL, and Redis
├── pnpm-workspace.yaml          # Monorepo workspace package definitions
├── turbo.json                   # Turborepo task pipeline orchestration
├── package.json                 # Monorepo root package scripts
└── README.md                    # Platform documentation and developer guide
```

---

## 3. Completed Phases (0–6)

| Phase | Title | Summary & Deliverables |
|---|---|---|
| **Phase 0** | **Foundation & Audit** | Repository audit, monolithic inventory, dependency reconciliation, baseline validation. |
| **Phase 1** | **Enterprise Monorepo & Infra** | pnpm + Turborepo setup, Microsoft OpenJDK 21, Spring Boot 4.1.1, Docker Compose container recovery, Flyway V1 baseline. |
| **Phase 2** | **Enterprise Identity & RBAC** | Argon2id hashing, RS256 JWT key-rotation, HttpOnly cookies, CSRF double-submit protection, multi-tenant session isolation, audit logging, 34 automated security tests. |
| **Phase 3** | **Dynamic Program Catalog** | PostgreSQL 16.8 dynamic career programs (`V7`), Redis caching (`catalog:list:*`), TanStack Query React integration, brand preservation (`#2563EB`). |
| **Phase 4** | **Modular Course & Syllabus Engine** | Flyway `V8` schema, 8 migrated courses, 32 curriculum modules, 128 structured lessons, curriculum semantic versioning (`v1.0.0`), server-enforced lesson boundaries (403 on locked lessons), interactive React syllabus modal, 60/60 backend tests passing. |
| **Phase 5** | **Admissions, Enrollment & Payments** | Flyway `V9` schema (12 tables), student application lifecycle, administrative reviews & offers, atomic pessimistic seat locking (`SELECT FOR UPDATE`), server-authoritative tuition pricing, provider-neutral payment gateway (`PaymentGateway` with Stripe, Razorpay & Test adapters), webhook replay protection, immutable invoices, React `EnrollModal.tsx` connected to real API, 70/70 backend tests passing. |
| **Phase 6** | **Learning Dashboard, Video & Progress** | Flyway `V10` schema (5 tables), authenticated student learning dashboard (`/dashboard`), enrollment-based private lesson gate, tokenized secure playback sessions, RFC 7233 byte-range video streaming (HTTP 206), server-authoritative monotonic progress tracking, idempotency replay defense, React video player with resume markers, 81/81 backend tests passing. |

---

## 4. Core Technology Stack

### Backend Services
- **Runtime:** Java 21 LTS (Microsoft OpenJDK 21.0.12.1)
- **Framework:** Spring Boot 4.1.1 & Spring Modulith 2.1.1
- **Security:** Spring Security 6.x, Argon2id Password Encoder (OWASP compliant), RS256 JWT
- **Data Access:** Spring Data JPA, Hibernate ORM 7.x, HikariCP
- **Migrations:** Flyway Community Edition 10.x
- **Caching:** Spring Data Redis, Lettuce Client, Redis 7.4 Alpine
- **Validation:** Jakarta Validation 3.0, Problem Details (RFC 7807)
- **Financial Architecture:** Integer minor units (`amountCents`), provider-neutral payment gateway abstraction
- **Streaming Pipeline:** RFC 7233 byte-range HTTP streaming (HTTP 206 Partial Content), tokenized playback sessions

### Web Frontend
- **Framework:** React 19.0 with TypeScript 5.8
- **Build Tool:** Vite 8.3 with Rolldown bundling
- **State & Server Cache:** TanStack Query v5 (`@tanstack/react-query`)
- **Styling:** Tailwind CSS 3.4 with custom design tokens (`@nexarats/ui-tokens`)
- **Animations:** Framer Motion 12.x
- **Icons:** Lucide React
- **Linter:** Oxlint (high-performance lint engine)

### Infrastructure & DevOps
- **Relational Database:** PostgreSQL 16.8 (Debian Bookworm container)
- **Cache & Key-Value:** Redis 7.4 (Alpine container)
- **Object Storage:** MinIO S3-compatible storage
- **Task Orchestration:** Turborepo 2.11
- **CI/CD:** GitHub Actions with containerized PostgreSQL and Redis services

---

## 5. Database & Schema Migrations (Flyway V1–V10)

All schema evolutions are managed strictly through forward-only Flyway migrations under:
`services/core-backend/src/main/resources/db/migration/`

| Version | Migration Script | Description |
|---|---|---|
| **V1** | `V1__init_enterprise_baseline.sql` | Core schema, tenants, users, roles, and permissions baseline. |
| **V2** | `V2__security_and_tenant_rls.sql` | Multi-tenant Row Level Security (RLS) policies and tenant isolation functions. |
| **V3** | `V3__session_and_audit_log.sql` | User sessions, refresh token persistence, and audit logging tables. |
| **V4** | `V4__seed_bootstrap_tenant.sql` | Idempotent system bootstrap tenant (`nexarats-main`) and role seeds. |
| **V5** | `V5__fix_rls_policies.sql` | Refinement of multi-tenant PostgreSQL RLS policies. |
| **V6** | `V6__fix_tenant_id_fk_constraints.sql` | Foreign key referential integrity constraints across tenant tables. |
| **V7** | `V7__create_programs_and_categories_schema.sql` | Dynamic career programs, categories, and initial seed catalog. |
| **V8** | `V8__create_courses_and_curriculum_schema.sql` | Modular courses, curriculum versions, modules, lessons, and `program_courses` M:N join. |
| **V9** | `V9__admissions_and_enrollment_schema.sql` | Cohorts, tuition prices, applications, status history, admission offers, seat reservations, payment orders, transactions, webhooks, enrollments, and invoices. |
| **V10** | `V10__learner_progress_and_content_access.sql` | Learner course progress, lesson watch progress, learning activity audit trail with JSONB, lesson media asset catalog, and tokenized playback sessions. |

---

## 6. API Specifications & Endpoints

Shared OpenAPI 3.1 specifications and TypeScript definitions are located in `packages/api-contracts/`.

### 6.1 Authentication & Identity (`/api/v1/auth`)
- `POST /api/v1/auth/register`: Register new learner in tenant context.
- `POST /api/v1/auth/login`: Issue RS256 Bearer JWT and set HttpOnly refresh token cookie.
- `POST /api/v1/auth/refresh`: Rotate refresh token and issue fresh JWT (requires matching CSRF cookie & header).
- `POST /api/v1/auth/logout`: Revoke active session in database and clear auth cookies.
- `GET /api/v1/auth/me`: Retrieve current authenticated profile, active tenant, roles, and permissions.
- `POST /api/v1/auth/switch-tenant`: Switch active tenant context for multi-tenant accounts.
- `POST /api/v1/auth/forgot-password`: Request password reset token.

### 6.2 Career Programs Catalog (`/api/v1/academic/programs`)
- `GET /api/v1/academic/programs`: Paginated career programs list with category, duration, and search filters.
- `GET /api/v1/academic/programs/{slug}`: Program detail view, curriculum phases, and capstone info.
- `GET /api/v1/academic/program-categories`: List active program categories.

### 6.3 Modular Courses & Syllabus Engine (`/api/v1/academic/courses`)
- `GET /api/v1/academic/courses`: Paginated courses list with category, level, and keyword filters.
- `GET /api/v1/academic/courses/{slug}`: Course detail view, learning outcomes, and related career programs.
- `GET /api/v1/academic/courses/{slug}/syllabus`: Published curriculum hierarchy with ordered modules and lesson summaries.
- `GET /api/v1/academic/courses/{slug}/modules`: Ordered module list for active curriculum.
- `GET /api/v1/academic/courses/{slug}/lessons/{lessonId}/preview`: Public lesson preview endpoint (sanitized preview or 403 Forbidden).
- `POST /api/v1/academic/courses/{courseId}/versions/{versionId}/publish`: Atomically publish curriculum version (admin only).

### 6.4 Admissions & Student Applications (`/api/v1/admissions`)
- `POST /api/v1/admissions/applications`: Submit student application (supports guest inquiries and authenticated learners).
- `GET /api/v1/admissions/applications/mine`: Retrieve own applications for authenticated applicant.
- `GET /api/v1/admissions/applications/{id}`: Application detail lookup (authorized owner or tenant admin).
- `POST /api/v1/admissions/applications/{id}/withdraw`: Withdraw pending application prior to review.
- `GET /api/v1/admissions/applications`: Paginated tenant applications list (admin/staff only).
- `POST /api/v1/admissions/applications/{id}/review`: Review application with `APPROVED` or `REJECTED` decision.
- `GET /api/v1/admissions/applications/{id}/offer`: Retrieve issued admission offer details.
- `POST /api/v1/admissions/offers/{id}/accept`: Admitted learner accepts admission offer.
- `POST /api/v1/admissions/offers/{id}/pay`: Initiate payment order for accepted admission offer.
- `GET /api/v1/admissions/enrollments/mine`: Retrieve confirmed enrollments for authenticated learner.
- `GET /api/v1/admissions/enrollments`: Tenant enrollments list (admin only).

### 6.5 Cohorts & Seat Capacity (`/api/v1/admissions/cohorts`)
- `GET /api/v1/admissions/cohorts`: Public list of active intakes with real-time available seat counts.
- `GET /api/v1/admissions/cohorts/{id}`: Detailed cohort capacity and enrollment deadlines.

### 6.6 Payment Orders & Webhooks (`/api/v1/payments`)
- `POST /api/v1/payments/orders`: Initialize server-authoritative payment order with idempotency key.
- `POST /api/v1/payments/orders/verify`: Verify payment signature and settle order.
- `GET /api/v1/payments/orders/{id}`: Retrieve payment order status (owner or admin).
- `POST /api/v1/payments/webhooks/{provider}`: Secure provider webhook callback with HMAC signature verification and replay defense.

### 6.7 Tuition Invoicing & Billing (`/api/v1/billing/invoices`)
- `GET /api/v1/billing/invoices/mine`: View own paid tuition invoices.
- `GET /api/v1/billing/invoices/{id}`: Retrieve immutable invoice snapshot (owner or admin).
- `GET /api/v1/billing/invoices`: List all tenant invoices (admin only).

### 6.8 Student Learning & Progress Engine (`/api/v1/learning`)
- `GET /api/v1/learning/dashboard`: Real-time student dashboard with enrolled courses, programs, progress metrics, and continue-learning targets.
- `GET /api/v1/learning/my-courses`: List active enrolled courses with server-calculated progress.
- `GET /api/v1/learning/my-programs`: List active enrolled career programs.
- `GET /api/v1/learning/courses/{courseId}/syllabus`: Full enrolled syllabus with module/lesson progress, watch times, and locked/completed state flags.
- `GET /api/v1/learning/courses/{courseId}/lessons/{lessonId}`: Private lesson details, asset metadata, and resume positions (enrollment verified).
- `GET /api/v1/learning/courses/{courseId}/progress`: Real-time server-authoritative course progress percentage and completed lesson counts.
- `POST /api/v1/learning/lessons/{lessonId}/playback-session`: Issue tokenized, short-lived media stream token (`vtok_...`).
- `GET /api/v1/learning/media/stream?token={token}`: RFC 7233 byte-range HTTP streaming endpoint (HTTP 206 Partial Content).
- `POST /api/v1/learning/lessons/{lessonId}/progress`: Ingest playback checkpoint with idempotency key, delta bounds, and watch-time tracking.
- `POST /api/v1/learning/lessons/{lessonId}/complete`: Explicit lesson completion trigger with automatic course progress recalculation.

---

## 7. Local Development Quickstart

### Prerequisites
- **Node.js:** `>= 20.x`
- **pnpm:** `>= 10.x` (`corepack enable pnpm`)
- **Java JDK:** `21 LTS` (Microsoft OpenJDK 21 or Eclipse Temurin 21)
- **Apache Maven:** `>= 3.9.x`
- **Docker Engine & Docker Compose:** `>= 24.x`

### Step 1: Clone and Install Workspace Dependencies
```bash
git clone https://github.com/surya7989/academy.git
cd academy
pnpm install
```

### Step 2: Start Infrastructure Containers
Start PostgreSQL 16.8, Redis 7.4, and MinIO:
```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```
Verify container health:
```bash
docker ps
```

### Step 3: Run Database Migrations & Start Backend Service
The Spring Boot application automatically executes Flyway migrations `V1` through `V10` on startup:
```bash
mvn -f services/core-backend/pom.xml spring-boot:run
```
The Core Backend is accessible at:
- **Health Check:** `http://localhost:8085/actuator/health`
- **API Base:** `http://localhost:8085/api/v1`

### Step 4: Run the React Web Application
In a separate terminal:
```bash
pnpm run dev:web
```
The React web portal is accessible at `http://localhost:5173`.
- **Student Dashboard:** `http://localhost:5173/dashboard`
- **Classroom Player:** `http://localhost:5173/learning/courses/:courseId/lessons/:lessonId`

---

## 8. Automated Testing & Verification

### 8.1 Backend Test Suite (JUnit 5 + Spring Boot Test)
Run the full backend integration test suite against live PostgreSQL and Redis:
```bash
mvn -f services/core-backend/pom.xml test
```
**Current Test Results:**
```text
[INFO] Results:
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 8.2 Frontend Linting & Production Build
Validate TypeScript strict types, ESLint rules via Oxlint, and Vite production bundle:
```bash
# Run Oxlint
pnpm run lint:web

# Run TypeScript check & Vite build
pnpm run build:web
```
**Current Build Results:**
- `oxlint`: 0 warnings, 0 errors across 89 files in 107ms.
- `vite build`: `✓ 2565 modules transformed`, bundle generated in `2.48s`.

---

## 9. Security, IAM & Financial Safety Architecture

1. **Server-Enforced Tenant Resolution:**
   - Client headers (`X-Tenant-ID`) or request parameters are never trusted for authorization.
   - Tenant context is extracted cryptographically from verified RS256 JWT claims (`tenant_id`, `sid`).
2. **Atomic Pessimistic Concurrency for Cohort Capacity:**
   - Row-level lock (`SELECT FOR UPDATE`) prevents overselling under high concurrency.
   - Verified by multi-threaded race simulation: when 8 threads compete for 2 seats, exactly 2 succeed and 6 receive HTTP 409 Conflict.
3. **Server-Authoritative Pricing & Integer Minor Units:**
   - Client cannot modify tuition prices; amounts are resolved strictly server-side.
   - Stored as integer minor units (`amount_cents`). Zero floating-point math.
4. **Tokenized Media Streaming & RFC 7233 Range Isolation:**
   - Playback URLs utilize short-lived session tokens (`vtok_...`) bound to the student and enrollment.
   - Direct storage paths and S3 credentials are never exposed to clients.
5. **Monotonic Progress & Checkpoint Idempotency:**
   - Checkpoints with identical idempotency keys are deduplicated to prevent watch-time double counting.
   - Watch time deltas are bounded (`<= 300s`) to reject client falsification.
6. **Payment Webhook Replay Protection:**
   - PostgreSQL unique constraint `(provider, event_id)` rejects duplicate webhooks idempotently without double-settlement.
7. **Double-Submit CSRF Defense:**
   - Cookie operations (`/api/v1/auth/refresh`, `/api/v1/auth/logout`) require both the `nexarats_csrf_token` cookie and a matching `X-CSRF-Token` request header.

---

## 10. Roadmap Snapshot (Phases 0–69)

Nexarats Academy is engineered across 70 discrete phases. Consult [`docs/academy-roadmap/11_COMPLETE_70_PHASE_ROADMAP.md`](./docs/academy-roadmap/11_COMPLETE_70_PHASE_ROADMAP.md) for full phase specifications:

- **Phases 0–6 (Completed):** Groundwork, Monorepo Setup, Enterprise IAM & Auth, Dynamic Program Catalog, Modular Course & Syllabus Engine, Admissions, Enrollment & Payments Engine, Student Learning Dashboard & Enrolled Video Engine.
- **Phase 7 (Next):** Capstone Assignment Submission, Code Execution Sandbox & Certificate Generation.
- **Phases 8–10:** Interactive LMS Community, Peer Reviews & Discussions.
- **Phases 11–20:** Mentorship Booking, Live Class Delivery, Quizzes & Grading.
- **Phases 21–35:** Enterprise Multi-Tenant SaaS Dashboards, Analytics, Invoicing & Corporate Portals.
- **Phases 36–50:** AI Tutor Subsystem (RAG + LangChain), Code Laboratory Sandbox, Automated Proctoring.
- **Phases 51–69:** Cross-Platform Flutter Mobile Apps, Verification Badges, Enterprise HRMS Integrations, Global Scale.

---

## 11. Documentation Directory

All architectural blueprints, security models, database schemas, and milestone handoff reports are version-controlled under [`docs/academy-roadmap/`](./docs/academy-roadmap/):

- [`03_COMPLETE_PRODUCT_REQUIREMENTS.md`](./docs/academy-roadmap/03_COMPLETE_PRODUCT_REQUIREMENTS.md): Functional and non-functional requirements.
- [`06_ENTERPRISE_ARCHITECTURE.md`](./docs/academy-roadmap/06_ENTERPRISE_ARCHITECTURE.md): Microservices, modulith boundaries, caching, and infrastructure.
- [`07_DOMAIN_AND_DATABASE_BLUEPRINT.md`](./docs/academy-roadmap/07_DOMAIN_AND_DATABASE_BLUEPRINT.md): Comprehensive relational ERD and table specifications.
- [`10_SECURITY_PRIVACY_AND_TENANT_MODEL.md`](./docs/academy-roadmap/10_SECURITY_PRIVACY_AND_TENANT_MODEL.md): IAM, RBAC, tenant resolution, and data privacy policies.
- [`11_COMPLETE_70_PHASE_ROADMAP.md`](./docs/academy-roadmap/11_COMPLETE_70_PHASE_ROADMAP.md): Complete 70-phase development roadmap.
- [`36_PHASE_5_IMPLEMENTATION_REPORT.md`](./docs/academy-roadmap/36_PHASE_5_IMPLEMENTATION_REPORT.md): Phase 5 admissions, enrollment, seat management & payments report.
- [`37_PHASE_5_DATABASE_PAYMENT_AND_SECURITY_REPORT.md`](./docs/academy-roadmap/37_PHASE_5_DATABASE_PAYMENT_AND_SECURITY_REPORT.md): Phase 5 database schema, financial precision & security report.
- [`38_PHASE_5_TEST_AND_VALIDATION_REPORT.md`](./docs/academy-roadmap/38_PHASE_5_TEST_AND_VALIDATION_REPORT.md): Phase 5 automated test results (70/70 passing) and validation evidence.
- [`39_PHASE_6_READINESS_AND_GITHUB_HANDOFF.md`](./docs/academy-roadmap/39_PHASE_6_READINESS_AND_GITHUB_HANDOFF.md): Phase 6 readiness assessment and git synchronization baseline.
- [`40_PHASE_6_IMPLEMENTATION_REPORT.md`](./docs/academy-roadmap/40_PHASE_6_IMPLEMENTATION_REPORT.md): Phase 6 student dashboard, enrolled learning, video streaming & progress tracking report.
- [`41_PHASE_6_DATABASE_MEDIA_AND_PROGRESS_REPORT.md`](./docs/academy-roadmap/41_PHASE_6_DATABASE_MEDIA_AND_PROGRESS_REPORT.md): Phase 6 database schema (Flyway V10), media delivery & progress calculation report.
- [`42_PHASE_6_TEST_AND_SECURITY_VALIDATION.md`](./docs/academy-roadmap/42_PHASE_6_TEST_AND_SECURITY_VALIDATION.md): Phase 6 test matrix (81/81 backend tests passing) and security verification.
- [`43_PHASE_7_READINESS_AND_GITHUB_HANDOFF.md`](./docs/academy-roadmap/43_PHASE_7_READINESS_AND_GITHUB_HANDOFF.md): Phase 7 readiness assessment and handoff baseline.

---

## License

Copyright © 2026 Nexarats Academy. All rights reserved. Enterprise proprietary software.
