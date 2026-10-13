# NEXARATS ACADEMY — RISKS, DEPENDENCIES & RELEASE GATES
## Enterprise Risk Register, Technical Dependency Graph, Quality Metrics, and Deployment Gates

- **Document ID:** `12_RISKS_DEPENDENCIES_AND_RELEASE_GATES`
- **Execution Date:** 2026-10-13
- **Classification:** Risk Governance & Release Quality Specification

---

## 1. Enterprise Risk Register & Mitigation Matrix

| Risk ID | Category | Risk Description | Probability | Impact | Mitigation Strategy | Contingency Plan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **RSK-01** | Technical | Breaking changes in existing React 19 / Vite setup during monorepo transition. | Medium | High | Isolate existing code into `apps/web` with zero dependency version upgrades in early phases. Verify builds with CI checks prior to merging. | Immediate rollback to Phase 0 standalone Git commit. |
| **RSK-02** | Security | Cross-tenant data leakage in multi-tenant database queries. | Low | Critical | Enforce PostgreSQL Row-Level Security (RLS) at database engine level. `tenant_id` populated automatically per session; zero trust in application code alone. | Automated security test suite simulating cross-tenant SQL queries runs on every pull request. |
| **RSK-03** | Architectural | LiveKit WebRTC SFU server bottlenecks during large live cohort lectures (1,000+ students). | Medium | High | Deploy LiveKit in clustered mesh topology with dedicated media relay nodes; implement automated audio-only fallback for low-bandwidth students. | Cloud-managed LiveKit Cloud failover cluster with autoscaling node pools. |
| **RSK-04** | Operational | Third-party payment gateway outages (Stripe / Razorpay downtime during peak enrollment). | Medium | High | Dual payment gateway architecture: route transactions dynamically between Stripe and Razorpay; support offline bank wire reconciliation. | Queue payment requests in Redis with automated retry when gateway recovers. |
| **RSK-05** | Technical | Code sandbox security vulnerabilities (arbitrary code execution escaping Docker containers in Judge0). | Low | Critical | Isolate code execution inside gVisor / Firecracker microVMs with disabled network interfaces, read-only root filesystems, and strict CPU/memory cgroups. | Automated killing of microVMs exceeding 5-second CPU limit; alerts sent to SecOps. |
| **RSK-06** | Financial | LLM API token cost spikes during high-volume AI tutor usage. | High | Medium | Implement aggressive semantic caching in Redis for common syllabus questions; enforce daily per-student token quotas (50 queries/day). | Fallback to self-hosted quantized local LLMs (e.g. Llama 3 8B) for non-critical queries. |
| **RSK-07** | Compliance | Non-compliance with GDPR / FERPA student privacy regulations leading to regulatory penalties. | Low | Critical | Data minimization policy; self-service data export/erasure pipelines; encrypted column-level storage for PII; signed Data Protection Agreements (DPAs). | Dedicated Data Protection Officer (DPO) on-call; 72-hour incident response SLA. |
| **RSK-08** | Performance | Frontend bundle bloat from monorepo expansion degrading Core Web Vitals below 90. | High | Medium | Enforce route-based code-splitting using `React.lazy()` and dynamic imports; enforce 200 KB initial JS bundle budget in Vite build CI. | Automated CI pipeline fails build if initial chunk size exceeds 250 KB gzip. |

---

## 2. Technical Dependency Graph

