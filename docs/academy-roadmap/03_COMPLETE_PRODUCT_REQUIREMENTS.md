# NEXARATS ACADEMY — COMPLETE PRODUCT REQUIREMENTS DOCUMENT (PRD)
## Enterprise Academy, LMS, Corporate Upskilling, AI Education & SaaS Platform

- **Document ID:** `03_COMPLETE_PRODUCT_REQUIREMENTS`
- **Execution Date:** 2026-10-13
- **Classification:** Enterprise Product Requirements Specification

---

## 1. Product Vision & Strategic Positioning

### 1.1 Executive Vision
Nexarats Academy is conceived as an all-in-one, enterprise-grade educational ecosystem bridging the gap between academic theory and high-growth industry careers. It serves as:
1. An **Elite Tech Academy (B2C)** delivering cohort-based bootcamps and specialized skill masterclasses with 100% job placement assurance.
2. An **Enterprise Learning & Development (B2B SaaS)** platform for corporations to upskill, track compliance, assess technical competencies, and manage workforce certifications.
3. An **AI-Native Adaptive Education Platform** integrating personalized 24/7 AI tutors, automated code execution sandboxes, and intelligent career pathway counseling.
4. A **Multi-Tenant SaaS Engine** supporting independent branches, partner universities, and enterprise clients under a unified architecture.

### 1.2 Target Market & Persona Segments
- **Aspiring Tech Learners (B2C Students):** College students and career switchers seeking job-ready skills, verified credentials, and interview pipelines.
- **Enterprise Employees (B2B Learners):** Corporate staff completing mandated compliance courses, cloud architectures, or technical leadership training.
- **Mentors & Industry Instructors:** Staff engineers and subject matter experts delivering live lectures, grading code submissions, and conducting 1:1 sessions.
- **Academy Operators & Counselors:** Internal staff managing admissions funnels, scheduling cohorts, and monitoring placement outcomes.
- **Corporate HR & L&D Leaders:** Managers assigning corporate learning pathways, monitoring completion analytics, and certifying teams.
- **Parents & Guardians:** Sponsors monitoring academic progress, attendance, and fee installments for junior or sponsored learners.
- **Platform Super Administrators:** Platform owners governing multi-tenant billing, system health, security policies, and third-party integrations.

---

## 2. Core Functional Requirements Across 20 Pillars

### Pillar 1: Public Academy Website (Preserved & Enhanced)
- **Brand Consistency:** Retain the existing aesthetic, Plus Jakarta Sans typography, and `#2563EB` blue brand palette.
- **Dynamic Program & Course Catalog:** Real-time search, categorization by career track, syllabus preview, and cohort countdowns.
- **Social Proof & Outcomes Display:** Verified alumni placement stories, salary multiplier benchmarks, and interactive hiring partner directories.
- **Conversion Optimization:** Instant enrollment inquiries, 1:1 counseling booking, platform demo scheduling, and chat assistance.
- **SEO & Performance:** Server-side metadata rendering, OpenGraph tags, sitemap generation, and 95+ Core Web Vitals target.

### Pillar 2: Student Learning Portal (LMS)
- **Learner Dashboard:** Real-time progress trackers, upcoming live session cards, pending assignments, and streak counters.
- **Curriculum Player:** Video player with playback speeds, multi-resolution HLS streaming, timestamp notes, transcripts, and downloadable source code.
- **Interactive Code Sandboxes:** In-browser IDE powered by Monaco Editor and backend container execution (Judge0 / Firecracker microVMs).
- **Personalized Learning Pathway:** Adaptive module gating where advancing to advanced modules requires passing diagnostic checkpoints.
- **Resource Repository:** PDF cheat sheets, starter repositories, slide decks, and external reading lists.

### Pillar 3: Mentor & Instructor Portal
- **Cohort Schedule Management:** Calendar view of scheduled live lectures, office hours, and 1:1 review sessions.
- **Attendance & Engagement Tracking:** Automated attendance logging from live streams, poll participation tracking, and camera-on metrics.
- **Assignment & Code Review Workbench:** Side-by-side Git diff viewer, inline line-commenting, rubric-based scoring, and audio/video feedback recording.
- **1:1 Mentorship Hub:** Availability schedule picker, automated Google Calendar / Outlook synchronization, session notes, and student progress overviews.
- **Instructor Compensation & Payouts:** Session tracking, hourly/revenue-share calculations, and payout request logs.

### Pillar 4: Academy Administration Portal
- **Cohort & Batch Operations:** Batch creation, student enrollment limits, teacher assignments, timetable conflict detection, and room allocations.
- **Admissions Pipeline (CRM):** Lead capture, counselor assignment, pipeline stages (Inquiry, Counseling, Entrance Exam, Offer, Enrolled), and conversion metrics.
- **Curriculum Lifecycle Management:** Version-controlled course builder, module sequencing, prerequisite chains, and content publishing workflows.
- **Placement & Career Operations:** Job board management, student resume verification, company interview drive coordination, and offer tracking.
- **Operational Reports:** Drop-off heatmaps, mentor satisfaction ratings, batch completion rates, and financial reports.

