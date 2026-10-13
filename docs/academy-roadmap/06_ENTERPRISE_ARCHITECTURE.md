# NEXARATS ACADEMY — ENTERPRISE ARCHITECTURE BLUEPRINT
## System Topology, Modular Monolith Design, Distributed AI, and Cloud Infrastructure

- **Document ID:** `06_ENTERPRISE_ARCHITECTURE`
- **Execution Date:** 2026-10-13
- **Classification:** Enterprise Technical Architecture Specification

---

## 1. High-Level Architectural Topology

```text
                               ┌────────────────────────────────────────────────────────┐
                               │                    Cloudflare CDN & WAF                 │
                               │           DDoS Protection / Edge SSL / Caching          │
                               └───────────┬────────────────────────────────┬───────────┘
                                           │                                │
                       ┌───────────────────▼───────────────────┐            │
                       │             Web Clients               │            │
                       │    React 19 + TypeScript + Vite       │            │
                       │   (Public, Student, Mentor, Admin)    │            │
                       └───────────────────┬───────────────────┘            │
                                           │                                │
                       ┌───────────────────▼───────────────────┐            │
                       │            Mobile Clients             │            │
                       │          Flutter + Dart App           │            │
                       │           (iOS & Android)             │            │
                       └───────────────────┬───────────────────┘            │
                                           │                                │
                               ┌───────────▼────────────────────────────────▼───────────┐
                               │                   API Gateway & Ingress                 │
                               │                NGINX / Envoy Reverse Proxy              │
                               │         Rate Limiting / SSL Offload / Auth Pre-check    │
                               └───────────┬────────────────────────────────┬───────────┘
                                           │                                │
               ┌───────────────────────────▼────────────────────────┐       │
               │             CORE BACKEND SYSTEM                    │       │
               │    Java 21 + Spring Boot 4.1.1 Modular Monolith    │       │
               │                                                    │       │
               │  ┌──────────────────────┐  ┌────────────────────┐  │       │
               │  │  Tenant & Org Module │  │ Auth & RBAC Module │  │       │
               │  ├──────────────────────┤  ├────────────────────┤  │       │
               │  │  Curriculum & LMS    │  │ Admissions & CRM   │  │       │
               │  ├──────────────────────┤  ├────────────────────┤  │       │
               │  │  Mentorship Module   │  │ Finance & Billing  │  │       │
               │  ├──────────────────────┤  ├────────────────────┤  │       │
               │  │  Assessment & Coding │  │ Analytics & Reports│  │       │
               │  └──────────────────────┘  └────────────────────┘  │       │
               └───────────┬───────────────────┬────────────────┬───┘       │
                           │                   │                │           │
       ┌───────────────────▼───────┐           │     ┌──────────▼───────────▼───────────┐
       │   AI & ADAPTIVE SUBSYSTEM │           │     │     REAL-TIME MEDIA CLUSTER      │
       │     Python 3.12 + FastAPI │           │     │       LiveKit WebRTC SFU         │
       │    LangChain / LlamaIndex │           │     │    Interactive Video / Audio     │
       │   Syllabus RAG & AI Tutor │           │     │   Screen Sharing / Breakouts     │
       └───────────────────┬───────┘           │     └──────────────────────────────────┘
                           │                   │
                           ▼                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                 MESSAGE BROKER & EVENT BUS             │
       │          Apache Kafka / RabbitMQ Event Streams         │
       │    Async Jobs / Video Transcoding / Webhook Dispatch   │
       └───────────────────┬───────────────────┬────────────────┘
                           │                   │
       ┌───────────────────▼────────┐      ┌───▼────────────────────┐      ┌────────────────────────┐
       │      PRIMARY DATABASE      │      │     CACHE & PUB/SUB    │      │  OBJECT STORAGE (S3)   │
       │   PostgreSQL 16 Cluster    │      │     Redis 7 Cluster    │      │  AWS S3 / MinIO Tier   │
       │    Multi-Tenant Schemas    │      │   Sessions / Counters  │      │  Lesson Videos / HLS   │
       │  pgvector / Read Replicas  │      │  Live Polls / Caching  │      │  Assets & Transcripts  │
       └────────────────────────────┘      └────────────────────────┘      └────────────────────────┘
```

---

## 2. Component Technology Stack

### 2.1 Web Frontend
- **Framework:** React 19 + TypeScript (Strict mode enabled).
- **Tooling:** Vite 8 with Hot Module Replacement (HMR).
- **Styling:** Tailwind CSS 3.4 (with customized brand design tokens preserved).
- **State & Server Cache:** TanStack Query v5 (for remote API caching) + Zustand (for client-side transient state).
- **Routing:** React Router v7 with dynamic code-splitting (`React.lazy`).
- **Icons & Animation:** Lucide React, Framer Motion v14.

### 2.2 Mobile Client Applications
- **Framework:** Flutter (latest stable SDK).
- **Language:** Dart.
- **Styling:** Material 3 with Custom Nexarats Brand Theme (`NexaratsColors`, `NexaratsTypography`).
- **Live Video:** LiveKit Flutter SDK for native WebRTC streaming.
- **Offline Storage:** Drift (SQLite) with encrypted local storage for downloaded course lessons.

