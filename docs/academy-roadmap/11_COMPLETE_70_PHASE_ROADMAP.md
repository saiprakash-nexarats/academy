# NEXARATS ACADEMY — COMPLETE 70-PHASE IMPLEMENTATION ROADMAP
## Comprehensive Master Roadmap from Phase 0 to Phase 69 with Milestones and Delivery Gates

- **Document ID:** `11_COMPLETE_70_PHASE_ROADMAP`
- **Execution Date:** 2026-10-13
- **Classification:** Master Enterprise Engineering Roadmap

---

## 1. Roadmap Architecture & Strategic Milestones

The 70 phases are organized across 9 sequential delivery milestones, ensuring value is released continuously rather than waiting for Phase 69.

```text
┌────────────────────────────────────────────────────────────────────────┐
│ MILESTONE 1: Core Foundation & Minimum Usable Student Learning MVP    │
│ Phases 0 – 12 | Release Target: Weeks 1 – 12                          │
│ Deliverable: Working End-to-End Student Admissions, Auth & Video LMS   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 2: Mentor Operations, 1:1 Booking & Live WebRTC Classrooms   │
│ Phases 13 – 21 | Release Target: Weeks 13 – 20                         │
│ Deliverable: Live Interactive Lectures, WebRTC, Class Transcoding      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 3: Coding Sandboxes, Proctored Exams & Verified Credentials  │
│ Phases 22 – 29 | Release Target: Weeks 21 – 28                         │
│ Deliverable: In-Browser Monaco IDE, Judge0 Labs, X.509 Certificates    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 4: Corporate B2B Training & Multi-Tenant Organization SaaS   │
│ Phases 30 – 38 | Release Target: Weeks 29 – 36                         │
│ Deliverable: Enterprise HR Portal, SCIM / SSO, Compliance Dashboards   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 5: Placement Crucible, Resume ATS Builder & Alumni Network   │
│ Phases 39 – 45 | Release Target: Weeks 37 – 42                         │
│ Deliverable: 500+ Partner Drives, Job Board, Mock System Defenses      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 6: AI-Native Syllabus RAG Tutor & Adaptive Learning Engine   │
│ Phases 46 – 52 | Release Target: Weeks 43 – 48                         │
│ Deliverable: FastAPI LangChain Tutor, Code Debugger, Diagnostic Tests  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 7: Cross-Platform Native Mobile Applications (iOS & Android) │
│ Phases 53 – 59 | Release Target: Weeks 49 – 54                         │
│ Deliverable: Flutter Native Apps, Offline Video Sync, Native WebRTC    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 8: Enterprise Integrations, Global Search & Advanced BI      │
│ Phases 60 – 65 | Release Target: Weeks 55 – 60                         │
│ Deliverable: OpenSearch, Workday/Degreed Connectors, Predictive Churn │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│ MILESTONE 9: Fleet Hardening, Global High Availability & Production GA  │
│ Phases 66 – 69 | Release Target: Weeks 61 – 64                         │
│ Deliverable: Multi-Region Failover, SOC2 Type II Audit, Production GA │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Phase-by-Phase Specification (Phases 0 – 69)

### Milestone 1: Core Foundation & Minimum Usable Student Learning MVP

#### Phase 0: Existing Project Audit, Baseline Preservation, and Enterprise Expansion Blueprint
- **Objective:** Audit existing codebase, preserve landing page, verify builds, document gap analysis, and produce complete 70-phase blueprints.
- **Dependencies:** None. Current workspace baseline.
- **Features:** 14 architecture blueprint documents, environment inventory, feature matrix, zero destructive modifications.
- **Frontend Requirements:** Run `oxlint` and `tsc -b && vite build` without errors.
- **Backend Requirements:** Inspect `server/` directory and document non-functional skeleton status.
- **Database Requirements:** Specify domain models and RLS isolation strategy without migrations.
- **Security:** Identify mock credential exposure; define OAuth2 roadmap.
- **Acceptance Criteria:** Zero modified existing source files; 14 blueprint docs generated in `docs/academy-roadmap/`.
- **Completion Evidence:** Passed build outputs, audit report signed off.

#### Phase 1: Enterprise Repository Monorepo Scaffold & Tooling Setup
- **Objective:** Establish clean monorepo structure (pnpm / Turborepo) housing existing web app, backend, and shared token packages without breaking current Vite setup.
- **Dependencies:** Phase 0.
- **Features:** Setup `apps/web` (existing Vite app), `apps/mobile` (Flutter), `services/core-backend` (Spring Boot), `services/ai-subsystem` (FastAPI), `packages/ui-tokens`.
- **Frontend Requirements:** Move current files to `apps/web` with all paths and aliases intact.
- **Backend Requirements:** Initialize Maven Spring Boot 4.1.1 + Spring Modulith 2.1.1 skeleton with Java 21 LTS.
- **Database Requirements:** Setup local Docker Compose for PostgreSQL 16 and Redis 7.
- **Security:** Setup `.env.example` templates, pre-commit gitleaks hooks.
- **Acceptance Criteria:** `apps/web` builds and runs identically to baseline.
- **Completion Evidence:** Clean `pnpm build` across all monorepo workspaces.

#### Phase 2: Core Identity, Multi-Tenant Architecture & Auth Service
- **Objective:** Implement real user authentication with JWT, refresh token rotation, and tenant context propagation.
- **Dependencies:** Phase 1.
- **Features:** OAuth2 resource server, RS256 token issuance, bcrypt/argon2 hashing, tenant scoping filters.
- **Frontend Requirements:** Connect existing `LoginModal` to real `/api/v1/auth/login` endpoint; store JWT in memory and refresh token in HttpOnly cookie.
- **Backend Requirements:** Spring Security (resolved by Spring Boot dependency configuration) authentication provider, user details service, tenant context interceptor.
- **Database Requirements:** Tables: `tenants`, `users`, `roles`, `permissions`, `user_roles`. Enable RLS.
- **Security:** Rate limiting on login attempts (Redis token bucket), CSRF protection.
- **Acceptance Criteria:** Valid credentials return signed JWT; invalid credentials return RFC 7807 error.
- **Completion Evidence:** Unit and integration tests passing for auth workflows.

#### Phase 3: Public Catalog & Dynamic Programs API
- **Objective:** Migrate static program data to dynamic PostgreSQL database and connect public pages.
- **Dependencies:** Phase 2.
- **Features:** REST endpoints for programs, categories, and duration filtering.
- **Frontend Requirements:** Update `ProgramsPage.tsx` and `PopularProgramsSection.tsx` to fetch data via TanStack Query with loading skeletons.
- **Backend Requirements:** `ProgramService`, `ProgramRepository` with caching in Redis.
- **Database Requirements:** Table: `programs`. Seed with existing data from `src/data/programs.ts`.
- **Security:** Public read-only endpoints with CDN cache headers.
- **Acceptance Criteria:** Programs catalog renders identical visuals backed by live PostgreSQL data.
- **Completion Evidence:** Database query logs matching API response payload.

#### Phase 4: Modular Course Catalog & Syllabus Engine
- **Objective:** Implement course library backend with hierarchical module and lesson structures.
- **Dependencies:** Phase 3.
- **Features:** Hierarchical course/module/lesson model, level filters, interactive syllabus previews.
- **Frontend Requirements:** Connect `CoursesPage.tsx` and syllabus modal to live API.
- **Backend Requirements:** `CourseService`, `CurriculumService` with published version controls.
- **Database Requirements:** Tables: `courses`, `curriculum_modules`, `lessons`. Seed from `src/data/courses.ts`.
- **Security:** Public lessons marked previewable; locked lessons require enrollment check.
- **Acceptance Criteria:** Course cards and syllabus modals render from database without UI regression.
- **Completion Evidence:** API test coverage for course search and category filtering.

#### Phase 5: Admissions Funnel, CRM Lead Pipeline & Counseling Booking
- **Objective:** Replace mock inquiry and enrollment forms with real lead persistence and counselor routing.
- **Dependencies:** Phase 2.
- **Features:** Lead capture, automatic counselor assignment, email notifications, counseling slot booking.
- **Frontend Requirements:** Connect `EnrollModal.tsx` and `ContactSection.tsx` to real API with loading and error states.
- **Backend Requirements:** `AdmissionsService`, SendGrid email dispatcher, lead scoring queue.
- **Database Requirements:** Tables: `inquiry_leads`, `applications`, `counseling_appointments`.
- **Security:** Turnstile bot protection, input sanitization against XSS.
- **Acceptance Criteria:** Form submissions create database records and trigger real confirmation emails.
- **Completion Evidence:** Successful lead creation verified in database and test mailbox.

#### Phase 6: Student Profile & Academic Onboarding
- **Objective:** Allow accepted prospective students to complete enrollment, setup profile, and accept terms.
- **Dependencies:** Phase 5.
- **Features:** Profile setup wizard, avatar upload, GitHub/LinkedIn linking, terms e-signing.
- **Frontend Requirements:** Implement `/portal/student/onboarding` multi-step form with form validation.
- **Backend Requirements:** S3 pre-signed upload URL generator for avatars, profile update service.
- **Database Requirements:** Table: `student_profiles`, `legal_agreements`.
- **Security:** Virus scanning on uploaded avatars (ClamAV), validation of MIME types.
- **Acceptance Criteria:** Student completes onboarding; status transitions to `ACTIVE_STUDENT`.
- **Completion Evidence:** Avatar uploaded to S3 and profile persisted in PostgreSQL.

#### Phase 7: Tuition Billing, Installments & Payment Gateway Integration
- **Objective:** Enable real student payments via Stripe and Razorpay with installment support.
- **Dependencies:** Phase 6.
- **Features:** PaymentIntent creation, webhook handlers, installment schedules, tax invoice generation.
- **Frontend Requirements:** Embed Stripe Elements / Razorpay SDK in checkout modal; show invoice receipts.
- **Backend Requirements:** Payment webhook listener with HMAC verification, PDF invoice generator.
- **Database Requirements:** Tables: `invoices`, `payment_transactions`, `installment_schedules`.
- **Security:** PCI-DSS compliance (no raw card data stored), idempotency keys on payment transactions.
- **Acceptance Criteria:** Successful test credit card charge creates invoice and updates enrollment status.
- **Completion Evidence:** Verified Stripe test transaction and downloaded PDF invoice.

#### Phase 8: Cohort Batch Scheduler & Timetable Engine
- **Objective:** Enable academy administrators to schedule cohort batches and assign faculty.
- **Dependencies:** Phase 4.
- **Features:** Batch creation, student seat caps, timetable planner, conflict detector.
- **Frontend Requirements:** Admin batch management views (`/portal/admin/batches`).
- **Backend Requirements:** Batch scheduling service with room/mentor overlap validation.
- **Database Requirements:** Tables: `cohort_batches`, `batch_schedules`.
- **Security:** Restricted to `ROLE_ACADEMY_ADMIN`.
- **Acceptance Criteria:** Admin creates batch with 50-student cap; schedule conflicts trigger warning.
- **Completion Evidence:** Timetable conflict unit tests passing.

#### Phase 9: Student Enrollment & Batch Allocation
- **Objective:** Link paying students to specific cohort batches and initialize academic progress trackers.
- **Dependencies:** Phase 7, Phase 8.
- **Features:** Automatic batch assignment upon payment, waitlist queue for filled batches.
- **Frontend Requirements:** Display assigned cohort and batch details in student dashboard.
- **Backend Requirements:** Enrollment state machine service; publishes `StudentEnrolledEvent`.
- **Database Requirements:** Table: `student_enrollments`. Unique index on `(student_id, batch_id)`.
- **Security:** Enforce tenant isolation on student enrollment records.
- **Acceptance Criteria:** Student assigned to batch; dashboard reflects cohort start countdown.
- **Completion Evidence:** Enrollment record verified with `status = ENROLLED`.

#### Phase 10: Student LMS Dashboard (Core Portal Shell)
- **Objective:** Replace static platform preview in `ExperienceSection` with fully functional Student Portal.
- **Dependencies:** Phase 9.
- **Features:** Student dashboard overview, active courses widget, learning streak counters, announcements.
- **Frontend Requirements:** Build `/portal/student/dashboard` matching the design system and tokens.
- **Backend Requirements:** Aggregated student dashboard API (`GET /api/v1/learning/dashboard-summary`).
- **Database Requirements:** Table: `student_streaks`, `daily_activity_logs`.
- **Security:** Protected by JWT auth and student role verification.
- **Acceptance Criteria:** Student logs in and sees personal enrolled courses and accurate streak count.
- **Completion Evidence:** Dashboard loads in < 150ms with live student data.

#### Phase 11: Modular Curriculum Video Player & Progress Tracking
- **Objective:** Deliver streaming video lessons with automated resume playback and completion tracking.
- **Dependencies:** Phase 10.
- **Features:** HLS video player, video bookmarking, transcript drawer, auto-marking lesson completed at 90%.
- **Frontend Requirements:** Build `/portal/student/courses/:id/player` with keyboard shortcuts.
- **Backend Requirements:** CloudFront signed cookie generator, heartbeat progress ping receiver.
- **Database Requirements:** Table: `lesson_progress`. Track `watch_time_seconds`, `completed_at`.
- **Security:** Signed URLs expire in 4 hours; prevent concurrent stream abuse.
- **Acceptance Criteria:** Video plays cleanly; refreshing page resumes at exact playback second.
- **Completion Evidence:** Video playback progress persisted and reflected on course progress bar.

#### Phase 12: End-to-End MVP Milestone Release Gate & Validation
- **Objective:** Comprehensive testing and release of **Milestone 1: Minimum Usable Student Learning MVP**.
- **Dependencies:** Phases 0 – 11.
- **Features:** End-to-end integration: Visitor landing page -> Apply -> Pay -> Login -> Learn -> Complete Lesson.
- **Frontend Requirements:** Zero console errors; smooth navigation between public and student portal.
- **Backend Requirements:** End-to-end integration tests covering entire student acquisition funnel.
- **Database Requirements:** Production migration verification with Flyway.
- **Security:** Penetration test on auth and payment endpoints.
- **Acceptance Criteria:** A new learner can sign up, pay test tuition, log in, watch lesson, and have progress saved.
- **Completion Evidence:** Automated Playwright E2E test passes green across entire student lifecycle.

---

### Milestone 2: Mentor Operations, 1:1 Booking & Live WebRTC Classrooms

#### Phase 13: Mentor Application, Onboarding & Faculty Verification
- **Objective:** Establish mentor registration, resume review, and contract approval workflows.
- **Dependencies:** Phase 2.
- **Features:** Mentor application form, admin review workbench, contract signing, profile approval.
- **Frontend Requirements:** Build `/portal/admin/mentors/applications` review screens.
- **Backend Requirements:** Background check status tracker, mentor invite token generator.
- **Database Requirements:** Tables: `mentor_applications`, `mentor_profiles`.
- **Security:** PII data masking on mentor background verification files.
- **Acceptance Criteria:** Approved applicant receives invite and accesses Mentor Portal.
- **Completion Evidence:** Mentor account successfully transitioned from `PENDING` to `ACTIVE`.

#### Phase 14: S3 Media Infrastructure & Video Transcoding Pipeline
- **Objective:** Build enterprise video processing pipeline converting raw lecture uploads into multi-bitrate HLS.
- **Dependencies:** Phase 11.
- **Features:** AWS MediaConvert / FFmpeg worker cluster, adaptive bitrate (1080p, 720p, 480p, 360p), thumbnail generator.
- **Frontend Requirements:** Adaptive bitrate selector inside video player.
- **Backend Requirements:** Async video upload processor emitting Kafka events on transcode completion.
- **Database Requirements:** Table: `media_assets`. Store HLS manifests, durations, resolutions.
- **Security:** S3 bucket policies preventing direct public access; access exclusively via CloudFront.
- **Acceptance Criteria:** Uploaded MP4 converts to HLS within 5 minutes with multiple bitrate streams.
- **Completion Evidence:** HLS `.m3u8` playlist verified playing in browser.

#### Phase 15: 1:1 Mentorship Availability & Booking Engine
- **Objective:** Replace mock 1:1 scheduling in `MentorsPage.tsx` with live calendar booking.
- **Dependencies:** Phase 13.
- **Features:** Recurring slot scheduler, atomic booking transactions, mentor calendar sync.
- **Frontend Requirements:** Upgrade `MentorsPage.tsx` and student mentorship booking screens.
- **Backend Requirements:** Slot conflict locking using Redis distributed locks; email invite generator.
- **Database Requirements:** Tables: `mentor_availability_slots`, `mentorship_sessions`.
- **Security:** Verify student mentorship credit balance before slot confirmation.
- **Acceptance Criteria:** Student books slot; slot becomes immediately unavailable to other students.
- **Completion Evidence:** Calendar `.ics` file generated and session logged in database.

#### Phase 16: LiveKit WebRTC Infrastructure Cluster Setup
- **Objective:** Deploy and configure scalable LiveKit WebRTC SFU cluster for real-time video classrooms.
- **Dependencies:** Phase 1.
- **Features:** LiveKit server deployment, JWT room token generator, audio/video mesh networking.
- **Frontend Requirements:** Integrate LiveKit React SDK components in student and mentor views.
- **Backend Requirements:** Spring Boot room manager provisioning rooms on demand.
- **Database Requirements:** Table: `webrtc_rooms`.
- **Security:** Room tokens cryptographically signed with 30-minute expiration; role-scoped permissions.
- **Acceptance Criteria:** 20+ participants join live room simultaneously with sub-200ms audio/video latency.
- **Completion Evidence:** Active WebRTC connection benchmark passing with zero packet loss.

#### Phase 17: Interactive Classroom Features (Chat, Screen Share & Whiteboard)
- **Objective:** Equip live classrooms with essential collaborative teaching tools.
- **Dependencies:** Phase 16.
- **Features:** High-fps screen sharing, in-call chat, interactive collaborative whiteboard, hand-raising queue.
- **Frontend Requirements:** Build `/portal/student/live-sessions/:id/room` and mentor live studio controls.
- **Backend Requirements:** LiveKit data channel message dispatcher; presence event broadcaster.
- **Database Requirements:** Table: `classroom_chat_messages`.
- **Security:** Instructor can mute/kick disruptive participants; profanity filter on chat.
- **Acceptance Criteria:** Instructor shares screen; students view 1080p stream with interactive chat.
- **Completion Evidence:** Screen sharing and chat telemetry logs verified during multi-user test.

#### Phase 18: Automated Classroom Recording & Instant Archival
- **Objective:** Automatically capture, transcode, and publish live classroom lectures.
- **Dependencies:** Phase 16, Phase 14.
- **Features:** LiveKit Egress service recording composited video + screen share to S3; auto-publishes to student portal.
- **Frontend Requirements:** Show "Recording in Progress" banner during class; display past recordings in dashboard.
- **Backend Requirements:** LiveKit webhook listener processing `egress_ended` events.
- **Database Requirements:** Table: `live_class_recordings`.
- **Security:** Recordings restricted to enrolled batch members.
- **Acceptance Criteria:** Finished lecture recording appears in student portal within 15 minutes of class end.
- **Completion Evidence:** Automated Egress test generates valid HLS recording on S3.

#### Phase 19: Automated Attendance Tracking & Presence Telemetry
- **Objective:** Eliminate manual attendance taking through automated WebRTC presence logging.
- **Dependencies:** Phase 16.
- **Features:** Heartbeat presence tracking, minimum 75% attendance threshold calculation, absence alerts.
- **Frontend Requirements:** Display personal attendance percentage in student and guardian dashboards.
- **Backend Requirements:** Attendance aggregation worker computing duration seconds from connection logs.
- **Database Requirements:** Table: `live_session_attendance`.
- **Security:** Anti-spoofing checks detecting multi-tab or proxy attendance fraud.
- **Acceptance Criteria:** Student attending 60 of 90 minutes receives accurate 66.6% attendance record.
- **Completion Evidence:** Attendance report exported to CSV matches connection timestamps.

#### Phase 20: Mentor Review Workbench & Code Grading
- **Objective:** Provide instructors with an enterprise assignment grading and Git pull request review workbench.
- **Dependencies:** Phase 13.
- **Features:** Side-by-side Git diff viewer, inline line commenting, rubric scoring, audio feedback clips.
- **Frontend Requirements:** Build `/portal/mentor/grading/:id/workbench`.
- **Backend Requirements:** GitHub API integration fetching PR diffs, rubric calculation engine.
- **Database Requirements:** Tables: `assignment_submissions`, `grading_rubrics`, `mentor_feedback_comments`.
- **Security:** Mentors can only grade assignments belonging to their assigned batches.
- **Acceptance Criteria:** Mentor leaves inline comment and rubric score; student receives instant notification.
- **Completion Evidence:** Graded assignment displays mentor diff comments and score in student portal.

#### Phase 21: Mentor Wallet, Timesheets & Payout Engine
- **Objective:** Automate faculty compensation tracking, lecture hours logging, and monthly payouts.
- **Dependencies:** Phase 13, Phase 18.
- **Features:** Automated timesheets calculated from live teaching and 1:1 sessions; payout request workflow.
- **Frontend Requirements:** Mentor earnings wallet view (`/portal/mentor/earnings`).
- **Backend Requirements:** Compensation calculator, Stripe Connect / Wise payout integration.
- **Database Requirements:** Tables: `mentor_timesheets`, `mentor_payout_transactions`.
- **Security:** Two-factor authorization required to modify payout bank details.
- **Acceptance Criteria:** Completed teaching hours accurately multiply by hourly rate in monthly ledger.
- **Completion Evidence:** Payout ledger balance verified against completed sessions.

---

### Milestone 3: Coding Sandboxes, Proctored Exams & Verified Credentials

#### Phase 22: In-Browser Monaco IDE & Containerized Execution Bridge
- **Objective:** Embed interactive coding environment in LMS executing unit tests inside secure containers.
- **Dependencies:** Phase 11.
- **Features:** Monaco Editor integration, syntax highlighting for 10+ languages, Judge0 API bridge.
- **Frontend Requirements:** Build `/portal/student/courses/:id/sandbox` split-pane layout.
- **Backend Requirements:** Ephemeral container execution runner with strict timeouts and memory caps.
- **Database Requirements:** Table: `coding_exercises`, `exercise_test_cases`.
- **Security:** Sandboxes isolated in network-restricted Docker containers with zero external internet access.
- **Acceptance Criteria:** Student writes Python function; clicks "Run"; sees test results in < 2 seconds.
- **Completion Evidence:** Successful code execution output verified across Python, JavaScript, and Java.

#### Phase 23: Versatile Examination Engine (MCQ, Subjective & Coding)
- **Objective:** Comprehensive testing engine supporting diagnostic quizzes, module exams, and finals.
- **Dependencies:** Phase 22.
- **Features:** Question bank manager, randomized question ordering, timed exam countdowns, auto-grading.
- **Frontend Requirements:** Build `/portal/student/exams/:id/proctored` full-screen exam interface.
- **Backend Requirements:** Exam state engine autosaving answers every 10 seconds.
- **Database Requirements:** Tables: `question_banks`, `exam_papers`, `exam_attempts`.
- **Security:** Prevent right-click, copy-paste, and print-screen during active exams.
- **Acceptance Criteria:** Student completes 50-question exam; receives automated score breakdown upon submit.
- **Completion Evidence:** Exam attempt saved with timestamps, answers, and calculated percentage score.

#### Phase 24: Remote Proctoring & Anti-Cheat Telemetry
- **Objective:** Enforce integrity during high-stakes exams through automated proctoring telemetry.
- **Dependencies:** Phase 23.
- **Features:** Tab-switch detection, webcam presence snapshots, audio anomaly detection, proctoring incident log.
- **Frontend Requirements:** Webcam preview in exam window, tab-switch warning dialogs.
- **Backend Requirements:** Machine learning image analyzer detecting multiple faces or absent student.
- **Database Requirements:** Table: `proctoring_incident_logs`.
- **Security:** Webcam snapshots encrypted at rest; automatically deleted after 90 days.
- **Acceptance Criteria:** Switching browser tabs 3 times flags attempt and alerts exam administrator.
- **Completion Evidence:** Proctoring incident log accurately records timestamped tab-switch events.

#### Phase 25: Capstone Project Defense & Review Workflow
- **Objective:** Final practical milestone: students defend production capstone projects before a panel.
- **Dependencies:** Phase 20, Phase 22.
- **Features:** GitHub repository submission, architecture diagram upload, live defense schedule picker.
- **Frontend Requirements:** Build `/portal/student/career-hub/capstone-defense` workflow.
- **Backend Requirements:** Multi-examiner rubric scoring aggregation.
- **Database Requirements:** Table: `capstone_submissions`, `defense_evaluations`.
- **Security:** Capstone codebase checked against GitHub public repositories for plagiarism.
- **Acceptance Criteria:** Student passes defense with >= 75% score from two independent evaluators.
- **Completion Evidence:** Capstone defense evaluation signed off and stored in database.

#### Phase 26: Cryptographically Signed Credential Generator
- **Objective:** Replace mock certificate in `CertificateModal` with verifiable digital credentials.
- **Dependencies:** Phase 25.
- **Features:** Automated X.509 cryptographic signing, high-resolution vector PDF generator, QR code stamper.
- **Frontend Requirements:** Upgrade `CertificateModal.tsx` and credential viewer views.
- **Backend Requirements:** PDF generation service (Puppeteer / PDFBox), SHA-256 digital signature generator.
- **Database Requirements:** Table: `issued_certificates`.
- **Security:** Tamper-proof certificate hash; revocation checks.
- **Acceptance Criteria:** Clicking "Download PDF" generates authentic, signed PDF certificate with valid QR.
- **Completion Evidence:** Downloaded PDF verified with Adobe Acrobat digital signature validator.

#### Phase 27: Public Credential Verification Portal
- **Objective:** Public-facing credential verification portal for employers and recruiters.
- **Dependencies:** Phase 26.
- **Features:** Fast public lookup page (`https://verify.nexarats.academy/{certId}`), accreditation badge.
- **Frontend Requirements:** Build `/verify/:certificateId` public verification view.
- **Backend Requirements:** High-speed cached public verification API.
- **Database Requirements:** Indexed query on `issued_certificates.certificate_code`.
- **Security:** Public endpoint with rate limiting to prevent enumeration attacks.
- **Acceptance Criteria:** Scanning QR code on certificate opens verified public page with learner details.
- **Completion Evidence:** Verification page loads in < 100ms displaying accurate certificate details.