### Pillar 5: Employee Learning & Development Portal (Corporate B2B)
- **Corporate Learner Onboarding:** Single Sign-On (SSO) through Okta, Azure AD, or Google Workspace; department and role assignment.
- **Assigned Learning Tracks:** Mandatory compliance modules with strict deadlines, skill upskilling tracks, and elective course libraries.
- **Skill Competency Radar:** Visual benchmark comparing employee skill scores against company benchmark profiles.
- **Manager Approval Workflows:** Requests for elective courses or external certification reimbursements.

### Pillar 6: Corporate Organization Administration Portal
- **Department & Team Hierarchy:** Multi-level tree organization (Enterprise -> Division -> Department -> Team -> Employee).
- **Cohort & Track Assignment Engine:** Bulk assign learning paths based on role, department, or tenure.
- **Compliance & Completion Reporting:** Real-time compliance monitoring, overdue alerts, and automated weekly summary emails to executive sponsors.
- **License & Seat Management:** Active seat tracking, license pool allocation, and automated seat revocation on employee deprovisioning (SCIM).

### Pillar 7: Parent or Guardian Portal
- **Child Academic Overview:** Attendance logs, quiz scores, project defense grades, and mentor feedback notes.
- **Direct Counselor Communication:** Dedicated messaging channel with assigned academic counselor.
- **Fee Management & Receipts:** View tuition installments, upcoming payment due dates, and download official tax receipts.
- **Early Warning Alerts:** Automated notifications triggered when student misses 2 consecutive live sessions or fails an assessment.

### Pillar 8: Platform Super Administration
- **Multi-Tenant Fleet Governance:** Provisioning new academy branches or corporate tenants with dedicated schema/database routing.
- **Platform Telemetry & Health:** Real-time API latency metrics, active WebSocket connections, video room loads, and queue depths.
- **Global Feature Flags:** Toggle features (e.g. AI Tutor, Live Classroom beta) per tenant or per subscription tier.
- **Security & Incident Operations:** Global audit trail viewer, IP blocklists, failed login investigation, and emergency tenant suspension.

### Pillar 9 & 10: Native Mobile Applications (Android & iOS)
- **Framework:** Flutter + Dart for responsive native cross-platform execution (supersedes React Native per ADR-007).
- **Offline Mode:** Encrypted local Drift (SQLite) storage for video lessons, lecture audio, and offline reading materials with sync-on-reconnect.
- **Push Notifications:** Instant alerts via FCM and APNs for upcoming live sessions, assignment deadlines, mentor review comments, and forum mentions.
- **Mobile Live Classroom:** Native LiveKit Flutter SDK integration for audio/video streaming, chat, and hand-raising.

### Pillar 11: Live Classroom & Meeting System
- **Real-Time Infrastructure:** Low-latency WebRTC SFU powered by LiveKit Cloud / self-hosted cluster.
- **Classroom Features:** Multi-speaker layout, screen sharing with audio, live breakout rooms, interactive whiteboard, live polls, and Q&A upvoting.
- **Automated Cloud Recording:** Dual-stream recording (speaker + screen), automated transcoding to HLS, and instant publishing to student portal within 15 minutes.
- **Automated Attendance:** Continuous presence pinging; exports attendance percentage directly into gradebook.

### Pillar 12: Exams, Assignments, Coding Laboratories & Grading
- **Versatile Exam Engine:** Multiple Choice Questions (MCQ), coding challenges, subjective essay questions, and file upload submissions.
- **Proctoring & Anti-Cheat:** Browser tab-switch detection, webcam proctoring snapshots, copy-paste disabled environments, and AI plagiarism checks.
- **Containerized Code Sandbox:** Ephemeral Docker/microVM sandboxes running unit tests in Python, Java, JavaScript, TypeScript, Go, C++, and Rust.
- **Gradebook & Feedback:** Automated grading for MCQs and unit tests; manual rubric grading with annotations for capstone projects.

### Pillar 13: Certificates, Credentials & Verification
- **Automated Issuance:** Triggered upon 100% course completion, passing all mandatory assessments, and mentor defense approval.
- **Cryptographic Security:** SHA-256 digital signature, X.509 certificate timestamping, and immutable credential hash stored in database.
- **Public Verification Portal:** Dedicated verification page (`https://verify.nexarats.academy/{certId}`) displaying student name, course, syllabus, issue date, and grade.
- **Social & Professional Sharing:** LinkedIn One-Click Add to Profile integration, vector PDF download, and dynamic social preview images.

