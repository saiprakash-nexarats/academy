# Nexarats Academy — AI Subsystem Workspace (Placeholder)
## Architectural Blueprint & Future Integration Boundary

- **Target Technology:** Python 3.12 + FastAPI
- **AI Tooling:** LangChain / LlamaIndex, `pgvector`, OpenAI / Anthropic APIs, Whisper AI
- **Status:** **PHASE 1 SCAFFOLD — SCHEDULED FOR PHASES 46–52 (MILESTONE 6).**

### Future Implementation Scope
- Independent asynchronous microservice communicating with Spring Boot core backend via mTLS and message queues.
- Syllabus-grounded Retrieval-Augmented Generation (RAG) conversational tutor using Server-Sent Events (SSE).
- Socratic code debugging assistant evaluating student stack traces without leaking raw solutions.
- Dynamic diagnostic quiz generation targeting student-specific conceptual weaknesses.
- Speech analysis coach for behavioral mock screening interviews.

*Note: This directory is intentionally a documented workspace boundary in Phase 1. Heavy AI frameworks are not provisioned in Phase 1 to prevent resource bloat.*
