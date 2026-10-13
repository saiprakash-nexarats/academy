# Nexarats Academy — Enterprise Monorepo

Welcome to the **Nexarats Academy** repository. This project is a comprehensive enterprise educational LMS, multi-tenant academy, employee learning platform, and SaaS training system built across 70 roadmap phases.

---

## 1. Repository Structure

```text
academy/
├── apps/
│   ├── web/                    # React 19 + TypeScript + Vite web portal (@nexarats/web)
│   └── mobile/                 # Flutter + Dart cross-platform mobile workspace (Phase 53+)
│
├── services/
│   ├── core-backend/           # Java 21 LTS + Spring Boot 4.1.1 + Spring Modulith 2.1.1
│   └── ai-subsystem/           # Python + FastAPI RAG tutor placeholder (Phase 46+)
│
├── packages/
│   ├── ui-tokens/              # Shared brand design tokens (@nexarats/ui-tokens)
│   └── api-contracts/          # OpenAPI / Zod type contracts (@nexarats/api-contracts)
│
├── infrastructure/
│   ├── docker/                 # PostgreSQL 16, Redis 7, MinIO docker-compose setup
│   └── scripts/                # Cross-platform developer management scripts (.ps1, .sh)
│
├── docs/
│   └── academy-roadmap/        # Authoritative 70-phase blueprints and execution reports
│
├── legacy/
│   └── server-node-skeleton/   # Inactive early Node prototype preserved for provenance
│
├── .github/
│   └── workflows/ci.yml        # CI/CD pipeline for web, backend, and security
├── pnpm-workspace.yaml         # pnpm workspace definition
├── turbo.json                  # Turborepo task orchestrator
├── package.json                # Root workspace scripts
└── .gitignore                  # Enterprise secret and build artifact exclusion
```

---

## 2. Prerequisites & Toolchain

| Tool | Version / Specification | Purpose |
|---|---|---|
| **Node.js** | `>= 20.x` (Tested on `v26.7.0`) | JavaScript runtime |
| **pnpm** | `>= 10.x` (Tested on `v12.11.2`) | High-efficiency package manager |
| **Java JDK** | `21 LTS` (Microsoft OpenJDK `21.0.12.1`) | Core backend runtime |
| **Apache Maven** | `>= 3.9.x` (Tested on `3.9.9`) | Backend build tool |
| **Docker Engine** | `>= 24.x` (Tested on `29.7.2`) | Local databases and caching |
| **Git** | `>= 2.40.x` | Source control |

---

## 3. Local Development Quickstart

### Step 1: Install Workspace Dependencies
```bash
pnpm install
```

### Step 2: Build Design Tokens
```bash
pnpm --filter=@nexarats/ui-tokens build
```

### Step 3: Run the Web Application
```bash
pnpm run dev:web
```
The React landing page is accessible at `http://localhost:5173`.

### Step 4: Start Local Infrastructure (PostgreSQL, Redis, MinIO)
**PowerShell (Windows):**
```powershell
.\infrastructure\scripts\dev-up.ps1
```
**Bash (macOS / Linux):**
```bash
./infrastructure/scripts/dev-up.sh
```
Or directly with Docker Compose:
```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

### Step 5: Run the Java Backend
Ensure `JAVA_HOME` points to JDK 21:
**PowerShell (Windows):**
```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;C:\Users\voakh\AppData\Local\Programs\apache-maven-3.9.9\bin;$env:Path"
mvn -f services/core-backend/pom.xml spring-boot:run
```
The core backend starts on `http://localhost:8085`.
- Health Check: `http://localhost:8085/api/v1/health`
- Spring Actuator: `http://localhost:8085/actuator/health`

---

## 4. Verification and Testing

### Frontend Lint & Production Build
```bash
pnpm run lint:web
pnpm run build:web
```

### Backend Unit, Context & Modulith Architecture Tests
```bash
mvn -f services/core-backend/pom.xml test
```

### Health Check Script
```powershell
.\infrastructure\scripts\check-health.ps1
```

---

## 5. Port Allocations

| Service | Port | Endpoint / Purpose |
|---|---|---|
| **Web Portal (Vite)** | `5173` | React Landing Page |
| **Core Backend (Spring Boot)** | `8085` | REST API & Actuator (`/api/v1/health`, `/actuator`) |
| **PostgreSQL 16** | `5432` | Relational Database (`nexarats_academy`) |
| **Redis 7** | `6379` | Cache & Rate Limiting |
| **MinIO API** | `9000` | S3-compatible Object Storage |
| **MinIO Console** | `9001` | MinIO Storage Web Dashboard |

---

## 6. Architecture & Security Policies

1. **Multi-Tenancy Resolution**: Client headers (`X-Tenant-ID`) or subdomains must NEVER be trusted blindly. Tenant resolution must be derived from authenticated server-side sessions/tokens. Database Row Level Security (RLS) is defense-in-depth, not a replacement for application authorization.
2. **Zero Credentials in Git**: Real credentials, `.env` files, and local logs are strictly ignored by `.gitignore`.
3. **Brand Preservation**: Nexarats Academy brand colors (`#2563EB`, `#1D4ED8`, `#0F1D40`), Plus Jakarta Sans typography, and UI layout must remain consistent.