### Pillar 14: Admissions, Enrollment & Finance
- **Multi-Currency Payment Processing:** Support for Stripe, Razorpay, PayPal, bank wire transfers, and localized payment options.
- **Flexible Tuition Models:** Upfront payment with discount, milestone installments, Income Share Agreements (ISA), and corporate invoicing.
- **Automated Invoicing & Tax Compliance:** Automated GST / VAT / Sales Tax calculation and compliant invoice PDF generation.
- **Refund & Scholarship Engine:** Configurable refund windows, scholarship discount codes, and financial aid approval workflows.

### Pillar 15: AI Tutors & Adaptive Learning
- **Syllabus-Grounded AI Tutor (RAG):** Context-aware conversational AI assistant trained exclusively on course lecture transcripts, documentation, and slides.
- **Code Debugging Assistant:** Explains runtime stack traces and logical errors without directly providing copy-paste solutions, fostering genuine learning.
- **Adaptive Diagnostic Quizzing:** Dynamically generates practice problems targeting a student's specific weak concepts identified in previous exams.
- **AI Voice Conversation Coach:** Simulates mock behavioral and technical screening interviews with real-time feedback on pacing and technical precision.

### Pillar 16: Learning Communities & Communication
- **Cohort Discussion Channels:** Forum threads and real-time chat channels organized by course, module, and batch.
- **Direct Messaging:** Secure 1:1 and group messaging between students, teaching assistants, and mentors.
- **Markdown & Code Syntax Highlighting:** Built-in code block rendering with copy button and interactive code snippet previews.
- **Moderation Tools:** Automated profanity and PII filters, report message workflow, and moderator shadow-banning.

### Pillar 17: Internships, Placement & Alumni Management
- **Enterprise Career Portal:** Dedicated portal for 500+ hiring partners to post openings, review pre-vetted candidate pools, and request interviews.
- **ATS Resume Builder:** Standardized, verified resume generator pulling verified Nexarats capstone projects, course certificates, and mentor ratings.
- **Interview Crucible:** Scheduling mock interview rounds with Big Tech interviewers, complete with scorecards and recorded replays.
- **Alumni Network:** Mentorship circles, alumni directory, job referral board, and continuing education discounts.

### Pillar 18: Learning Analytics & Advanced Reporting
- **Executive BI Dashboards:** Visualizations for churn risk, student engagement velocity, completion drop-offs, and Net Promoter Score (NPS).
- **Predictive At-Risk Identification:** Machine learning model flagging students falling behind in assignments or attendance for proactive intervention.
- **Skill Competency Heatmaps:** Maps student batch performance against industry skill frameworks (SFIA, Bloom's Taxonomy).
- **Scheduled Automated Exports:** CSV, Excel, and automated weekly PDF digest delivery to management.

### Pillar 19: Multi-Organization & Multi-Branch SaaS Capabilities
- **Tenant Isolation:** Rigid data segregation supporting multiple enterprise clients, academy branches, and university partners.
- **Custom Branding & White-Labeling:** Custom subdomains (`tenant.nexarats.academy`), custom logos, color schemes, and email sender addresses.
- **Hierarchical Governance:** Super Admin -> Tenant Admin -> Branch Admin -> Department Manager -> End User.

### Pillar 20: Enterprise Security, Privacy, Auditability & APIs
- **Enterprise SSO:** SAML 2.0 and OpenID Connect integration for seamless enterprise corporate login.
- **Role-Based & Attribute-Based Access Control (RBAC/ABAC):** Granular permission sets governing access to student data, grades, and financials.
- **Comprehensive Audit Logging:** Append-only log of every administrative action, data export, grade modification, and login event.
- **Developer Integration APIs:** Public REST API and Webhooks allowing corporate LMS (e.g. Cornerstone, Degreed, Workday) synchronization.

---

## 3. Essential Supporting Capabilities (Bridging the Gaps)

In addition to the 20 primary pillars, the following essential supporting systems are required for enterprise operations:
1. **SCORM 1.2 / 2004 & xAPI (Tin Can) Ingestion Engine:** Ability to upload and play standardized corporate training packages.
2. **Automated Plagiarism & LLM-Detection Engine:** Evaluates student project submissions against GitHub public repos and AI generation markers.
3. **Automated Translation & Multi-Lingual Subtitling (i18n):** Real-time subtitle generation in 10+ languages using Whisper AI for global distribution.
4. **Contract & Agreement E-Signing:** Digital signing of student enrollment terms, job assurance agreements, and corporate NDAs prior to portal entry.
5. **Event-Driven Webhook Broker:** Emits webhook events (`student.enrolled`, `exam.graded`, `certificate.issued`) to external enterprise IT systems.
