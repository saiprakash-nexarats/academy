# Nexarats Academy — Phase 3 Database & API Report
## PostgreSQL 16.8 Schema Design, Spring Modulith REST APIs & Redis 7.4 Caching Architecture

**Status:** Phase 3 Complete  
**Date:** October 13, 2026  
**Execution Agent:** Gemini 3.8 Flash (High) — Antigravity  
**Authoritative Scope:** Phase 3 only  

---

## 1. PostgreSQL 16.8 Schema Design (Flyway V7)

Database schema evolution is managed via Flyway with `spring.jpa.hibernate.ddl-auto=validate` strictly enforced. Migration `V7__create_programs_schema.sql` creates normalized tables for program categories and catalog programs.

### 1.1 `program_categories` Table

```sql
CREATE TABLE IF NOT EXISTS program_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_program_categories_slug ON program_categories(slug);
CREATE INDEX IF NOT EXISTS idx_program_categories_active ON program_categories(is_active, display_order);
```

### 1.2 `programs` Table

```sql
CREATE TABLE IF NOT EXISTS programs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    category_id UUID NOT NULL REFERENCES program_categories(id) ON DELETE RESTRICT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    short_description VARCHAR(500) NOT NULL,
    detailed_description TEXT,
    image_asset_key VARCHAR(100) NOT NULL,
    duration_value INTEGER NOT NULL,
    duration_unit VARCHAR(20) NOT NULL DEFAULT 'MONTHS',
    duration_label VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    badge VARCHAR(50),
    rating NUMERIC(3, 2) NOT NULL DEFAULT 4.80,
    curriculum_overview TEXT,
    career_outcomes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    published_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_programs_duration_val CHECK (duration_value > 0),
    CONSTRAINT chk_programs_rating CHECK (rating >= 1.0 AND rating <= 5.0),
    CONSTRAINT chk_programs_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_programs_status_featured_order ON programs(status, is_featured, display_order);
CREATE INDEX IF NOT EXISTS idx_programs_category_id ON programs(category_id);
CREATE INDEX IF NOT EXISTS idx_programs_slug ON programs(slug);
CREATE INDEX IF NOT EXISTS idx_programs_duration ON programs(duration_value, duration_unit);
CREATE INDEX IF NOT EXISTS idx_programs_tenant_id ON programs(tenant_id);
```

### 1.3 Least-Privilege Role Grants

In compliance with Phase 2's Row-Level Security baseline (`V6`), table access was granted to the unprivileged application role:

```sql
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'academy_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE program_categories, programs TO academy_app_user;
    END IF;
END
$$;
```

---

## 2. Idempotent Data Seed Mapping

The original frontend mock catalog definitions in `apps/web/src/data/programs.ts` were deterministically mapped into PostgreSQL. All inserts employ `ON CONFLICT (slug) DO NOTHING` to guarantee idempotency across multiple startup or test cycles.

### 2.1 Program Categories Seed

| Category Slug | Name | Display Order | Description |
|---|---|---|---|
| `software-engineering` | Software Engineering | 1 | Modern web, backend, and full stack systems engineering |
| `artificial-intelligence` | Artificial Intelligence | 2 | Data science, machine learning models, and Generative AI systems |
| `design` | Design | 3 | User experience, product design, prototyping, and modern interaction systems |
| `marketing` | Marketing | 4 | Growth strategy, performance marketing, brand expansion, and tech PM |

### 2.2 Programs Seed

| Slug | Title | Category | Duration | Level | Rating | Featured | Badge | Asset Key |
|---|---|---|---|---|---|---|---|---|
| `full-stack` | Full Stack Development | Software Engineering | 6 Months | Beginner to Advanced | 4.90 | Yes | Bestseller | `course-fullstack` |
| `data-science` | Data Science & AI | Artificial Intelligence | 6 Months | Beginner to Advanced | 4.85 | Yes | None | `course-datascience` |
| `ui-ux` | UI/UX Design | Design | 6 Months | Beginner to Advanced | 4.92 | Yes | None | `course-uiux` |
| `digital-marketing` | Digital Marketing | Marketing | 4 Months | Beginner to Advanced | 4.80 | Yes | None | `course-marketing` |
| `cloud-devops` | Cloud & DevOps Engineering | Software Engineering | 5 Months | Intermediate to Advanced | 4.88 | No | High Demand | `course-fullstack` |
| `mobile-app` | Mobile App Development | Software Engineering | 4 Months | Beginner to Intermediate | 4.82 | No | None | `course-uiux` |
| `gen-ai-engineers` | Generative AI & LLM Systems | Artificial Intelligence | 4 Months | Intermediate to Advanced | 4.96 | No | Trending | `course-datascience` |
| `product-management` | Tech Product Management | Marketing | 4 Months | Beginner to Advanced | 4.79 | No | None | `course-marketing` |