#### Phase 28: LinkedIn Credential Integration & Social Sharing
- **Objective:** Allow graduates to add verified credentials directly to their LinkedIn profiles with 1 click.
- **Dependencies:** Phase 27.
- **Features:** LinkedIn Add to Profile URL generator, OpenGraph dynamic social preview cards.
- **Frontend Requirements:** "Add to LinkedIn" button in certificate viewer and modals.
- **Backend Requirements:** Dynamic OpenGraph image generator (Puppeteer / S3) rendering certificate previews.
- **Database Requirements:** Table: `social_share_events`.
- **Security:** Signed share URLs preventing tampering of shared social titles.
- **Acceptance Criteria:** Clicking LinkedIn button prepopulates official license number and issuing authority.
- **Completion Evidence:** LinkedIn certificate preview renders official Nexarats Academy credentials.

#### Phase 29: Academic Transcripts & Official Record Exports
- **Objective:** Generate formal academic transcripts showing total hours, GPA, and competencies.
- **Dependencies:** Phase 26.
- **Features:** Official transcript generator, institutional seal stamp, verification hash.
- **Frontend Requirements:** Build `/portal/student/transcripts` view with print and download buttons.
- **Backend Requirements:** Academic transcript aggregation engine compiling course, exam, and lab records.
- **Database Requirements:** Table: `academic_transcripts`.
- **Security:** Tamper-evident PDF watermark and institutional digital signature.
- **Acceptance Criteria:** Student exports comprehensive official transcript accepted by academic institutions.
- **Completion Evidence:** Generated transcript accurately reflects all passed modules and grades.

