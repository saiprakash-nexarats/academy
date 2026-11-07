# NEXARATS ACADEMY — ROADMAP PHASE ALIGNMENT & RECONCILIATION

**Document ID:** `PHASE_ALIGNMENT`  
**Classification:** Enterprise Architecture & Roadmap Governance  
**Project:** Nexarats Academy  
**Master Roadmap Reference:** `docs/academy-roadmap/11_COMPLETE_70_PHASE_ROADMAP.md` (Phases 0–69)  
**Baseline Audited Commit:** `268e666`  
**Recovery Baseline:** Phase 6 Recovery  
**Status:** ACTIVE RECONCILIATION  

---

## 1. Executive Summary & Purpose

During the independent architecture, security, and QA audit of Phase 6, a significant roadmap misalignment was identified between the **Master 70-Phase Implementation Roadmap** (`11_COMPLETE_70_PHASE_ROADMAP.md`) and the **Implementation Reports** (Reports 36–43).

Specifically:
- In the Git repository and implementation reports, "Phase 6" implemented the **Student LMS Dashboard** and the **Modular Curriculum Video Player with Progress Tracking**.
- In the master enterprise architecture blueprint, **Phase 6** was defined as **Student Profile & Academic Onboarding** (`student_profiles`, `legal_agreements`, avatar S3 upload, ClamAV anti-malware scanning, terms e-signing), while the LMS Dashboard and Video Player were specified as **Phase 10** and **Phase 11**.
- In addition, parts of Roadmap Phase 7 (*Tuition Billing & Installments*), Phase 8 (*Cohort Batch Scheduler & Timetable Engine*), and Phase 9 (*Student Enrollment & Batch Allocation*) were partially collapsed into Git Phase 5 (`enrollments` and `payments` tables) without delivering the full timetable conflict detection engine or multi-step onboarding wizard.
- Implementation Report 43 prematurely proposed jumping directly to Phase 7 as "Capstone Assignment Submission, Code Execution Sandbox & Certificate Generation" (which corresponds to Milestone 3 / Roadmap Phases 22–29).

This document establishes the official bidirectional mapping, documents missing deliverables, enforces architectural prerequisites, and preserves the complete 70-phase master roadmap without rewriting historical commits.

---

## 2. Bidirectional Phase Mapping Matrix

The table below reconciles Git implementation phases with the master 70-phase specification:

| Git / Report Phase | Implemented Domain | Master Roadmap Phase | Master Roadmap Title | Delivery Milestone | Reconciliation Status |
|:---|:---|:---|:---|:---|:---|
| **Phase 0** | Baseline Preservation & Gap Analysis | **Phase 0** | Existing Project Audit & Expansion Blueprint | Milestone 1 | **Verified Complete** |
| **Phase 1** | Monorepo Structure & Tooling | **Phase 1** | Enterprise Repository Monorepo Scaffold | Milestone 1 | **Verified Complete** |
| **Phase 2** | Auth, JWT, Roles, Tenants | **Phase 2** | Core Identity, Multi-Tenant Architecture & Auth | Milestone 1 | **Verified Complete** |
| **Phase 3** | Public Catalog & Dynamic Programs | **Phase 3** | Public Catalog & Dynamic Programs API | Milestone 1 | **Verified Complete** |
| **Phase 4** | Modular Course Catalog & Syllabus | **Phase 4** | Modular Course Catalog & Syllabus Engine | Milestone 1 | **Verified Complete** |
| **Phase 5** | Admissions Leads & Basic Checkout | **Phase 5** | Admissions Funnel & CRM Lead Pipeline | Milestone 1 | **Implemented (Partial Scope)** |
| *Merged into P5* | Payment Gateway & Transaction Logs | **Phase 7** | Tuition Billing, Installments & Payment Gateway | Milestone 1 | **Implemented (Basic Checkout only; Installments pending)** |
| *Merged into P5* | Basic Cohort Model | **Phase 8** | Cohort Batch Scheduler & Timetable Engine | Milestone 1 | **Partial (Timetable & Conflict Engine pending)** |
| *Merged into P5* | Student Course Enrollment State | **Phase 9** | Student Enrollment & Batch Allocation | Milestone 1 | **Partially Complete (Waitlist queues & cohort countdown pending)** |
| **Phase 6B** | Student Profile & Academic Onboarding | **Phase 6** | **Student Profile & Academic Onboarding** | Milestone 1 | **Verified Complete (Report 44)** |
| **Git Phase 6** | Student Learning Dashboard | **Phase 10** | Student LMS Dashboard (Core Portal Shell) | Milestone 1 | **Partially Complete (Streaks & announcements pending)** |
| **Git Phase 6** | Video Streaming & Watch Progress | **Phase 11** | Modular Curriculum Video Player & Progress | Milestone 1 | **Partially Complete (Video bookmarks & transcript drawer pending)** |
| **Blocked Gate**| Formal Milestone 1 Acceptance | **Phase 12** | End-to-End MVP Milestone Release Gate | Milestone 1 | **BLOCKED (Requires Phase 6 and prerequisites completion)** |
| *Proposed in R43* | Capstone Sandbox & Certificates | **Phase 22–29**| Coding Sandboxes, Proctored Exams & Credentials | Milestone 3 | **DEFERRED (Prerequisites Required)** |

---

## 3. Inventory of Missing Roadmap Deliverables

The following architectural deliverables from Milestone 1 (Phases 0–12) remain pending implementation before any Milestone 2 (Mentorship/WebRTC) or Milestone 3 (Coding Sandbox/Certificates) work can commence:

