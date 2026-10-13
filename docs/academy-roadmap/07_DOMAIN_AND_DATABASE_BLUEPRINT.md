# NEXARATS ACADEMY — DOMAIN & DATABASE BLUEPRINT
## Entity Models, Relationships, Multi-Tenant Data Isolation, and Lifecycle State Machines

- **Document ID:** `07_DOMAIN_AND_DATABASE_BLUEPRINT`
- **Execution Date:** 2026-10-13
- **Classification:** Domain-Driven Data Architecture Specification

---

## 1. Multi-Tenant Data Isolation Architecture

To ensure strict tenant boundary enforcement across B2B enterprise clients, academy branches, and international hubs, Nexarats Academy implements **Tenant Scoping with PostgreSQL Row-Level Security (RLS)**:

```text
Every Tenant Table contains a non-nullable tenant_id:
┌────────────────────────────────────────────────────────┐
│  table_name                                            │
├────────────────────────────────────────────────────────┤
│  id UUID PRIMARY KEY DEFAULT gen_random_uuid()         │
│  tenant_id UUID NOT NULL REFERENCES tenants(id)        │
│  ... entity columns ...                                │
│  created_at TIMESTAMPTZ DEFAULT clock_timestamp()      │
│  updated_at TIMESTAMPTZ DEFAULT clock_timestamp()      │
└────────────────────────────────────────────────────────┘

PostgreSQL Row-Level Security Policy:
ALTER TABLE {table_name} ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_policy ON {table_name}
  USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid);
```

- When an HTTP request enters the Spring Boot application, the `TenantContextFilter` extracts the tenant identifier from the JWT or subdomain and sets the PostgreSQL connection session variable: `SET LOCAL app.current_tenant_id = '...'`.
- Any query automatically filters out records belonging to other tenants, preventing data leakage even in the event of an application-level bug.

---

## 2. Core Domain Entities & Relational Schema

### 2.1 Multi-Tenancy & SaaS Governance Domain
- `tenants` (Core tenant registry)
  - `id`: UUID (PK)
  - `slug`: VARCHAR(64) UNIQUE (e.g., `acme-corp`, `nexarats-main`)
  - `name`: VARCHAR(255) NOT NULL
  - `tenant_type`: ENUM (`B2C_ACADEMY`, `B2B_ENTERPRISE`, `UNIVERSITY_PARTNER`)
  - `custom_domain`: VARCHAR(255) NULL
  - `branding_config`: JSONB (custom logo, primary color, email templates)
  - `status`: ENUM (`PROVISIONING`, `ACTIVE`, `SUSPENDED`, `ARCHIVED`)
  - `created_at`, `updated_at`: TIMESTAMPTZ

- `organizations` & `branches` (Tenant hierarchical structure)
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `parent_org_id`: UUID NULL (FK -> `organizations.id` for nested tree)
  - `name`: VARCHAR(255) NOT NULL
  - `code`: VARCHAR(64) NOT NULL
  - `location_address`: JSONB

### 2.2 Identity, Users & RBAC Domain
- `users`
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `email`: VARCHAR(255) NOT NULL
  - `phone_number`: VARCHAR(32) NULL
  - `password_hash`: VARCHAR(255) NOT NULL (Argon2id)
  - `first_name`: VARCHAR(128) NOT NULL
  - `last_name`: VARCHAR(128) NOT NULL
  - `avatar_url`: VARCHAR(1024) NULL
  - `status`: ENUM (`INVITED`, `ACTIVE`, `LOCKED`, `DEACTIVATED`)
  - `mfa_enabled`: BOOLEAN DEFAULT FALSE
  - `mfa_secret`: VARCHAR(255) NULL
  - `sso_provider`: VARCHAR(64) NULL (`OKTA`, `AZURE_AD`, `GOOGLE`, `LOCAL`)
  - UNIQUE(`tenant_id`, `email`)

- `roles` & `permissions`
  - `id`: UUID (PK)
  - `code`: VARCHAR(64) NOT NULL (`ROLE_STUDENT`, `ROLE_MENTOR`, `ROLE_ACADEMY_ADMIN`, etc.)
  - `description`: TEXT
  - `permissions`: JSONB (Array of granular privileges: `course:read`, `grade:write`)

- `user_roles`
  - `user_id`: UUID (FK -> `users.id`)
  - `role_id`: UUID (FK -> `roles.id`)
  - `organization_id`: UUID NULL (FK -> `organizations.id`)
  - PRIMARY KEY(`user_id`, `role_id`)