---

### Milestone 4: Corporate B2B Training & Multi-Tenant Organization SaaS

#### Phase 30: Enterprise Organization Hierarchy & Department Trees
- **Objective:** Enable corporate clients to structure employees into multi-level divisions and teams.
- **Dependencies:** Phase 2.
- **Features:** Department tree management, manager assignments, corporate cost centers.
- **Frontend Requirements:** Build `/portal/corporate/departments` with interactive tree view.
- **Backend Requirements:** Recursive CTE query endpoints in Spring Boot for hierarchical department trees.
- **Database Requirements:** Tables: `corporate_organizations`, `departments`.
- **Security:** Enforce strict corporate tenant boundary; managers can only view their subtree.
- **Acceptance Criteria:** Corporate admin creates nested department structure (Engineering -> Backend).
- **Completion Evidence:** Department tree queries benchmarked with zero cross-tenant leakage.

#### Phase 31: Enterprise SSO (SAML 2.0 / Okta / Azure AD) Integration
- **Objective:** Enable 1-click corporate employee login using enterprise Identity Providers.
- **Dependencies:** Phase 2.
- **Features:** SAML 2.0 SP metadata generator, IdP metadata ingestion, Just-In-Time (JIT) user provisioning.
- **Frontend Requirements:** Enterprise domain login screen redirecting to corporate IdP.
- **Backend Requirements:** Spring Security SAML 2.0 extension, assertion validator.
- **Database Requirements:** Table: `sso_configurations`.
- **Security:** Enforce SHA-256 signatures on SAML assertions; validate certificate expiration.
- **Acceptance Criteria:** Corporate user logs in via Okta without entering Nexarats-specific password.
- **Completion Evidence:** SAML assertion parsed and employee logged into corporate portal.