### 2.3 Core Backend (Modular Monolith)
- **Language:** Java 21 LTS (utilizing Virtual Threads / Project Loom for lightweight high-concurrency requests).
- **Framework:** Spring Boot 4.1.1 + Spring Modulith 2.1.1 (ADR-002, superseding EOL Spring Boot 3.3).
- **Architectural Pattern:** **Modular Monolith** organized with clean domain-driven boundaries and Spring Modulith verification. This architecture prevents early microservice network overhead while maintaining strict internal decoupling through application module boundaries and Java package encapsulation.
- **Persistence:** Spring Data JPA + Hibernate with Flyway database migration scripts.
- **Security:** Spring Security (version resolved by Spring Boot dependency management) with stateless sessions, Nimbus Jose JWT validation, and RBAC/ABAC method security.
- **Validation:** Jakarta Bean Validation (Hibernate Validator).

### 2.4 AI & Machine Learning Subsystem
- **Language:** Python 3.12.
- **Framework:** FastAPI (asynchronous ASGI framework).
- **AI Orchestration:** LangChain / LlamaIndex for Retrieval-Augmented Generation (RAG).
- **Vector Search:** `pgvector` extension inside PostgreSQL or dedicated Qdrant vector database.
- **Embeddings & LLMs:** OpenAI API (`text-embedding-3-small`, `gpt-4o-mini`) / Anthropic Claude 3.5 Sonnet / Local HuggingFace embeddings.
- **Asynchronous Processing:** Celery with Redis broker for heavy document embedding ingestion.

### 2.5 Data & Infrastructure Services
- **Primary Database:** PostgreSQL 16 (High-Availability active-passive replica setup).
- **In-Memory Cache:** Redis 7 (Cluster mode with persistent snapshotting).
- **Live Classroom SFU:** LiveKit Server (Go-based WebRTC Selective Forwarding Unit).
- **Object Storage:** S3-compatible storage (MinIO for local development; AWS S3 + CloudFront CDN for production).
- **Event Bus:** RabbitMQ / Apache Kafka for background asynchronous job orchestration (video transcoding, notification dispatches, webhook publishing).

---

## 3. Modular Monolith Domain Decomposition

The Core Spring Boot application is organized into 10 cohesive, decoupled modules:

```text
com.nexarats.academy/
├── tenant/            # Multi-tenancy, organization hierarchy, white-labeling
├── auth/              # Identity, OAuth2, session governance, MFA, RBAC
├── academic/          # Programs, courses, modules, lessons, prerequisites
├── cohort/            # Batches, live timetables, room schedules, attendance
├── mentorship/        # Mentor profiles, 1:1 slot booking, compensation ledger
├── assessment/        # Exams, question banks, code sandbox bridge, rubrics
├── admissions/        # Lead CRM, counseling calendar, enrollment workflow
├── corporate/         # B2B departments, employee seats, compliance tracking
├── finance/           # Invoices, installments, payment gateways, tax calculation
├── notification/      # Email (SendGrid), SMS (Twilio), Push (FCM), In-app WebSocket
└── telemetry/         # Audit logging, system health, business metrics
```

### 3.1 Inter-Module Communication Rules
1. **No Cross-Module Database Joins:** A repository in `com.nexarats.academy.academic` may not perform direct SQL joins on tables owned by `com.nexarats.academy.finance`.
2. **Synchronous Contracts via Service Interfaces:** Cross-module queries execute via explicit public Java interfaces (e.g. `StudentEnrollmentQueryService`).
3. **Asynchronous Decoupling via Domain Events:** Cross-module lifecycle events are published via Spring's `ApplicationEventPublisher` (e.g. `StudentPassedExamEvent` triggers `CertificateGenerationListener` in the academic module).

---

## 4. Scalability, High Availability & Disaster Recovery

### 4.1 Scalability Targets
- **Concurrent Live Students:** 50,000 active concurrent connections.
- **Live Classroom Capacity:** Up to 1,000 concurrent participants per live lecture session across WebRTC SFU mesh nodes.
- **API Response Latency:** P95 < 80ms for all cached read requests; P95 < 200ms for transactional database writes.
- **Database Connection Pooling:** HikariCP with connection pooling tuned for Virtual Threads (50–100 physical connections supporting 10,000+ concurrent requests).

### 4.2 High Availability & Failover
- **Stateless App Nodes:** The Spring Boot backend and FastAPI AI service are strictly stateless. Any node failure is automatically replaced by Kubernetes / Docker Swarm health probes.
- **PostgreSQL Replication:** Primary read-write node with streaming synchronous replication to a standby node, plus 2 read replicas for reporting and catalog queries.
- **Redis High Availability:** Redis Sentinel cluster providing automated master failover within 3 seconds.

### 4.3 Disaster Recovery Plan (RPO & RTO)
- **Recovery Point Objective (RPO):** < 5 minutes (continuous WAL archiving to S3).
- **Recovery Time Objective (RTO):** < 30 minutes (automated Infrastructure-as-Code Terraform / Docker redeployment).
- **Automated Nightly Backups:** Daily encrypted snapshots of all PostgreSQL databases and S3 bucket cross-region replication.
