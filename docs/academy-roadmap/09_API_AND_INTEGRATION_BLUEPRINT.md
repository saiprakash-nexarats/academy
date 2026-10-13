# NEXARATS ACADEMY — API & INTEGRATION BLUEPRINT
## RESTful Endpoints, RFC 7807 Error Standards, Webhooks, WebSockets, and External Integrations

- **Document ID:** `09_API_AND_INTEGRATION_BLUEPRINT`
- **Execution Date:** 2026-10-13
- **Classification:** Enterprise API Architecture & Integration Specification

---

## 1. API Design Standards & Conventions

### 1.1 Architecture Standards
- **Protocol:** HTTPS (TLS 1.3 mandatory).
- **Format:** JSON (`application/json; charset=utf-8`).
- **Base URI Structure:** `https://api.nexarats.academy/api/v1/{module}/{resource}`
- **Authentication:** Bearer token (`Authorization: Bearer <JWT>`) for authenticated routes; OAuth2 / API keys for machine-to-machine integrations.
- **Tenant Scoping:** Explicit header `X-Tenant-ID: <uuid>` or extracted directly from verified JWT claims (`tenant_id`).
- **Idempotency:** Supported on all `POST` and `PUT` payment and enrollment mutations via `Idempotency-Key: <uuid>`.

### 1.2 RFC 7807 Problem Details Error Standard
All error responses adhere strictly to the IETF RFC 7807 standard:

```json
{
  "type": "https://api.nexarats.academy/errors/insufficient-mentorship-credits",
  "title": "Insufficient Mentorship Credits",
  "status": 402,
  "detail": "Your account has 0 available 1:1 mentorship credits. Please purchase additional credits to schedule this session.",
  "instance": "/api/v1/mentorship/sessions/book",
  "timestamp": "2026-10-13T07:45:00Z",
  "tenantId": "d3b07384-d113-4f44-9989-e137456d25f3",
  "errorCode": "MENTOR_CREDIT_DEPLETED",
  "invalidParams": []
}
```

---

## 2. Core REST API Endpoint Catalog

### 2.1 Authentication & Identity (`/api/v1/auth`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Authenticate with email/password; returns JWT + sets HttpOnly cookie | Public |
| `POST` | `/api/v1/auth/refresh` | Exchange valid refresh token cookie for new access token | Public |
| `POST` | `/api/v1/auth/logout` | Invalidate active refresh token in Redis | Authenticated |
| `POST` | `/api/v1/auth/mfa/verify` | Submit TOTP / Authenticator code during login challenge | Public (Pre-auth) |
| `POST` | `/api/v1/auth/sso/saml/callback` | Enterprise SAML 2.0 assertion consumer service callback | Public |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile, roles, and active tenant context | Authenticated |

### 2.2 Admissions & CRM (`/api/v1/admissions`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/admissions/inquiries` | Submit public prospective student inquiry form (from landing page) | Public |
| `POST` | `/api/v1/admissions/applications` | Submit detailed multi-step bootcamp enrollment application | Public |
| `GET` | `/api/v1/admissions/leads` | List lead pipeline with stage filters (Kanban CRM) | `ROLE_COUNSELOR` |
| `POST` | `/api/v1/admissions/counseling/book` | Schedule 1:1 counseling consultation session | Public |
| `POST` | `/api/v1/admissions/offers/issue` | Issue official admission letter with tuition terms | `ROLE_ACADEMY_ADMIN` |

### 2.3 Programs & Course Curriculum (`/api/v1/academic`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/academic/programs` | List published career programs (public catalog) | Public |
| `GET` | `/api/v1/academic/programs/{slug}` | Get detailed program syllabus, cohorts, and mentor lineup | Public |
| `GET` | `/api/v1/academic/courses` | List modular skill courses with category/level filters | Public |
| `GET` | `/api/v1/academic/courses/{slug}/syllabus` | Get complete module sequence and lesson previews | Public |
| `POST` | `/api/v1/academic/courses` | Create new course draft | `ROLE_ACADEMY_ADMIN` |
| `PUT` | `/api/v1/academic/modules/{id}/sequence` | Update lesson sequence ordering via drag-and-drop | `ROLE_ACADEMY_ADMIN` |

### 2.4 Student Learning & LMS (`/api/v1/learning`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/learning/my-courses` | List enrolled courses with progress percentages | `ROLE_STUDENT` |
| `GET` | `/api/v1/learning/courses/{id}/curriculum` | Get full course player manifest with video tokens | `ROLE_STUDENT` |
| `POST` | `/api/v1/learning/lessons/{id}/progress` | Record watch time or mark lesson completed | `ROLE_STUDENT` |
| `GET` | `/api/v1/learning/assignments` | List pending and submitted assignments | `ROLE_STUDENT` |
| `POST` | `/api/v1/learning/assignments/{id}/submit` | Submit GitHub repository link or file submission | `ROLE_STUDENT` |

### 2.5 Live Classrooms & WebRTC (`/api/v1/classrooms`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/classrooms/upcoming` | List upcoming scheduled live cohort lectures | `ROLE_STUDENT` |
| `POST` | `/api/v1/classrooms/{id}/token` | Generate signed LiveKit WebRTC room connection token | `ROLE_STUDENT` |
| `POST` | `/api/v1/classrooms/{id}/start` | Instructor launches room & triggers recording service | `ROLE_MENTOR` |
| `POST` | `/api/v1/classrooms/{id}/terminate` | Instructor closes room & initiates HLS transcoding | `ROLE_MENTOR` |
| `GET` | `/api/v1/classrooms/{id}/recording` | Retrieve CloudFront signed HLS playback URL for past class | `ROLE_STUDENT` |