### Deliverable A: Student Profile & Academic Onboarding (Original Phase 6)
- **Status:** Skipped in Git; required for student lifecycle completion.
- **Frontend Requirements:**
  - Multi-step onboarding wizard at `/portal/student/onboarding`.
  - Profile setup form (bio, career goals, timezone, GitHub/LinkedIn profile URL validation).
  - Terms of Service & Code of Conduct digital agreement e-signing.
- **Backend Requirements:**
  - S3/MinIO pre-signed URL generator for avatar image uploads.
  - Profile management service (`StudentProfileService`).
  - Legal agreement acceptance audit trail service.
- **Database Requirements:**
  - Tables: `student_profiles`, `legal_agreements` (Flyway migration V11).
- **Security:**
  - MIME-type validation, file size limits, and ClamAV integration for avatar uploads.
  - Transition student status from `ENROLLED` to `ACTIVE_STUDENT` only after agreement acceptance.

### Deliverable B: Timetable Engine & Conflict Detection (Original Phase 8)
- **Status:** Basic `cohort_batches` table exists, but schedule planning and conflict detection were deferred.
- **Backend Requirements:**
  - Timetable scheduling engine validating lecture halls, mentor timeslots, and student availability.
  - Overlap and double-booking conflict detection algorithms.
- **Database Requirements:**
  - Table: `batch_schedules` with recurring weekly timeslot definitions.

### Deliverable C: Installment Billing & Invoicing (Original Phase 7 Enhancements)
- **Status:** Basic upfront tuition payment transaction logged, but scheduled recurring installments and PDF tax invoices are deferred.
- **Backend Requirements:**
  - Installment scheduler (`installment_schedules`).
  - Automated PDF receipt/invoice generator.

### Deliverable D: Waitlist Queue & Cohort Countdown (Original Phase 9 Enhancements)
- **Status:** Direct enrollment and program track cascading implemented, but waitlist queue for full cohorts and dashboard countdown are missing.
- **Backend Requirements:**
  - Waitlist queue entity (`enrollment_waitlists`).
  - Automatic seat vacancy promotion engine.
- **Frontend Requirements:**
  - Cohort start countdown card in student dashboard.

### Deliverable E: Learning Streaks & Announcements (Original Phase 10 Enhancements)
- **Status:** Dashboard shell, active courses, and lesson counters implemented, but learning streaks and announcements are missing.
- **Backend & Database Requirements:**
  - Table: `student_streaks`, `announcements`.
  - Daily streak calculation service based on learning activity logs.
- **Frontend Requirements:**
  - Learning streak flame counter widget and announcements banner in `/dashboard`.

### Deliverable F: Video Bookmarking & Transcript Drawer (Original Phase 11 Enhancements)
- **Status:** Video streaming, resume playback, keyboard shortcuts, and 80% viewing completion gate verified, but bookmarks and transcripts are missing.
- **Frontend & Database Requirements:**
  - Table: `lesson_bookmarks`, `lesson_transcripts`.
  - VideoPlayer bookmarking button and transcript search drawer.

---

## 4. Prerequisite Dependency Graph Before Capstone Work

```mermaid
flowchart TD
    P2["Phase 2: Auth & Tenant Model (DONE)"] --> P5["Phase 5: Admissions Funnel (PARTIAL)"]
    P4["Phase 4: Course Catalog (DONE)"] --> P8["Phase 8: Batch & Timetable Scheduler (PARTIAL)"]
    P5 --> P7["Phase 7: Billing & Payments (PARTIAL)"]
    P5 --> P6["Phase 6: Student Profile Onboarding (NOT STARTED)"]
    P7 --> P9["Phase 9: Enrollment & Batch Allocation (PARTIAL)"]
    P8 --> P9
    P9 --> P10["Phase 10: LMS Dashboard (PARTIAL)"]
    P10 --> P11["Phase 11: Video Player & Progress (PARTIAL)"]
    P6 --> P12["Phase 12: E2E MVP Milestone 1 Release Gate (BLOCKED)"]
    P11 --> P12
    P12 --> M2["Milestone 2: Mentorship & WebRTC (Phases 13-21)"]
    M2 --> M3["Milestone 3: Capstone, Sandboxes & Certs (Phases 22-29)"]

    style P6 fill:#ffcccc,stroke:#ff0000,stroke-width:2px;
    style P9 fill:#fff2cc,stroke:#d6b656,stroke-width:2px;
    style P10 fill:#fff2cc,stroke:#d6b656,stroke-width:2px;
    style P11 fill:#fff2cc,stroke:#d6b656,stroke-width:2px;
    style P12 fill:#f8cecc,stroke:#b85450,stroke-width:2px;
```

> [!CAUTION]
> **Architectural Guardrail:** Capstone assignment submission and automated code execution sandboxes belong to **Milestone 3 (Phases 22–29)**. Attempting to implement Capstone Sandboxes immediately following Phase 6 bypasses:
> 1. Student Academic Onboarding and Legal Agreements (Phase 6).
> 2. End-to-End MVP Milestone Release Gate (Phase 12).
> 3. Mentor assignment and review infrastructure (Phase 13).
> 4. Video transcoding infrastructure (Phase 14).
> 
> Proceeding without these foundations would leave certificates unbacked by legal identity and capstone submissions with no mentor grading workflows.

---

## 5. Official Phase Naming Convention Going Forward

To prevent future confusion between git branch labels and the 70-phase enterprise roadmap:
1. Every new phase must cite both its **Execution Phase Number** and its **Master Roadmap Phase ID** (e.g., `Phase 06 (Roadmap Phases 10-11)`).
2. The backlog must schedule **Phase 6B (Student Profile & Academic Onboarding)** before the Milestone 1 release gate.
3. No roadmap phase will be renumbered, deleted, or skipped without an explicit Architecture Decision Record (ADR).