#### Phase 32: SCIM 2.0 Automated User Provisioning & Deprovisioning
- **Objective:** Automatically synchronize employee roster changes from corporate HR systems.
- **Dependencies:** Phase 31.
- **Features:** SCIM 2.0 `/Users` and `/Groups` REST endpoints, automated seat revocation on offboarding.
- **Frontend Requirements:** SCIM connection status indicators in corporate settings.
- **Backend Requirements:** SCIM protocol compliant controller in Spring Boot with Bearer token auth.
- **Database Requirements:** Table: `scim_sync_logs`.
- **Security:** SCIM tokens generated with rotating secret hashes.
- **Acceptance Criteria:** Terminating an employee in Okta immediately disables their Nexarats access.
- **Completion Evidence:** SCIM deprovisioning payload test passes and revokes seat.

#### Phase 33: Bulk Employee Enrollment & CSV Import Engine
- **Objective:** Allow HR administrators to bulk onboard hundreds of employees simultaneously.
- **Dependencies:** Phase 30.
- **Features:** CSV drag-and-drop parser, column mapper, dry-run validation, background batch processing.
- **Frontend Requirements:** Build `/portal/corporate/employees/bulk-upload` with progress bar.
- **Backend Requirements:** Spring Batch async job processing bulk records with error rollback.
- **Database Requirements:** Table: `bulk_import_jobs`.
- **Security:** Rate limit bulk imports; validate email syntax and domain whitelists.
- **Acceptance Criteria:** Uploading 500-employee CSV creates accounts and dispatches welcome emails.
- **Completion Evidence:** Bulk job completes with zero corrupted records and full error log.