### 2.3 Curriculum, Programs & Courses Domain
- `programs` (Flagship career programs e.g. "Full Stack Development (6 Months)")
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `title`: VARCHAR(255) NOT NULL
  - `slug`: VARCHAR(255) NOT NULL
  - `description`: TEXT NOT NULL
  - `duration_weeks`: INT NOT NULL
  - `level`: ENUM (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `ALL_LEVELS`)
  - `job_assurance_eligible`: BOOLEAN DEFAULT TRUE
  - `banner_image_url`: VARCHAR(1024)
  - `status`: ENUM (`DRAFT`, `PUBLISHED`, `ARCHIVED`)
  - UNIQUE(`tenant_id`, `slug`)

- `courses` (Modular courses e.g. "Advanced React & Architecture")
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `program_id`: UUID NULL (FK -> `programs.id`)
  - `title`: VARCHAR(255) NOT NULL
  - `slug`: VARCHAR(255) NOT NULL
  - `category`: VARCHAR(128) NOT NULL
  - `level`: ENUM (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`)
  - `duration_hours`: INT NOT NULL
  - `status`: ENUM (`DRAFT`, `PUBLISHED`, `ARCHIVED`)
  - `meta_skills`: JSONB (Array of skill tags)

- `curriculum_modules` & `lessons`
  - `id`: UUID (PK)
  - `course_id`: UUID (FK -> `courses.id`)
  - `sequence_order`: INT NOT NULL
  - `title`: VARCHAR(255) NOT NULL
  - `lesson_type`: ENUM (`VIDEO`, `CODING_LAB`, `READING`, `QUIZ`, `LIVE_SESSION`)
  - `content_payload`: JSONB (Video URL, HLS manifest, markdown text, initial code template)
  - `prerequisite_lesson_id`: UUID NULL

### 2.4 Batches, Cohorts & Schedules Domain
- `cohort_batches`
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `program_id`: UUID (FK -> `programs.id`)
  - `batch_code`: VARCHAR(64) NOT NULL (e.g. `FS-2026-Q1-A`)
  - `start_date`: DATE NOT NULL
  - `end_date`: DATE NOT NULL
  - `max_student_capacity`: INT DEFAULT 100
  - `lead_instructor_id`: UUID (FK -> `users.id`)
  - `status`: ENUM (`UPCOMING`, `ACTIVE`, `COMPLETED`, `CANCELLED`)

- `live_class_sessions`
  - `id`: UUID (PK)
  - `batch_id`: UUID (FK -> `cohort_batches.id`)
  - `instructor_id`: UUID (FK -> `users.id`)
  - `title`: VARCHAR(255) NOT NULL
  - `scheduled_start_time`: TIMESTAMPTZ NOT NULL
  - `duration_minutes`: INT NOT NULL
  - `webrtc_room_id`: VARCHAR(128) NOT NULL (LiveKit room identifier)
  - `recording_hls_url`: VARCHAR(1024) NULL
  - `status`: ENUM (`SCHEDULED`, `LIVE`, `ENDED`, `RECORDING_READY`)

- `live_session_attendance`
  - `session_id`: UUID (FK -> `live_class_sessions.id`)
  - `student_id`: UUID (FK -> `users.id`)
  - `joined_at`: TIMESTAMPTZ NOT NULL
  - `left_at`: TIMESTAMPTZ NULL
  - `duration_seconds`: INT DEFAULT 0
  - `engagement_score`: INT DEFAULT 100
  - PRIMARY KEY(`session_id`, `student_id`)

### 2.5 Enrollments & Academic Progress Domain
- `student_enrollments`
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `student_id`: UUID (FK -> `users.id`)
  - `program_id`: UUID NULL (FK -> `programs.id`)
  - `course_id`: UUID NULL (FK -> `courses.id`)
  - `batch_id`: UUID NULL (FK -> `cohort_batches.id`)
  - `enrollment_type`: ENUM (`DIRECT_B2C`, `CORPORATE_SPONSORED`, `SCHOLARSHIP`)
  - `progress_percent`: DECIMAL(5,2) DEFAULT 0.00
  - `status`: ENUM (`ENROLLED`, `IN_PROGRESS`, `COMPLETED`, `PAUSED`, `DROPPED_OUT`)
  - `enrolled_at`: TIMESTAMPTZ DEFAULT clock_timestamp()

- `lesson_progress`
  - `id`: UUID (PK)
  - `enrollment_id`: UUID (FK -> `student_enrollments.id`)
  - `lesson_id`: UUID (FK -> `lessons.id`)
  - `completed`: BOOLEAN DEFAULT FALSE
  - `watch_time_seconds`: INT DEFAULT 0
  - `last_position_seconds`: INT DEFAULT 0
  - `completed_at`: TIMESTAMPTZ NULL
  - UNIQUE(`enrollment_id`, `lesson_id`)

### 2.6 Mentorship & 1:1 Booking Domain
- `mentor_profiles`
  - `id`: UUID (PK)
  - `user_id`: UUID (FK -> `users.id`)
  - `current_company`: VARCHAR(128) NOT NULL
  - `current_role`: VARCHAR(128) NOT NULL
  - `years_of_experience`: INT NOT NULL
  - `specializations`: JSONB (Array of string skills)
  - `bio`: TEXT NOT NULL
  - `rating_average`: DECIMAL(3,2) DEFAULT 5.00
  - `total_sessions_completed`: INT DEFAULT 0
  - `hourly_compensation_rate`: DECIMAL(10,2)

- `mentor_availability_slots`
  - `id`: UUID (PK)
  - `mentor_id`: UUID (FK -> `mentor_profiles.id`)
  - `slot_start_time`: TIMESTAMPTZ NOT NULL
  - `slot_end_time`: TIMESTAMPTZ NOT NULL
  - `is_booked`: BOOLEAN DEFAULT FALSE

- `mentorship_sessions`
  - `id`: UUID (PK)
  - `mentor_id`: UUID (FK -> `mentor_profiles.id`)
  - `student_id`: UUID (FK -> `users.id`)
  - `slot_id`: UUID (FK -> `mentor_availability_slots.id`)
  - `topic`: VARCHAR(255) NOT NULL
  - `student_notes`: TEXT NULL
  - `mentor_feedback_notes`: TEXT NULL
  - `webrtc_room_url`: VARCHAR(1024) NOT NULL
  - `status`: ENUM (`SCHEDULED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `NO_SHOW`, `CANCELLED`)

