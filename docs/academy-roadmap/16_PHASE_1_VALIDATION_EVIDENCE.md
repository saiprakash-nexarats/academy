# Nexarats Academy — Phase 1 Validation Evidence
## Concrete Execution Logs, Verification Commands, and Exit Codes

**Document ID:** `16_PHASE_1_VALIDATION_EVIDENCE.md`  
**Phase:** Phase 1 (Enterprise Monorepo Foundation, Java Backend Skeleton, and Local Development Infrastructure)  
**Verification Date:** 2026-10-13  
**Verified By:** Gemini 3.8 Flash (Antigravity)  

---

## 1. Environment & Prerequisite Verification

All tools were verified directly on the developer host machine:

### 1.1 Node.js Runtime
```powershell
Command: node --version
Exit Code: 0
Output:
v26.7.0
```

### 1.2 npm Package Manager
```powershell
Command: npm --version
Exit Code: 0
Output:
11.19.0
```

### 1.3 pnpm Package Manager
```powershell
Command: pnpm --version
Exit Code: 0
Output:
12.11.2
```

### 1.4 Java JDK 21 LTS
```powershell
Command: & "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot\bin\java.exe" -version
Exit Code: 0
Output:
openjdk version "21.0.12.1" 2026-07-21 LTS
OpenJDK Runtime Environment Microsoft-10115386 (build 21.0.12.1+8-LTS)
OpenJDK 64-Bit Server VM Microsoft-10115386 (build 21.0.12.1+8-LTS, mixed mode, sharing)
```

### 1.5 Apache Maven
```powershell
Command: & "C:\Users\voakh\AppData\Local\Programs\apache-maven-3.9.9\bin\mvn.cmd" -version
Exit Code: 0
Output:
Apache Maven 3.9.9 (8e8579a9e76f7d015ee5ec7bf33d77018a36e29e)
Maven home: C:\Users\voakh\AppData\Local\Programs\apache-maven-3.9.9
Java version: 21.0.12.1, vendor: Microsoft, runtime: C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot
Default locale: en_IN, platform encoding: UTF-8
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

### 1.6 Git Version
```powershell
Command: git --version
Exit Code: 0
Output:
git version 2.55.0.windows.3
```

### 1.7 Docker Engine
```powershell
Command: docker --version
Exit Code: 0
Output:
Docker version 29.7.2, build 49911ea
```

---

## 2. Frontend Validation Evidence (`apps/web`)

### 2.1 Workspace Linting (oxlint)
```bash
Command: pnpm run lint:web
Underlying Command: turbo run lint --filter=@nexarats/web -> oxlint
Exit Code: 0
Execution Time: 436ms
Output:
   • turbo 2.11.7
   • Packages in scope: @nexarats/web
   • Running lint in 1 package
   • Remote caching disabled

@nexarats/web:lint: $ oxlint
@nexarats/web:lint: Found 0 warnings and 0 errors.
@nexarats/web:lint: Finished in 118ms on 77 files with 116 rules using 12 threads.

 Tasks:    1 successful, 1 total
Cached:    0 cached, 1 total
  Time:    436ms
```

### 2.2 Fresh Production Build (`tsc -b && vite build`)
```bash
Command: pnpm exec turbo run build --filter=@nexarats/web --force
Exit Code: 0
Execution Time: 5.847s (Vite build: 1.92s)
Output:
   • turbo 2.11.7
   • Packages in scope: @nexarats/web
   • Running build in 1 package
   • Remote caching disabled

@nexarats/web:build: $ tsc -b && vite build
@nexarats/web:build: vite v8.3.4 building client environment for production...
@nexarats/web:build: transforming...
@nexarats/web:build: ✓ 2508 modules transformed.
@nexarats/web:build: rendering chunks...
@nexarats/web:build: computing gzip size...
@nexarats/web:build: dist/index.html                                  1.17 kB │ gzip:   0.66 kB
@nexarats/web:build: dist/assets/student-sneha-Dhf-sXrs.jpg          27.28 kB
@nexarats/web:build: dist/assets/student-priya-CkeI6gpN.jpg          39.88 kB
@nexarats/web:build: dist/assets/student-ananya-D0rK7yoN.jpg         42.50 kB
@nexarats/web:build: dist/assets/student-rahul-COCfiCJE.jpg          49.62 kB
@nexarats/web:build: dist/assets/course-uiux-aOhsswWh.jpg            53.47 kB
@nexarats/web:build: dist/assets/course-datascience-CllStu9O.jpg     67.19 kB
@nexarats/web:build: dist/assets/course-marketing-DRU3gY4g.jpg       68.11 kB
@nexarats/web:build: dist/assets/mentor-arjun-Cidk3DFT.jpg          644.86 kB
@nexarats/web:build: dist/assets/hero-student-Dmpl4Ahq.jpg          696.08 kB
@nexarats/web:build: dist/assets/course-fullstack-BhaXRIql.jpg      729.52 kB
@nexarats/web:build: dist/assets/hero-student-clean-Nqaehh0h.png  1,476.50 kB
@nexarats/web:build: dist/assets/index-C5h8dlAX.css                  54.00 kB │ gzip:   9.67 kB
@nexarats/web:build: dist/assets/index-BR2YEGbJ.js                  781.33 kB │ gzip: 228.66 kB
@nexarats/web:build: ✓ built in 1.92s

 Tasks:    1 successful, 1 total