#### Phase 34: Mandatory Compliance Training & Deadline Governance
- **Objective:** Track and enforce mandatory annual corporate compliance courses (SOC2, HIPAA, Ethics).
- **Dependencies:** Phase 30.
- **Features:** Hard deadline enforcement, overdue escalation alerts, manager notification cadence.
- **Frontend Requirements:** Urgent compliance banners in employee portal; compliance meter in admin view.
- **Backend Requirements:** Daily scheduled quartz worker scanning for approaching and overdue deadlines.
- **Database Requirements:** Tables: `compliance_requirements`, `compliance_audit_records`.
- **Security:** Compliance records are immutable and audit-logged for external regulatory audits.
- **Acceptance Criteria:** Employee nearing 7-day deadline receives automated notification; overdue triggers manager alert.
- **Completion Evidence:** Scheduled compliance scanner accurately triggers escalation webhooks.

#### Phase 35: Enterprise Skill Competency Matrix & Gap Analytics
- **Objective:** Visual matrix benchmarking team technical skills against corporate targets.
- **Dependencies:** Phase 23, Phase 30.
- **Features:** Skill heatmap radar, department skill comparison, personalized training recommendations.
- **Frontend Requirements:** Build `/portal/corporate/skills-matrix` with interactive heatmaps.
- **Backend Requirements:** Skill aggregation analytics engine calculating team percentile ranks.
- **Database Requirements:** Table: `skill_competency_scores`.
- **Security:** Aggregated metrics protect individual employee privacy where mandated.
- **Acceptance Criteria:** HR director views heatmap identifying Cloud Architecture as lowest-scoring skill.
- **Completion Evidence:** Heatmap calculations match assessment scores across departments.

#### Phase 36: Corporate License Pool & Seat Reallocation
- **Objective:** Manage corporate subscription seat quotas and reallocate unused licenses.
- **Dependencies:** Phase 30.
- **Features:** Seat utilization gauges, inactive user seat reclaim, license upgrade requests.
- **Frontend Requirements:** License utilization progress bars in `/portal/corporate/licenses`.
- **Backend Requirements:** Atomic seat quota validator preventing over-provisioning.
- **Database Requirements:** Tables: `corporate_license_pools`, `seat_allocations`.
- **Security:** Seat revocation immediately frees license pool count.
- **Acceptance Criteria:** Reclaiming seat from inactive employee immediately allows assigning to new hire.
- **Completion Evidence:** License pool count decrements and increments accurately under load.

#### Phase 37: White-Labeling & Custom Tenant Subdomains
- **Objective:** Support custom subdomains (`acme.nexarats.academy`) and custom branding per enterprise tenant.
- **Dependencies:** Phase 2.
- **Features:** Subdomain routing, custom tenant logos, color schemes, custom transactional email senders.
- **Frontend Requirements:** Dynamic theme provider injecting tenant CSS variables at runtime.
- **Backend Requirements:** Wildcard SSL certificate management, tenant branding API.
- **Database Requirements:** Table: `tenant_branding_configs`.
- **Security:** Prevent subdomain takeover; validate custom domain CNAME records.
- **Acceptance Criteria:** Visiting `acme.nexarats.academy` renders Acme logo and colors on login and portal.
- **Completion Evidence:** Custom branding rendered dynamically without code recompilation.

#### Phase 38: Corporate L&D Milestone Release Gate & Validation
- **Objective:** Comprehensive testing and release of **Milestone 4: Corporate B2B Training Platform**.
- **Dependencies:** Phases 30 – 37.
- **Features:** End-to-end enterprise flow: SSO login -> Department assignment -> Compliance training -> Certify -> HR Report.
- **Frontend Requirements:** Flawless theme rendering across light and dark modes in corporate portals.
- **Backend Requirements:** Complete integration test suite verifying multi-tenant data boundaries.
- **Database Requirements:** Row-Level Security penetration testing verifying zero cross-tenant leakage.
- **Security:** SOC 2 compliance readiness audit on corporate data handling.
- **Acceptance Criteria:** Enterprise client provisions 500 seats via SCIM and tracks 100% compliance.
- **Completion Evidence:** Multi-tenant security audit passed with zero isolation defects.

---

### Milestone 5: Placement Crucible, Resume ATS Builder & Alumni Ecosystem

#### Phase 39: Enterprise Hiring Partner Job Board
- **Objective:** Connect 500+ hiring partners with qualified academy graduates through an exclusive job board.
- **Dependencies:** Phase 26.
- **Features:** Job posting management, applicant tracking, candidate filtering by capstone scores.
- **Frontend Requirements:** Build `/portal/student/career-hub` and partner job listings.
- **Backend Requirements:** Job board service with role requirements and salary transparency.
- **Database Requirements:** Tables: `hiring_partners`, `job_postings`, `job_applications`.
- **Security:** Partner access restricted to pre-approved corporate recruiters.
- **Acceptance Criteria:** Partner posts role; eligible graduates receive automated notification to apply.
- **Completion Evidence:** Job application workflow verified from posting to candidate submission.

#### Phase 40: ATS-Optimized Resume Generator
- **Objective:** Automated resume builder importing verified capstones, grades, and credentials.
- **Dependencies:** Phase 26, Phase 39.
- **Features:** Standardized ATS-friendly PDF resume export, verified project commit highlights.
- **Frontend Requirements:** Build `/portal/student/career-hub/resume` builder interface.
- **Backend Requirements:** Clean text PDF generator passing standard ATS parsers (Lever, Greenhouse).
- **Database Requirements:** Table: `student_resumes`.
- **Security:** Student PII masked until candidate applies to specific role.
- **Acceptance Criteria:** Generated resume scores 95+ on standard ATS readability benchmarks.
- **Completion Evidence:** Exported PDF successfully parsed by third-party ATS simulator.

#### Phase 41: The Placement Crucible: 1:1 Big Tech Mock Interviews
- **Objective:** Prepare candidates through rigorous mock system design, coding, and behavioral interviews.
- **Dependencies:** Phase 15.
- **Features:** Mock interview scheduling, rubric scorecards, recorded video replays, action items.
- **Frontend Requirements:** Mock interview booking hub (`/portal/student/career-hub/mock-crucible`).
- **Backend Requirements:** Interviewer scorecard evaluation engine.
- **Database Requirements:** Tables: `mock_interviews`, `mock_scorecards`.
- **Security:** Interview recordings stored with signed URL access restricted to student and counselor.
- **Acceptance Criteria:** Student completes 5 mock rounds; receives aggregate readiness score for job drives.
- **Completion Evidence:** Completed mock scorecard logged in database with actionable feedback.

#### Phase 42: Exclusive Campus Placement Drives & Interview Coordination
- **Objective:** Coordinate exclusive recruitment drives with fast-tracked technical interviews.
- **Dependencies:** Phase 39.
- **Features:** Drive scheduling, candidate shortlisting, interview round tracking, offer logger.
- **Frontend Requirements:** Admin drive management views (`/portal/admin/placement/drives`).
- **Backend Requirements:** Multi-stage applicant tracking pipeline service.
- **Database Requirements:** Tables: `placement_drives`, `interview_rounds`, `job_offers`.
- **Security:** Job offer details (salary, equity) encrypted with column-level encryption.
- **Acceptance Criteria:** Partner conducts drive; shortlists 10 candidates; extends 3 verified offers.
- **Completion Evidence:** Job offer logged and counted toward 100% job assurance guarantee metrics.