```text
[Phase 0: Baseline Audit]
       │
       ▼
[Phase 1: Monorepo Scaffold]
       │
       ├────────────────────────────────────────┬────────────────────────────────────────┐
       ▼                                        ▼                                        ▼
[Phase 2: Auth & Tenant Model]          [Phase 16: LiveKit Cluster]              [Phase 46: FastAPI AI Setup]
       │                                        │                                        │
       ├─────────────────────────┐              ├─────────────────────────┐              ├─────────────────────────┐
       ▼                         ▼              ▼                         ▼              ▼                         ▼
[Phase 3-4: Curricula]     [Phase 5: CRM]  [Phase 17: Live Rooms]    [Phase 18: Record]  [Phase 47: RAG Pipeline] [Phase 48: Tutor]
       │                         │              │                         │              │                         │
       ├─────────────────────────┘              ├─────────────────────────┘              └────────────┬────────────┘
       ▼                                        ▼                                                     │
[Phase 6-7: Enroll & Pay]                 [Phase 19-21: Mentors]                                      │
       │                                        │                                                     │
       ▼                                        ▼                                                     │
[Phase 8-11: LMS Player] ◄──────────────────────┘                                                     │
       │                                                                                              │
       ▼                                                                                              │
[Phase 12: MILESTONE 1 MVP GATE] ◄────────────────────────────────────────────────────────────────────┤
       │                                                                                              │
       ├────────────────────────────────────────┬────────────────────────────────────────┐            │
       ▼                                        ▼                                        ▼            ▼
[Phase 22-25: Sandboxes & Exams]        [Phase 30-37: Corporate B2B]             [Phase 39-45: Placement] [Phase 49-52: AI ML]
       │                                        │                                        │            │
       ▼                                        ▼                                        ▼            │
[Phase 26-29: Verified Credentials]     [Phase 38: MILESTONE 4 GATE]             [Phase 45: Job Assurance]   │
       │                                        │                                        │            │
       └────────────────────────────────────────┼────────────────────────────────────────┴────────────┘
                                                │
                                                ▼
                                  [Phase 53-59: Native Mobile Apps]
                                                │
                                                ▼
                                  [Phase 60-65: Integrations & BI]
                                                │
                                                ▼
                                  [Phase 66-69: Production GA Fleet]
```

---

## 3. Quality Metrics & CI/CD Release Gates

Every phase must pass through 5 automated release gates prior to promotion:

```text
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     GATE 1:     │    │     GATE 2:     │    │     GATE 3:     │    │     GATE 4:     │    │     GATE 5:     │
│   Code Hygiene  │───►│   Test Coverage │───►│   Security SAST │───►│  Perf & Vitals  │───►│ Architecture QA │
│ Lint & Strict TS│    │  Unit & Integr. │    │  Trivy & Secrets│    │ Bundle & Latency│    │  Principal Sign │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 3.1 Gate Criteria Details
1. **Gate 1: Code Hygiene & Compilation:**
   - Oxlint: Zero warnings, zero errors.
   - TypeScript: Zero errors under `strict: true` across all frontend and mobile workspaces.
   - Spring Boot: Clean Gradle/Maven build with checkstyle verification.
2. **Gate 2: Automated Test Coverage:**
   - Minimum **80% line coverage** on all business logic services in Spring Boot and FastAPI.
   - Minimum **70% coverage** on frontend utility functions and state reducers.
   - 100% of critical user journeys validated via Playwright end-to-end tests.
3. **Gate 3: Security & Static Analysis (SAST/DAST):**
   - Zero High or Critical CVEs in dependencies (Trivy / Snyk).
   - Zero exposed secrets or hardcoded tokens (Gitleaks).
   - OWASP Top 10 automated security scanner pass.
4. **Gate 4: Performance Budgets & Core Web Vitals:**
   - Web frontend initial JavaScript bundle < 200 KB gzip.
   - Public landing page Lighthouse performance score >= 90.
   - API endpoints P95 response time < 150ms under baseline load.
5. **Gate 5: Architectural Sign-Off:**
   - Peer review and formal sign-off by Principal Software Architect and QA Lead.

---

## 4. Rollback & Deployment Strategy

### 4.1 Blue/Green Zero-Downtime Deployment
- Production workloads run on two identical production clusters (Blue and Green).
- New releases deploy to Green cluster; automated synthetic health probes verify API endpoints and database migrations.
- Router (NGINX / Cloudflare) shifts 10% traffic (Canary) for 15 minutes; if error rates remain below 0.01%, 100% traffic shifts to Green.
- Blue cluster remains standing for 2 hours to enable instant 1-click traffic rollback if anomalies emerge.

### 4.2 Database Migration Rollback Strategy
- Every Flyway database migration script must include a paired down-migration plan (`V__*.sql` and `U__*.sql`).
- All schema modifications follow the **Expand and Contract pattern**:
  - *Expand:* Add new column/table as nullable or with defaults; application writes to both old and new columns.
  - *Migrate:* Background job backfills legacy records.
  - *Contract:* Remove legacy column only in the subsequent release, guaranteeing backward compatibility during deployment.