Cached:    0 cached, 1 total
  Time:    5.847s
```

### 2.3 Shared UI Tokens Build (`packages/ui-tokens`)
```bash
Command: pnpm --filter=@nexarats/ui-tokens build
Exit Code: 0
Output:
Wrote dist/index.js and dist/index.d.ts without errors.
```

---

## 3. Java Backend Validation Evidence (`services/core-backend`)

### 3.1 Maven Compilation
```bash
Command: mvn -f services/core-backend/pom.xml compile
Exit Code: 0
Build Status: BUILD SUCCESS
Output:
[INFO] --- compiler:3.15.0:compile (default-compile) @ core-backend ---
[INFO] Compiling 7 source files with javac [debug parameters release 21] to target\classes
[INFO] BUILD SUCCESS
```

### 3.2 Automated Test Execution (Unit, Context & Modulith Architecture)
```bash
Command: mvn -f services/core-backend/pom.xml test
Exit Code: 0
Build Status: BUILD SUCCESS
Execution Time: 32.111s
Total Tests: 4
Failures: 0
Errors: 0
Skipped: 0
Output:
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.nexarats.academy.common.web.HealthCheckControllerTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.432 s -- in com.nexarats.academy.common.web.HealthCheckControllerTests
[INFO] Running com.nexarats.academy.CoreBackendApplicationTests
2026-10-13 08:29:44.904 [main] [INFO ] [NONE] c.n.a.CoreBackendApplicationTests - Starting CoreBackendApplicationTests using Java 21.0.12.1
2026-10-13 08:29:44.905 [main] [DEBUG] [NONE] c.n.a.CoreBackendApplicationTests - Running with Spring Boot v4.1.1, Spring v7.0.9
2026-10-13 08:29:44.905 [main] [INFO ] [NONE] c.n.a.CoreBackendApplicationTests - The following 1 profile is active: "test"
2026-10-13 08:29:48.240 [SimpleAsyncTaskExecutor-1] [INFO ] [NONE] c.tngtech.archunit.core.PluginLoader - Detected Java version 21.0.12.1
2026-10-13 08:30:02.482 [main] [INFO ] [NONE] c.n.a.CoreBackendApplicationTests - Started CoreBackendApplicationTests in 17.935 seconds
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 19.07 s -- in com.nexarats.academy.CoreBackendApplicationTests
[INFO] Running com.nexarats.academy.ModularityTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.213 s -- in com.nexarats.academy.ModularityTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  32.111 s
[INFO] Finished at: 2026-10-13T08:30:05+05:30
[INFO] ------------------------------------------------------------------------
```

---

## 4. Infrastructure Validation Evidence

### 4.1 Docker Compose Configuration Syntax Check
```bash
Command: docker compose -f infrastructure/docker/docker-compose.yml config
Exit Code: 0
Services Configured:
  - postgres: postgres:16-alpine (Port 5432)
  - redis: redis:7-alpine (Port 6379)
  - minio: quay.io/minio/minio:latest (Ports 9000, 9001)
Volumes Configured:
  - nexarats_postgres_data
  - nexarats_redis_data
  - nexarats_minio_data
Networks Configured:
  - nexarats-network (bridge)
```

### 4.2 Docker Runtime Verification Status
- Docker daemon is installed and was responsive to CLI commands (`Docker version 29.7.2`).
- Image pull error recorded for `minio/minio:latest` on Docker Hub; resolved by updating `docker-compose.yml` to use `quay.io/minio/minio:latest`.
- Interrupted image pull triggered a transient Windows named pipe timeout (`dockerDesktopLinuxEngine/v1.55 500`). As required by execution rules, live Docker container startup is marked **PENDING DAEMON RESTART** rather than falsely claimed as running.

---

## 5. Git Baseline Status Evidence

- **Git Repository Initialized:** `git init` executed in project root.
- **`.gitignore` Rules Updated:**
  - `node_modules/`, `.pnpm-store/`, `.turbo/`, `dist/`
  - `.env`, `.env.local`, `.env.*.local`, `*.pem`, `*.key`
  - `target/`, `*.class`, `.m2/`
  - `.DS_Store`, `Thumbs.db`
- **Initial Commit Status:**
  - `git config user.name` and `git config user.email` are not configured in the developer's environment.
  - Per instruction ("Do not change Git author identity automatically. Do not overwrite existing user configuration. Document the issue without inventing a successful commit"), the initial commit is staged and ready for the developer to run once their identity is set:
    ```bash
    git config --global user.name "Your Name"
    git config --global user.email "your.email@example.com"
    git commit -m "chore(phase-1): enterprise monorepo foundation, java backend skeleton, and local dev infrastructure"
    ```