### 2.7 Assessments, Exams & Coding Sandboxes Domain
- `assessments`
  - `id`: UUID (PK)
  - `course_id`: UUID (FK -> `courses.id`)
  - `title`: VARCHAR(255) NOT NULL
  - `assessment_type`: ENUM (`QUIZ`, `CODING_CHALLENGE`, `CAPSTONE_DEFENSE`, `FINAL_EXAM`)
  - `passing_score_percent`: DECIMAL(5,2) DEFAULT 70.00
  - `time_limit_minutes`: INT DEFAULT 60
  - `proctoring_required`: BOOLEAN DEFAULT FALSE

- `coding_submissions`
  - `id`: UUID (PK)
  - `assessment_id`: UUID (FK -> `assessments.id`)
  - `student_id`: UUID (FK -> `users.id`)
  - `code_repository_url`: VARCHAR(1024) NULL
  - `source_code_snapshot`: TEXT NOT NULL
  - `programming_language`: VARCHAR(32) NOT NULL
  - `test_cases_passed`: INT NOT NULL
  - `total_test_cases`: INT NOT NULL
  - `execution_time_ms`: INT NOT NULL
  - `grade_score`: DECIMAL(5,2) NOT NULL
  - `status`: ENUM (`QUEUED`, `RUNNING`, `PASSED`, `FAILED`, `MANUAL_REVIEW_REQUIRED`)

### 2.8 Certificates & Verification Domain
- `issued_certificates`
  - `id`: UUID (PK)
  - `certificate_code`: VARCHAR(64) UNIQUE NOT NULL (e.g. `NX-2026-9481`)
  - `student_id`: UUID (FK -> `users.id`)
  - `program_id`: UUID NULL (FK -> `programs.id`)
  - `course_id`: UUID NULL (FK -> `courses.id`)
  - `final_grade_percent`: DECIMAL(5,2) NOT NULL
  - `issue_date`: DATE NOT NULL
  - `pdf_storage_url`: VARCHAR(1024) NOT NULL
  - `cryptographic_signature`: TEXT NOT NULL (SHA-256 + RSA Private Key sign)
  - `verification_public_url`: VARCHAR(512) NOT NULL
  - `status`: ENUM (`ACTIVE`, `REVOKED`)

