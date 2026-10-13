# Legacy Node.js / Express / Prisma Skeleton
## Provenance and Inactivity Notice

- **Origin:** Early prototype package configuration discovered during Phase 0 audit in the root `server/` directory.
- **Contents:**
  - `package.json`: Express 4.21, Prisma 6.3, Zod 3.24, tsx dev server configurations.
  - `tsconfig.json`: NodeNext compilation settings for `src/**/*`.
- **Status:** **INACTIVE & DEPRECATED.**
- **Rationale:** The approved enterprise backend for Nexarats Academy is Java 21 LTS + Spring Boot (Modular Monolith) located in `services/core-backend/`.
- **Directive:** These legacy configuration files are preserved for historical provenance only. Do not install dependencies, generate Prisma clients, or run an Express server from this directory.