### 2.6 Mentorship & 1:1 Sessions (`/api/v1/mentorship`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/mentorship/mentors` | Search and filter mentor directory with verified ratings | Public |
| `GET` | `/api/v1/mentorship/mentors/{id}/slots` | Fetch real-time available time slots for selected mentor | Authenticated |
| `POST` | `/api/v1/mentorship/sessions/book` | Book 1:1 session (atomic credit deduction) | `ROLE_STUDENT` |
| `POST` | `/api/v1/mentorship/sessions/{id}/feedback` | Mentor submits post-call student feedback & notes | `ROLE_MENTOR` |
| `GET` | `/api/v1/mentorship/earnings` | Mentor wallet summary and monthly compensation ledger | `ROLE_MENTOR` |

### 2.7 Assessments, Exams & Sandboxes (`/api/v1/assessments`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/assessments/{id}/start` | Start timed exam; initializes proctoring session | `ROLE_STUDENT` |
| `POST` | `/api/v1/assessments/{id}/submit` | Submit answers for auto-grading (MCQ + coding) | `ROLE_STUDENT` |
| `POST` | `/api/v1/assessments/sandbox/execute` | Dispatch student code snippet to Judge0 for test runs | `ROLE_STUDENT` |
| `POST` | `/api/v1/assessments/proctoring/heartbeat` | Send webcam snapshot & browser tab focus telemetry | `ROLE_STUDENT` |

### 2.8 Credentials & Verification (`/api/v1/certificates`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/certificates/my-certificates` | List earned certificates with PDF download links | `ROLE_STUDENT` |
| `GET` | `/api/v1/certificates/verify/{code}` | Public credential verification endpoint (no auth needed) | Public |
| `GET` | `/api/v1/certificates/{id}/pdf` | Stream signed high-resolution vector PDF certificate | Authenticated |
| `POST` | `/api/v1/certificates/issue` | Programmatically issue certificate on passing capstone | `ROLE_ACADEMY_ADMIN` |

### 2.9 Finance & Payments (`/api/v1/finance`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/finance/checkout/create-intent` | Initialize Stripe PaymentIntent / Razorpay Order | `ROLE_STUDENT` |
| `GET` | `/api/v1/finance/invoices` | List invoices and upcoming installment due dates | `ROLE_STUDENT` |
| `GET` | `/api/v1/finance/invoices/{id}/pdf` | Download official tax-compliant invoice receipt | `ROLE_STUDENT` |
| `POST` | `/api/v1/finance/refunds/request` | Submit course tuition refund request | `ROLE_STUDENT` |

### 2.10 AI Tutor & RAG Subsystem (`/api/v1/ai`)
| Method | Endpoint | Description | Auth Level |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/ai/tutor/chat` | Send question to syllabus-grounded RAG AI tutor (SSE streaming) | `ROLE_STUDENT` |
| `POST` | `/api/v1/ai/code-debug/explain` | Analyze error stack trace without revealing direct solution | `ROLE_STUDENT` |
| `POST` | `/api/v1/ai/diagnostic/generate-quiz` | Dynamically generate practice quiz targeting weak concepts | `ROLE_STUDENT` |

---

## 3. Webhook Architecture & Event Dispatches

### 3.1 Inbound Third-Party Webhooks
| Source | Inbound Route | Purpose | Verification Mechanism |
| :--- | :--- | :--- | :--- |
| **Stripe** | `POST /api/v1/webhooks/stripe` | Handle `payment_intent.succeeded`, `invoice.payment_failed` | HMAC SHA-256 header `Stripe-Signature` |
| **Razorpay** | `POST /api/v1/webhooks/razorpay` | Handle `payment.captured`, `order.paid` | HMAC SHA-256 header `X-Razorpay-Signature` |
| **LiveKit** | `POST /api/v1/webhooks/livekit` | Handle `room_started`, `room_finished`, `participant_joined` | JWT Token in `Authorization` header |
| **Okta / SCIM** | `POST /api/v1/webhooks/scim/v2/Users` | Automated enterprise employee provisioning/deprovisioning | Bearer SCIM API Secret Token |

### 3.2 Outbound Enterprise Webhooks (Published by Nexarats)
Enterprise clients can subscribe to webhook events via the Corporate Admin Portal.

| Event Type | Triggering Action | Payload Schema |
| :--- | :--- | :--- |
| `employee.enrolled` | Employee starts assigned corporate track | `{ eventId, tenantId, employeeId, courseId, timestamp }` |
| `employee.compliance_completed` | Employee completes mandatory compliance module | `{ eventId, tenantId, employeeId, courseId, score, completedAt }` |
| `employee.compliance_overdue` | Employee breaches compliance deadline | `{ eventId, tenantId, employeeId, courseId, daysOverdue }` |
| `student.graduated` | Student earns certified career credential | `{ eventId, tenantId, studentId, programId, certificateCode }` |

---

## 4. WebSocket & Real-Time Event Protocols

### 4.1 Connection Handshake
- **Endpoint:** `wss://api.nexarats.academy/api/v1/ws`
- **Handshake Protocol:** STOMP over WebSocket with JWT authentication in the `CONNECT` frame:
  ```text
  CONNECT
  accept-version:1.2
  Authorization:Bearer <jwt_token>
  X-Tenant-ID:<uuid>
  \u0000
  ```

### 4.2 STOMP Subscription Channels
1. `/user/queue/notifications` — Private user notification stream (class starting in 15 mins, assignment graded).
2. `/topic/cohorts.{batchId}.chat` — Real-time group chat inside live cohort classrooms.
3. `/topic/cohorts.{batchId}.polls` — Instant poll distribution and live vote tally updates.
4. `/user/queue/exam-proctoring` — Proctoring telemetry commands (e.g. examiner warning message).