#### Phase 43: Salary Negotiation Advisory & Offer Verification
- **Objective:** Assist graduates in evaluating and negotiating compensation packages.
- **Dependencies:** Phase 42.
- **Features:** Offer upload portal, salary benchmarking calculator, counselor advisory chat.
- **Frontend Requirements:** Offer verification upload sheet in student career hub.
- **Backend Requirements:** Offer verification workflow requiring signed offer letter upload.
- **Database Requirements:** Table: `verified_job_placements`.
- **Security:** Strict confidentiality controls on verified compensation records.
- **Acceptance Criteria:** Offer verified; average cohort CTC recalculated dynamically.
- **Completion Evidence:** Verified placement linked to student graduation record.

#### Phase 44: Alumni Network Directory & Mentorship Circles
- **Objective:** Lifelong engagement platform connecting graduates with junior cohorts and peers.
- **Dependencies:** Phase 26.
- **Features:** Searchable alumni directory by company and city, alumni mentorship circles, job referral board.
- **Frontend Requirements:** Build `/portal/student/alumni` directory and community feed.
- **Backend Requirements:** Alumni directory API with privacy visibility toggles.
- **Database Requirements:** Tables: `alumni_profiles`, `job_referrals`.
- **Security:** Alumni can opt-out of public directory visibility.
- **Acceptance Criteria:** Graduate browses alumni working at Google and requests coffee chat referral.
- **Completion Evidence:** Referral request successfully routed to alumni member.

#### Phase 45: 100% Job Assurance Guarantee Compliance Auditor
- **Objective:** Automated auditing system tracking institutional compliance with job assurance guarantees.
- **Dependencies:** Phase 42, Phase 43.
- **Features:** Compliance dashboard tracking students without offers at 90/180 days; triggers tuition refund workflows if terms unmet.
- **Frontend Requirements:** Job assurance compliance dashboard in admin portal.
- **Backend Requirements:** Automated eligibility rules engine monitoring interview drive participation.
- **Database Requirements:** Table: `job_assurance_compliance_logs`.
- **Security:** Audit-ready compliance logs for consumer protection and regulatory bodies.
- **Acceptance Criteria:** System flags eligible graduates lacking placement for priority executive intervention.
- **Completion Evidence:** Compliance audit report generated matching verified placement rates.

---

### Milestone 6: AI-Native Syllabus RAG Tutor & Adaptive Learning Engine

#### Phase 46: Python FastAPI AI Subsystem Architecture & Vector Store
- **Objective:** Establish the independent Python FastAPI AI microservice with vector database.
- **Dependencies:** Phase 1.
- **Features:** FastAPI service setup, PostgreSQL `pgvector` / Qdrant vector database connection, JWT validation.
- **Frontend Requirements:** AI tutor chat drawer component.
- **Backend Requirements:** Inter-service mTLS communication between Spring Boot and FastAPI.
- **Database Requirements:** Vector extension `pgvector` enabled; table: `curriculum_embeddings`.
- **Security:** API keys for LLMs stored in AWS Secrets Manager; tenant-scoped vector collections.
- **Acceptance Criteria:** FastAPI service accepts query; performs vector similarity search in < 50ms.
- **Completion Evidence:** Vector index created and queried successfully with sample embeddings.

#### Phase 47: Curriculum Document Ingestion & RAG Indexing Pipeline
- **Objective:** Automated pipeline chunking and embedding course transcripts, lecture slides, and starter code.
- **Dependencies:** Phase 46, Phase 18.
- **Features:** Automated Whisper AI transcription of lecture videos; semantic chunking and embedding generation.
- **Frontend Requirements:** Admin ingestion status indicators in curriculum builder.
- **Backend Requirements:** Async Celery ingestion workers chunking text and generating embeddings via OpenAI / local models.
- **Database Requirements:** Tables: `ingestion_jobs`, `document_chunks`.
- **Security:** Redact student PII and instructor credentials prior to vector embedding.
- **Acceptance Criteria:** Publishing course automatically indexes all lessons into vector store.
- **Completion Evidence:** 10 hours of video transcripts embedded and searchable via cosine similarity.

#### Phase 48: 24/7 Syllabus-Grounded AI Learning Tutor (Streaming SSE)
- **Objective:** Context-aware AI tutor answering student questions strictly grounded in course materials.
- **Dependencies:** Phase 47.
- **Features:** Server-Sent Events (SSE) streaming chat, source citation pill links, hallucination guardrails.
- **Frontend Requirements:** Build `/portal/student/ai-tutor` with markdown rendering and code copying.
- **Backend Requirements:** LangChain / LlamaIndex RAG pipeline with prompt guardrails preventing off-topic queries.
- **Database Requirements:** Table: `ai_chat_conversations`, `ai_chat_messages`.
- **Security:** Prompt injection defenses; strict grounding rules preventing answer hallucination.
- **Acceptance Criteria:** AI tutor answers complex React Hook question citing specific timestamps from Lesson 4.
- **Completion Evidence:** Streaming SSE response completes with valid citations and code block formatting.

#### Phase 49: Intelligent Code Debugging Assistant (Socratic Method)
- **Objective:** AI assistant analyzing student stack traces and guiding them to fixes without giving away answers.
- **Dependencies:** Phase 22, Phase 48.
- **Features:** Socratic debugging prompt engine, error trace parser, hints progression system (Hint 1 -> 2 -> 3).
- **Frontend Requirements:** "Explain Error" button inside Monaco IDE lab output console.
- **Backend Requirements:** Specialized prompt template guiding student logically rather than writing code.
- **Database Requirements:** Table: `code_debugging_logs`.
- **Security:** Enforce strict instructions preventing the LLM from generating complete solution code.
- **Acceptance Criteria:** Student clicks "Explain Error"; AI explains off-by-one index error conceptually.
- **Completion Evidence:** Socratic feedback verified without raw solution leakage.

#### Phase 50: Adaptive Diagnostic Quizzing & Knowledge Gap Detection
- **Objective:** Dynamically generate personalized practice quizzes targeting student-specific weak concepts.
- **Dependencies:** Phase 23, Phase 48.
- **Features:** Knowledge graph tracking concept mastery; dynamic LLM quiz generator with auto-validation.
- **Frontend Requirements:** "Practice My Weak Areas" button in exam score analysis view.
- **Backend Requirements:** Concept mastery evaluation algorithm calculating weak topic vectors.
- **Database Requirements:** Table: `student_concept_mastery`.
- **Security:** Dynamically generated questions verified against curriculum ground truth.
- **Acceptance Criteria:** Student struggling with SQL joins receives targeted 5-question diagnostic drill.
- **Completion Evidence:** Diagnostic drill generated dynamically and scored in database.

#### Phase 51: AI Voice Screening Coach & Behavioral Interview Simulator
- **Objective:** Voice-interactive AI simulating behavioral (STAR method) and technical screening interviews.
- **Dependencies:** Phase 48.
- **Features:** Voice-to-text (Whisper), LLM interview evaluation, text-to-speech feedback on pacing and clarity.
- **Frontend Requirements:** Audio recording interview room with live speech waveform.
- **Backend Requirements:** Async audio processing pipeline scoring communication clarity and technical accuracy.
- **Database Requirements:** Tables: `ai_voice_interviews`, `ai_interview_scorecards`.
- **Security:** Audio clips deleted after scorecard generation per privacy settings.
- **Acceptance Criteria:** Student completes 10-minute behavioral screening; receives rubric score on pacing and clarity.
- **Completion Evidence:** Voice interview scorecard generated with actionable speech feedback.

