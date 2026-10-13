# Nexarats Academy — Flutter Mobile Application Workspace
## Architectural Blueprint & Future Integration Boundary (Phases 53–59)

> **ARCHITECTURAL DECISION NOTICE (SUPERSEDED):**  
> React Native and Expo SDK have been officially superseded and removed from the Nexarats Academy architecture (ADR-007).  
> The approved cross-platform mobile technology for Android and iOS is **Flutter + Dart**.

---

## 1. Approved Mobile Technology Stack

| Dimension | Selected Technology | Version / Standard |
|---|---|---|
| **Framework** | Flutter | Latest Stable SDK (`>= 3.24.x`) |
| **Language** | Dart | Latest Stable (`>= 3.5.x`) |
| **Design System** | Material 3 + Custom Nexarats Theme | Brand Tokens (`#2563EB`, `#1D4ED8`, `#0F1D40`, Plus Jakarta Sans) |
| **State Management** | `flutter_bloc` / `bloc` | Predictable event-driven state |
| **Routing** | `go_router` | Declarative, deep-link aware navigation |
| **Network & REST** | `dio` | HTTP client with interceptors for JWT & correlation IDs |
| **API Client** | Generated Dart OpenAPI Client | Generated from authoritative Spring Boot OpenAPI spec |
| **Local Persistence** | `drift` (SQLite) + `shared_preferences` | Type-safe offline relational database & preferences |
| **Secure Storage** | `flutter_secure_storage` | Hardware-backed Keychain (iOS) & Keystore (Android) |
| **Live Classroom** | `livekit_client` | Native WebRTC audio/video/screen share |
| **Push Notifications** | `firebase_messaging` + `flutter_local_notifications` | Push alerts & background sync triggers |
| **Crash Reporting** | `sentry_flutter` | Application telemetry, error logging, and tracing |
| **Video Playback** | `media_kit` / `video_player` | Hardware-accelerated offline/online lesson streaming |
| **Testing** | `flutter_test`, `integration_test`, `mocktail` | Unit, widget, and end-to-end integration tests |

---

## 2. Shared API Contracts & Backend Boundary

- **Single Source of Truth:** The Java 21 Spring Boot core backend (`services/core-backend/`) produces the authoritative OpenAPI 3.1 specification.
- **Polyglot Client Generation:**
  - `apps/web`: Generates / validates TypeScript API clients.
  - `apps/mobile`: Generates / validates Dart API clients using `openapi-generator-cli`.
- **Zero Client Authoritativeness:** Neither Web nor Flutter clients are authoritative for user identity, enrollment records, course progress, grades, assessment results, or payments. All business rules and RBAC permissions are strictly validated and enforced server-side.
- **Decoupled Workspaces:** `apps/mobile` does **NOT** depend on Node.js/TypeScript runtime packages.

---

## 3. Recommended Flutter Package Inventory

| Package | Purpose | Alternatives Evaluated | Rationale |
|---|---|---|---|
| `flutter_bloc` | State Management | Riverpod, Provider | Enterprise auditability, strict separation of events and states |
| `go_router` | Declarative Routing | AutoRoute, Beamer | Official Flutter team package with seamless web & mobile deep-linking |
| `dio` | HTTP & Interceptors | `http` | Built-in interceptors for JWT refresh tokens, retries, and `X-Correlation-ID` |
| `drift` | Offline Database | Hive, Isar | Type-safe SQL schema migrations, ideal for course curriculum and lesson caching |
| `flutter_secure_storage` | Key Storage | Custom native code | AES encryption on Android Keystore and iOS Keychain services |
| `livekit_client` | WebRTC Realtime | Agora, Twilio | Native LiveKit client with unified server protocol matching web |
| `sentry_flutter` | Crash Telemetry | Firebase Crashlytics | Cross-platform error tracing with breadcrumbs and release tracking |
| `google_fonts` | Typography | Asset bundling | Loads "Plus Jakarta Sans" dynamically or pre-bundled into assets |

---

## 4. Planned Project Structure (Phase 53 Implementation)

```text
apps/mobile/
├── android/                   # Native Android host project
├── ios/                       # Native iOS host project
├── lib/
│   ├── app/                   # App widget, theme, router, global blocs
│   │   ├── app.dart
│   │   ├── router.dart
│   │   └── theme.dart
│   ├── core/                  # Infrastructure, network, storage, security
│   │   ├── api/               # Dio client & OpenAPI interceptors
│   │   ├── error/             # Exception definitions (RFC 7807 client mapping)
│   │   ├── storage/           # Drift SQLite database & secure storage
│   │   └── theme/             # Nexarats brand tokens translated to ThemeData
│   ├── features/              # Feature-oriented modules (matching 42 screens)
│   │   ├── auth/              # Mobile login, biometric prompt, token lifecycle
│   │   ├── courses/           # Course catalog, search, filter, detail
│   │   ├── learning/          # Lesson player, download manager, offline sync
│   │   ├── mentorship/        # 1-on-1 mentor booking & WebRTC live sessions
│   │   ├── assessment/        # Quizzes, flashcards, diagnostic checkpoints
│   │   └── profile/           # User settings, certificates, tenant switch
│   └── main.dart              # Application entry point
├── test/                      # Unit and widget test suite
├── integration_test/          # End-to-end device integration tests
├── pubspec.yaml               # Flutter package dependencies
└── README.md                  # This architectural specification
```

*Note: Mobile screens (42 planned) will be implemented starting in Phase 53 (Milestone 7). No mobile screens are implemented during Phase 1.*