### 2.9 Finance, Invoices & Payments Domain
- `invoices`
  - `id`: UUID (PK)
  - `tenant_id`: UUID (FK -> `tenants.id`)
  - `invoice_number`: VARCHAR(64) UNIQUE NOT NULL
  - `user_id`: UUID (FK -> `users.id`)
  - `total_amount`: DECIMAL(12,2) NOT NULL
  - `tax_amount`: DECIMAL(12,2) DEFAULT 0.00
  - `currency`: VARCHAR(3) DEFAULT 'USD'
  - `status`: ENUM (`DRAFT`, `ISSUED`, `PAID`, `PARTIALLY_PAID`, `OVERDUE`, `REFUNDED`)
  - `due_date`: DATE NOT NULL

- `payment_transactions`
  - `id`: UUID (PK)
  - `invoice_id`: UUID (FK -> `invoices.id`)
  - `payment_gateway`: ENUM (`STRIPE`, `RAZORPAY`, `BANK_TRANSFER`, `CORPORATE_PO`)
  - `gateway_transaction_id`: VARCHAR(255) NOT NULL
  - `amount`: DECIMAL(12,2) NOT NULL
  - `status`: ENUM (`INITIATED`, `SUCCESS`, `FAILED`, `REFUNDED`)
  - `paid_at`: TIMESTAMPTZ NULL

---

## 3. Lifecycle State Machines

### 3.1 Student Admission & Enrollment State Machine
```text
[INQUIRY_RECEIVED] ──(Counselor Contacted)──► [COUNSELING_SCHEDULED]
       │
(Passed Screening)
       ▼
[ADMISSION_OFFERED] ──(Payment Made / ISA Signed)──► [ENROLLED]
       │                                                │
(Declined / Timeout)                              (Cohort Starts)
       ▼                                                ▼
  [ABANDONED]                                     [ACTIVE_LEARNER]
                                                        │
                                            (Failed Assessments)
                                                        ▼
                                                  [ACADEMIC_PROBATION]
                                                        │
                                                  (Completed 100%)
                                                        ▼
                                                  [GRADUATED_CERTIFIED]
```

### 3.2 Live Class Session State Machine
```text
[SCHEDULED] ──(Instructor Starts Room)──► [LIVE_STREAMING]
                                                │
                                    (Instructor Concludes)
                                                ▼
                                         [PROCESSING_RECORDING]
                                                │
                                    (HLS Transcoding Complete)
                                                ▼
                                         [ARCHIVED_PUBLISHED]
```

### 3.3 Assessment Submission State Machine
```text
[DRAFT] ──(Student Submits)──► [TEST_SUITE_QUEUED]
                                     │
                             (Judge0 Container Runs)
                                     ▼
                               [AUTOMATED_SCORED]
                                     │
                             (Requires Capstone Review?)
                             ├── YES ──► [MENTOR_REVIEW_PENDING] ──► [FINAL_GRADED]
                             └── NO  ──────────────────────────────► [FINAL_GRADED]
```

---

## 4. Cross-Module Domain Event Catalog

| Event Name | Source Module | Payload Attributes | Consumer Modules & Actions Triggered |
| :--- | :--- | :--- | :--- |
| `LeadInquirySubmittedEvent` | `admissions` | `leadId`, `email`, `phone`, `programId` | `notification`: Sends auto-reply email; `crm`: Assigns next available counselor. |
| `PaymentCapturedEvent` | `finance` | `invoiceId`, `userId`, `amount`, `currency` | `enrollment`: Activates `student_enrollments`; `notification`: Dispatches receipt invoice PDF. |
| `LiveClassTerminatedEvent` | `cohort` | `sessionId`, `webrtcRoomId`, `duration` | `telemetry`: Computes attendance logs; `media`: Spawns async recording transcode worker. |
| `AssignmentSubmittedEvent`| `assessment` | `submissionId`, `studentId`, `repoUrl` | `sandbox`: Dispatches tests to Judge0 queue; `mentor`: Notifies TA if manual review is needed. |
| `CourseCompletedEvent` | `academic` | `studentId`, `courseId`, `finalScore` | `credential`: Emits cryptographically signed digital certificate; `alumni`: Enters student into Career Placement CRM. |
| `ComplianceOverdueEvent` | `corporate` | `employeeId`, `tenantId`, `courseId` | `notification`: Escalates overdue warning to department manager and HR administrator. |