#### Phase 52: AI Learning Intelligence Milestone Release Gate & Validation
- **Objective:** Comprehensive testing and release of **Milestone 6: AI-Native Learning Subsystem**.
- **Dependencies:** Phases 46 – 51.
- **Features:** Full AI integration across curriculum RAG, in-browser debugging, adaptive tests, and voice coach.
- **Frontend Requirements:** Responsive AI drawer accessible across all student learning screens.
- **Backend Requirements:** Load testing on FastAPI microservice supporting 5,000 concurrent streaming connections.
- **Database Requirements:** Vector index optimization ensuring P95 query latency < 100ms.
- **Security:** Red-team evaluation testing prompt injection and model jailbreak defenses.
- **Acceptance Criteria:** AI tutor responds accurately with zero hallucination on syllabus-specific queries.
- **Completion Evidence:** Red-team security report signed off with zero bypass vulnerabilities.

---

### Milestone 7: Cross-Platform Native Mobile Applications (iOS & Android)

#### Phase 53: Flutter Mobile Architecture & Token Mapping
- **Objective:** Initialize Flutter mobile workspace (`apps/mobile/`) consuming Nexarats brand design tokens and OpenAPI contracts (ADR-007, superseding React Native & Expo).
- **Dependencies:** Phase 1, Phase 2.
- **Features:** Flutter stable SDK project setup, Material 3 `ThemeData` with Nexarats brand tokens, `go_router` declarative routing, `flutter_bloc` state management.
- **Frontend Requirements:** Splash screen (`MOB-01`), theme provider matching web colors (`#2563EB`, `#1D4ED8`, `#0F1D40`, Plus Jakarta Sans).
- **Backend Requirements:** Ensure all REST APIs accept mobile client User-Agent headers.
- **Database Requirements:** No schema change.
- **Security:** Secure storage for JWTs using `flutter_secure_storage` (Keychain on iOS, Keystore on Android).
- **Acceptance Criteria:** App compiles and launches cleanly on iOS Simulator and Android Emulator.
- **Completion Evidence:** Clean Flutter build executing on both iOS and Android platforms.

#### Phase 54: Mobile Authentication & Biometric Security (FaceID / Fingerprint)
- **Objective:** Native mobile login with biometric authentication and passwordless flows.
- **Dependencies:** Phase 53.
- **Features:** FaceID / TouchID biometric login via `local_auth`, demo account toggle.
- **Frontend Requirements:** Build `MOB-02` (Login), `MOB-03` (Enterprise SSO), `MOB-04` (Forgot Password).
- **Backend Requirements:** Mobile refresh token management with device fingerprinting.
- **Database Requirements:** Table: `mobile_device_registrations`.
- **Security:** Biometric keys securely stored in hardware-backed Secure Enclave / TEE.
- **Acceptance Criteria:** User logs in once; subsequent opens authenticate via FaceID in < 1 second.
- **Completion Evidence:** Biometric unlock verified on physical iOS and Android test devices.

#### Phase 55: Mobile Course Player & Offline Download Manager
- **Objective:** Allow students to download video lessons and watch completely offline during commutes.
- **Dependencies:** Phase 53, Phase 14.
- **Features:** Background video downloader, encrypted local storage, offline progress sync on reconnect.
- **Frontend Requirements:** Build `MOB-08` (Player), `MOB-09` (Audio Mode), `MOB-12` (Download Manager).
- **Backend Requirements:** Signed temporary download URLs with license expiry.
- **Database Requirements:** Local Drift (SQLite) database for offline progress caching.
- **Security:** Downloaded video files encrypted with AES-256 local key; cannot be exported outside app.
- **Acceptance Criteria:** User downloads 3 lessons; enters Airplane Mode; plays video with progress synced upon reconnect.
- **Completion Evidence:** Offline video playback and sync verified with network disconnection test.

#### Phase 56: Mobile Live Classroom (Native LiveKit WebRTC Integration)
- **Objective:** Full native WebRTC live classroom client for attending cohorts on mobile devices.
- **Dependencies:** Phase 53, Phase 16.
- **Features:** Native LiveKit Flutter SDK integration (`livekit_client`), speaker grid, hand-raising, in-call chat drawer, audio-only toggle.
- **Frontend Requirements:** Build `MOB-14` (Live Video Room), `MOB-15` (In-Call Chat).
- **Backend Requirements:** Room token generator supporting mobile participant metadata.
- **Database Requirements:** No schema change.
- **Security:** Microphone and camera permission handling compliant with iOS/Android guidelines.
- **Acceptance Criteria:** Student joins live classroom from phone; participates in chat and video seamlessly.
- **Completion Evidence:** 60-minute live session completed on mobile with zero memory leaks.

#### Phase 57: Native Push Notifications (Firebase Cloud Messaging & APNs)
- **Objective:** High-reliability push notifications for lecture reminders, mentor reviews, and deadlines.
- **Dependencies:** Phase 53.
- **Features:** `firebase_messaging` + `flutter_local_notifications` integration with Apple Push Notification service (APNs) and FCM.
- **Frontend Requirements:** Build `MOB-36` (Notifications Center) with category filter chips.
- **Backend Requirements:** Notification dispatcher service with user quiet-hours scheduling.
- **Database Requirements:** Table: `push_device_tokens`.
- **Security:** Push notification payloads omit sensitive PII data.
- **Acceptance Criteria:** Mentor grading student assignment triggers native push banner in < 3 seconds.
- **Completion Evidence:** Push notification received and tapped on locked iOS/Android devices.

#### Phase 58: Mobile Mentorship Booking & 1:1 Video Consultation
- **Objective:** Book and attend 1:1 mentor video sessions directly from the mobile app.
- **Dependencies:** Phase 53, Phase 15, Phase 56.
- **Features:** Native slot calendar picker, 1:1 LiveKit video room with front camera, post-session notes.
- **Frontend Requirements:** Build `MOB-23` (Mentorship Tab), `MOB-24` (Slot Picker), `MOB-25` (1:1 Call Room).
- **Backend Requirements:** Same unified mentorship API.
- **Database Requirements:** No schema change.
- **Security:** Video consultation stream encrypted peer-to-peer or via SFU.
- **Acceptance Criteria:** Student books 1:1 call and completes 15-minute consultation on mobile.
- **Completion Evidence:** 1:1 mobile video call completed with mentor feedback saved.

#### Phase 59: App Store & Google Play Release Preparation Gate
- **Objective:** Production hardening and submission of mobile apps to Apple App Store and Google Play.
- **Dependencies:** Phases 53 – 58.
- **Features:** App store metadata, app icon assets, privacy manifests, Fastlane automated build pipelines.
- **Frontend Requirements:** Complete compliance with Apple Human Interface Guidelines and Material Design 3.
- **Backend Requirements:** Production mobile API rate limiting and version header enforcement.
- **Database Requirements:** No schema change.
- **Security:** Apple App Privacy manifest completed; zero hardcoded secrets in binary.
- **Acceptance Criteria:** App passes Apple App Store Review and Google Play Console review.
- **Completion Evidence:** TestFlight and Google Play Internal Track distribution builds active.

---

### Milestone 8: Enterprise Integrations, Global Search & Advanced BI

#### Phase 60: Global Full-Text & Vector Search Engine (OpenSearch / pgvector)
- **Objective:** Unified search engine indexing programs, courses, mentors, lessons, forums, and alumni.
- **Dependencies:** Phase 4.
- **Features:** Instant typeahead search popover in header, fuzzy matching, search filters, relevance ranking.
- **Frontend Requirements:** Upgrade search drawer in `Header.tsx` to call live search API.
- **Backend Requirements:** OpenSearch / pgvector search worker indexing entity changes via Kafka.
- **Database Requirements:** Tables: `search_indexes`, `search_telemetry_queries`.
- **Security:** Search queries sanitized against injection; results strictly scoped to user tenant.
- **Acceptance Criteria:** Typing "React" in header displays matching programs, courses, mentors, and lessons in < 50ms.
- **Completion Evidence:** Search query latency and relevance benchmarks passing.