---

## 3. REST API Specification

### 3.1 Canonical & Compatibility Route Architecture

The platform architecture blueprints designated `/api/v1/academic/programs` as the authoritative canonical path. To maintain compatibility with existing client references, Spring Boot controllers simultaneously map `/api/v1/programs` and `/api/v1/academic/programs` to the same underlying service.

```java
@RestController
@RequestMapping({"/api/v1/academic/programs", "/api/v1/programs"})
public class ProgramCatalogController { ... }

@RestController
@RequestMapping({"/api/v1/academic/program-categories", "/api/v1/program-categories"})
public class ProgramCategoryController { ... }
```

### 3.2 Endpoint Capabilities

#### `GET /api/v1/academic/programs` (and `/api/v1/programs`)
- **Query Parameters:**
  - `category` (string): Filter by category slug or category name (e.g., `software-engineering`, `all`).
  - `duration` (integer): Filter by duration in months (e.g., `4`, `5`, `6`).
  - `featured` (boolean): Filter by homepage featured flag (`true` / `false`).
  - `search` / `q` (string, max 100 chars): Case-insensitive keyword search matching title, short description, and detailed description.
  - `page` (integer, default 0, min 0): Zero-based page number.
  - `size` (integer, default 20, min 1, max 100): Page limit to prevent unbounded memory usage.
  - `sort` (string, default "default"): Sorting strategy (`default` = displayOrder ASC; `rating` = rating DESC; `title` = title ASC).
- **Responses:**
  - `200 OK`: `PaginatedResponseDto<ProgramSummaryDto>` with items list, total count, page, size, and totalPages.
  - `400 Bad Request`: Returned when search query exceeds 100 characters (RFC 7807 `ProblemDetail`).

#### `GET /api/v1/academic/programs/{slug}` (and `/api/v1/programs/{slug}`)
- **Path Parameter:** `slug` (string, required): Unique public program slug. Also supports direct UUID lookup as a graceful fallback.
- **Responses:**
  - `200 OK`: Full `ProgramDetailDto` containing curriculum overview, career outcomes, duration, badge, and published timestamp.
  - `404 Not Found`: Returned when slug is nonexistent or unpublished (RFC 7807 `ProblemDetail`).

#### `GET /api/v1/academic/program-categories` (and `/api/v1/program-categories`)
- **Responses:**
  - `200 OK`: Array of `ProgramCategoryDto` sorted by displayOrder ASC.

---

## 4. Redis 7.4 Caching Architecture

Caching is implemented in `ProgramCatalogCacheService` utilizing Spring Data Redis (`StringRedisTemplate`) and Jackson `ObjectMapper` configured with `JavaTimeModule` for ISO-8601 timestamps.

### 4.1 Cache Keys & TTL Policy

| Target | Cache Key Pattern | TTL | Invalidation Trigger |
|---|---|---|---|
| Active Categories | `catalog:categories:all` | 1 Hour (3600s) | Category updates / seed execution |
| Program Detail | `catalog:program:{slug}` | 1 Hour (3600s) | Program record update or unpublishing |
| Filtered Program Lists | `catalog:list:cat={c}:dur={d}:feat={f}:q={q}:p={p}:s={s}:sort={sort}` | 1 Hour (3600s) | Program updates or publication status change |

### 4.2 Security & Operational Guarantees

1. **Namespace Isolation:** All catalog cache keys strictly use the `catalog:*` prefix. Eviction routines never affect Phase 2 session tokens (`session:active:*`), refresh tokens (`auth:refresh:*`), or rate-limiting counters (`ratelimit:*`).
2. **Safe Public Payloads:** Only sanitised public DTOs (`ProgramSummaryDto`, `ProgramDetailDto`, `ProgramCategoryDto`) are stored in Redis. No internal user data, credentials, or private tenant records enter the cache.
3. **Draft Records Excluded:** The service queries `status = 'PUBLISHED'` only. Any draft or archived program is rejected prior to caching.
4. **Bounded Key Cardinality:** Search queries exceeding 200 characters bypass Redis storage to prevent memory exhaustion attacks.
5. **Resilient Fallback:** All Redis operations are wrapped in safe exception handlers. If Redis is down, calls log a warning and read directly from PostgreSQL without failing the user request.

---

## 5. Multi-Tenancy & Publication Scope

The catalog model supports both platform-wide and tenant-specific programs:
- Platform-wide programs have `tenant_id IS NULL` and are visible across all academy tenants.
- Tenant-owned custom programs have a valid `tenant_id` referencing `tenants(id)`.
- Public read endpoints enforce `status = 'PUBLISHED'`, preventing draft or internal records from leaking.