#### Phase 61: Enterprise LMS Connectors (Workday, Cornerstone & Degreed)
- **Objective:** Synchronize corporate training completions with enterprise HRMS systems.
- **Dependencies:** Phase 34.
- **Features:** Pre-built connectors for Workday Learning, Cornerstone OnDemand, Degreed, SAP SuccessFactors.
- **Frontend Requirements:** Integration configuration cards in corporate admin portal.
- **Backend Requirements:** Integration sync worker emitting standardized xAPI / SCORM statements.
- **Database Requirements:** Table: `enterprise_lms_integrations`.
- **Security:** API credentials encrypted with customer-managed KMS keys.
- **Acceptance Criteria:** Employee completing course in Nexarats automatically marks complete in Workday.
- **Completion Evidence:** Verified xAPI completion statement received by external LRS.

#### Phase 62: Enterprise Business Intelligence Dashboards & Analytics
- **Objective:** Advanced reporting for executive sponsors, academic directors, and finance teams.
- **Dependencies:** Phase 10, Phase 20.
- **Features:** Visual BI dashboards: student retention funnel, mentor NPS, revenue MRR, cohort velocity.
- **Frontend Requirements:** Build `/portal/admin/analytics/academics` and corporate analytics views.
- **Backend Requirements:** Read-replica database queries with materialization and Redis caching.
- **Database Requirements:** Analytical views / materialized tables: `mv_cohort_completion_rates`.
- **Security:** Financial analytics restricted strictly to `ROLE_SUPER_ADMIN` and `ROLE_FINANCE`.
- **Acceptance Criteria:** Executive dashboard loads aggregated metrics across 50,000 students in < 300ms.
- **Completion Evidence:** BI reports match transactional database records with 100% precision.

#### Phase 63: Predictive At-Risk Student Identification (ML Churn Model)
- **Objective:** Machine learning model identifying students at risk of drop-off for proactive counselor intervention.
- **Dependencies:** Phase 19, Phase 23, Phase 62.
- **Features:** Churn risk scoring algorithm evaluating attendance drops, late assignments, and quiz scores.
- **Frontend Requirements:** High-risk student badges in cohort rosters and counselor dashboards.
- **Backend Requirements:** Nightly Python ML scoring worker publishing at-risk flags to counselor queues.
- **Database Requirements:** Table: `student_risk_evaluations`.
- **Security:** Predictive scores treated as confidential academic support records.
- **Acceptance Criteria:** Student missing 2 classes and 1 assignment flagged as "High Risk" for counselor outreach.
- **Completion Evidence:** Automated counselor outreach triggered for high-risk test student.

#### Phase 64: Automated Report Builder & Scheduled PDF Digests
- **Objective:** Allow administrators and HR managers to build custom reports with automated weekly email delivery.
- **Dependencies:** Phase 62.
- **Features:** Custom column selector, date range filter, export formats (CSV, Excel, PDF), email scheduler.
- **Frontend Requirements:** Build `/portal/corporate/reports/scheduled` report builder.
- **Backend Requirements:** Spring Batch report generation worker with SendGrid attachment delivery.
- **Database Requirements:** Table: `scheduled_report_configs`.
- **Security:** Report exports logged in operational audit trail.
- **Acceptance Criteria:** HR manager configures weekly Monday 9 AM compliance report; receives PDF email on schedule.
- **Completion Evidence:** Automated scheduled report email verified in test inbox.

#### Phase 65: Comprehensive Webhook Subscription Broker
- **Objective:** Public webhook gateway allowing third-party applications to subscribe to platform lifecycle events.
- **Dependencies:** Phase 60.
- **Features:** Webhook endpoint management, HMAC-SHA256 signature verification, automated retry with exponential backoff.
- **Frontend Requirements:** Webhook manager UI in corporate and admin settings with test payload ping.
- **Backend Requirements:** Reliable webhook delivery worker using Dead Letter Queues (DLQ) in RabbitMQ.
- **Database Requirements:** Tables: `webhook_subscriptions`, `webhook_delivery_attempts`.
- **Security:** Webhooks signed with tenant secret; TLS delivery mandatory.
- **Acceptance Criteria:** External system receives signed `student.graduated` webhook upon certificate issuance.
- **Completion Evidence:** Webhook delivery logs verify successful HTTP 200 acknowledgment.

---

### Milestone 9: Fleet Hardening, Global High Availability & Production GA

#### Phase 66: High-Availability Database Clustering & Multi-Region Read Replicas
- **Objective:** Scale PostgreSQL and Redis infrastructure to support high availability across geographic regions.
- **Dependencies:** Phase 2.
- **Features:** PostgreSQL Aurora Multi-AZ with automatic failover, 3 read replicas, Redis Sentinel HA cluster.
- **Frontend Requirements:** No UI change.
- **Backend Requirements:** Spring Boot read/write data source routing (`AbstractRoutingDataSource`).
- **Database Requirements:** Database replication latency monitoring.
- **Security:** Database traffic encrypted in transit with SSL certificates.
- **Acceptance Criteria:** Simulating primary database failure completes automatic failover in < 30 seconds with zero data loss.
- **Completion Evidence:** Chaos engineering failover test verified with zero request dropped.

#### Phase 67: Global CDN, Edge Caching & Asset Optimization
- **Objective:** Optimize global asset delivery ensuring Core Web Vitals score of 95+ worldwide.
- **Dependencies:** Phase 14.
- **Features:** Cloudflare Enterprise edge caching for static assets, Brotli compression, image optimization (WebP/AVIF).
- **Frontend Requirements:** Dynamic code-splitting on all routes via `React.lazy()` to reduce initial bundle < 200 KB.
- **Backend Requirements:** Cache-Control headers tuned for public vs private API responses.
- **Database Requirements:** No schema change.
- **Security:** Cloudflare WAF managed rules blocking OWASP Top 10 vulnerabilities at edge.
- **Acceptance Criteria:** Google PageSpeed Insights score >= 95 for Desktop and >= 90 for Mobile on landing page.
- **Completion Evidence:** Bundle analyzer confirms main vendor chunk reduced below 200 KB gzip.

#### Phase 68: SOC 2 Type II, ISO 27001 & Penetration Testing Audit
- **Objective:** External security audit, penetration testing, and regulatory compliance certification.
- **Dependencies:** Phase 10, Phase 38.
- **Features:** Automated evidence collection (Vanta / Drata), third-party penetration testing remediation.
- **Frontend Requirements:** Security vulnerability disclosure page and bug bounty submission form.
- **Backend Requirements:** Dependency vulnerability remediation, CSP headers hardening.
- **Database Requirements:** Comprehensive audit trail verification.
- **Security:** Zero Critical or High vulnerabilities across SAST, DAST, and container scans.
- **Acceptance Criteria:** Clean third-party penetration testing report with zero unmitigated vulnerabilities.
- **Completion Evidence:** Formal penetration test sign-off report issued by accredited cybersecurity auditor.

#### Phase 69: Final Production GA Release, Fleet Monitoring & Handoff
- **Objective:** Official General Availability (GA) launch of the complete Nexarats Academy ecosystem across all 20 pillars.
- **Dependencies:** Phases 0 – 68.
- **Features:** Production DNS switchover, 24/7 Datadog / Prometheus telemetry alerts, disaster recovery runbooks.
- **Frontend Requirements:** Final UI inspection across all 9 portal surfaces and mobile apps.
- **Backend Requirements:** Production cluster autoscaling policies verified under simulated 50,000-user load.
- **Database Requirements:** Final production database backup snapshot and disaster recovery drill.
- **Security:** Security operations center (SOC) monitoring active; emergency incident response team on-call.
- **Acceptance Criteria:** Platform running live in production with 99.99% uptime SLA across all 70 phases.
- **Completion Evidence:** Production GA deployment sign-off verified by Principal Architect, QA Lead, and Platform Owner.
