-- Nexarats Academy Phase 4 Migration
-- Modular Course Catalog, Hierarchical Curriculum, Curriculum Versioning, Lessons & Program Relationships

-- 1. Courses Table
CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    category VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    short_description VARCHAR(500) NOT NULL,
    detailed_description TEXT,
    image_asset_key VARCHAR(100) NOT NULL,
    level VARCHAR(50) NOT NULL,
    duration_weeks INTEGER NOT NULL DEFAULT 8,
    lessons_count INTEGER NOT NULL DEFAULT 0,
    projects_count INTEGER NOT NULL DEFAULT 0,
    rating NUMERIC(3, 2) NOT NULL DEFAULT 4.90,
    reviews_count INTEGER NOT NULL DEFAULT 0,
    enrolled_students VARCHAR(50) NOT NULL DEFAULT '1,000+',
    instructor_name VARCHAR(100) NOT NULL,
    instructor_company VARCHAR(100) NOT NULL,
    badge VARCHAR(50),
    prerequisites TEXT,
    tech_stack TEXT NOT NULL DEFAULT '[]',
    learning_outcomes TEXT NOT NULL DEFAULT '[]',
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    published_version_id UUID,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    published_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_courses_duration_weeks CHECK (duration_weeks > 0),
    CONSTRAINT chk_courses_rating CHECK (rating >= 1.0 AND rating <= 5.0),
    CONSTRAINT chk_courses_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_courses_status_featured_order ON courses(status, is_featured, display_order);
CREATE INDEX IF NOT EXISTS idx_courses_category ON courses(category);
CREATE INDEX IF NOT EXISTS idx_courses_slug ON courses(slug);
CREATE INDEX IF NOT EXISTS idx_courses_level ON courses(level);
CREATE INDEX IF NOT EXISTS idx_courses_tenant_id ON courses(tenant_id);

-- 2. Curriculum Versions Table (Atomic Published Versions vs Drafts)
CREATE TABLE IF NOT EXISTS curriculum_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    version_label VARCHAR(50) NOT NULL DEFAULT 'v1.0.0',
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    changelog TEXT,
    published_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_curriculum_version UNIQUE (course_id, version_number),
    CONSTRAINT chk_curriculum_version_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_curriculum_versions_course ON curriculum_versions(course_id, status);

-- Add published_version_id Foreign Key
ALTER TABLE courses ADD CONSTRAINT fk_courses_published_version
    FOREIGN KEY (published_version_id) REFERENCES curriculum_versions(id) ON DELETE SET NULL;

-- 3. Curriculum Modules Table (Ordered Sections within a Version)
CREATE TABLE IF NOT EXISTS curriculum_modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_id UUID NOT NULL REFERENCES curriculum_versions(id) ON DELETE CASCADE,
    module_number VARCHAR(10) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_label VARCHAR(50) NOT NULL DEFAULT '2 Weeks',
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_module_order UNIQUE (version_id, display_order)
);

CREATE INDEX IF NOT EXISTS idx_curriculum_modules_version_order ON curriculum_modules(version_id, display_order);

-- 4. Lessons Table (Ordered Units with Public Preview & Locked Boundaries)
CREATE TABLE IF NOT EXISTS lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL REFERENCES curriculum_modules(id) ON DELETE CASCADE,
    lesson_title VARCHAR(255) NOT NULL,
    summary TEXT,
    lesson_type VARCHAR(50) NOT NULL DEFAULT 'LECTURE',
    duration_minutes INTEGER NOT NULL DEFAULT 30,
    is_previewable BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    preview_content TEXT,
    private_content_key VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lesson_order UNIQUE (module_id, display_order),
    CONSTRAINT chk_lesson_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_lesson_type CHECK (lesson_type IN ('LECTURE', 'EXERCISE', 'CAPSTONE', 'QUIZ', 'LAB'))
);

CREATE INDEX IF NOT EXISTS idx_lessons_module_order ON lessons(module_id, display_order);
CREATE INDEX IF NOT EXISTS idx_lessons_previewable ON lessons(module_id, is_previewable);

-- 5. Program-Courses Relationship Table (Many-to-Many Mapping)
CREATE TABLE IF NOT EXISTS program_courses (
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (program_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_program_courses_course ON program_courses(course_id);

-- 6. Role Permissions (Row-Level Security / Least-Privilege Role)
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'academy_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE courses, curriculum_versions, curriculum_modules, lessons, program_courses TO academy_app_user;
    END IF;
END
$$;

-- 7. Deterministic Idempotent Seed Data Migration
DO $$
DECLARE
    v_c1_id UUID;
    v_c2_id UUID;
    v_c3_id UUID;
    v_c4_id UUID;
    v_c5_id UUID;
    v_c6_id UUID;
    v_c7_id UUID;
    v_c8_id UUID;
    v_ver_id UUID;
    v_mod_id UUID;
    v_prog_fs UUID;
    v_prog_ds UUID;
    v_prog_ui UUID;
    v_prog_mkt UUID;
    v_prog_cld UUID;
    v_prog_ai UUID;
BEGIN
    -- Query Program IDs by slug
    SELECT id INTO v_prog_fs FROM programs WHERE slug = 'full-stack' LIMIT 1;
    SELECT id INTO v_prog_ds FROM programs WHERE slug = 'data-science' LIMIT 1;
    SELECT id INTO v_prog_ui FROM programs WHERE slug = 'ui-ux' LIMIT 1;
    SELECT id INTO v_prog_mkt FROM programs WHERE slug = 'digital-marketing' LIMIT 1;
    SELECT id INTO v_prog_cld FROM programs WHERE slug = 'cloud-devops' LIMIT 1;
    SELECT id INTO v_prog_ai FROM programs WHERE slug = 'gen-ai-engineers' LIMIT 1;

    -- Course 1: Advanced React 19, Next.js 15 & Modern Web Architecture
    SELECT id INTO v_c1_id FROM courses WHERE slug = 'react-nextjs-architecture';
    IF v_c1_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'react-nextjs-architecture',
            'Advanced React 19, Next.js 15 & Modern Web Architecture',
            'Web & Full Stack', 'Intermediate', 8, 48, 4,
            '4,850+', 4.95, 1240, 'Arjun Mehta', 'Google',
            'courseFullstackImg', 'Bestseller',
            'Master modern server components, streaming architecture, TanStack Query, concurrency, and scalable frontend state machines.',
            'Go beyond boilerplate tutorials. Build high-concurrency, enterprise-ready web apps with Next.js 15 App Router, React 19 compiler patterns, optimistic UI updates, and production Docker containerization.',
            '["React 19", "Next.js 15", "TypeScript", "Tailwind CSS", "TanStack Query", "Zustand"]',
            '["Architect full-scale production apps using Next.js 15 App Router & Server Actions", "Implement concurrent rendering, streaming Suspense, and edge functions", "Master client-side state caching with TanStack Query and optimistic rollbacks", "Optimize Web Vitals (LCP, FID, CLS) to achieve consistent 95+ Lighthouse scores"]',
            'Foundational knowledge of JavaScript (ES6+) and basic React component mechanics.',
            true, 1, 'PUBLISHED'
        ) RETURNING id INTO v_c1_id;

        -- Create Curriculum Version 1
        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c1_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c1_id;

        -- Modules & Lessons for Course 1
        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Core React 19 Internals & Rendering Lifecycles', '2 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Virtual DOM Reconciliation', 'Deep dive into virtual DOM diffing and fiber nodes.', 'LECTURE', 30, true, 1, 'Public Preview: Comprehensive guide to React 19 diffing mechanics, reconciliation heuristics, and memory footprint.'),
        (v_mod_id, 'Fiber Architecture', 'Concurrency primitives, scheduler prioritization, and time slicing.', 'LECTURE', 45, false, 2, NULL),
        (v_mod_id, 'Custom Hook Optimization', 'Writing high-performance custom hooks with zero extra re-renders.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Memory Leak Profiling', 'Chrome DevTools heap snapshot analysis and leak prevention.', 'LAB', 45, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Next.js 15 App Router & Hybrid Server/Client Components', '2 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'RSC Serialization Boundaries', 'Server vs Client components and data crossing boundaries.', 'LECTURE', 35, false, 1, NULL),
        (v_mod_id, 'Dynamic Route Segments', 'Parallel routes, intercepting routes, and layout caching.', 'LECTURE', 40, false, 2, NULL),
        (v_mod_id, 'Streaming SSR with Suspense', 'Chunked HTTP transfer encoding and partial hydration.', 'LAB', 50, false, 3, NULL),
        (v_mod_id, 'Server Actions & Mutations', 'Type-safe server actions, form validation, and revalidation.', 'LAB', 45, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Enterprise State Management & Network Caching', '2 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Zustand Scalable Stores', 'Modular slicing, selectors, and subscription optimization.', 'LECTURE', 35, false, 1, NULL),
        (v_mod_id, 'TanStack Query Infinite Pagination', 'Cursor pagination, stale-while-revalidate, and prefetching.', 'LAB', 40, false, 2, NULL),
        (v_mod_id, 'Optimistic UI Updates', 'Instant visual transitions with deterministic rollback on error.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Offline First Patterns', 'IndexedDB persistence and background sync coordination.', 'LECTURE', 40, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Production Capstone: Real-Time Collaborative Workspace', '2 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'WebSockets & CRDTs', 'Real-time collaborative editing with conflict-free replicated data types.', 'LECTURE', 50, false, 1, NULL),
        (v_mod_id, 'Dockerized Multi-Stage Builds', 'Ultra-lean production image creation and standalone output.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Automated CI/CD with GitHub Actions', 'Linting, unit testing, and zero-downtime deployment pipelines.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Mentor Code Defense', 'Presenting capstone architecture to senior industry architects.', 'CAPSTONE', 60, false, 4, NULL);

        -- Map to Program
        IF v_prog_fs IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_fs, v_c1_id, 1) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 2: Generative AI, LangChain & Autonomous Production Agents
    SELECT id INTO v_c2_id FROM courses WHERE slug = 'genai-llm-agents';
    IF v_c2_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'genai-llm-agents',
            'Generative AI, LangChain & Autonomous Production Agents',
            'AI & Machine Learning', 'Advanced', 10, 54, 5,
            '3,920+', 4.98, 980, 'Dr. Devika Sen', 'Amazon AWS',
            'courseDatascienceImg', 'Trending',
            'Build production RAG pipelines, fine-tune open-source models with LoRA, and deploy multi-agent autonomous frameworks.',
            'Bridge theoretical machine learning and cutting-edge GenAI engineering. Master prompt engineering, vector databases (Pinecone/Milvus), LangGraph multi-agent loops, and production LLMOps on AWS.',
            '["Python", "LangChain", "Llama 3", "Pinecone", "PyTorch", "FastAPI"]',
            '["Build end-to-end multi-tenant RAG systems with hybrid vector search and re-ranking", "Fine-tune open-weight models (Llama 3 / Mistral) using PEFT/LoRA techniques", "Construct stateful multi-agent autonomous loops with LangGraph and function calling", "Deploy production LLM inference endpoints with token streaming and latency guards"]',
            'Basic Python programming and familiarity with linear algebra and REST APIs.',
            true, 2, 'PUBLISHED'
        ) RETURNING id INTO v_c2_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c2_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c2_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Foundations of Modern LLMs & Prompt Engineering', '2 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Transformer Architecture Overview', 'Attention mechanisms, positional encodings, and token embeddings.', 'LECTURE', 40, true, 1, 'Public Preview: Foundational introduction to decoder-only transformers, self-attention calculations, and context windows.'),
        (v_mod_id, 'Tokenization & Attention', 'BPE tokenizers, vocabulary limits, and compute scaling laws.', 'LECTURE', 40, false, 2, NULL),
        (v_mod_id, 'Advanced Few-Shot Prompting', 'Chain-of-thought, ReAct prompting, and structured JSON output guards.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'System Prompt Security', 'Preventing prompt injections, jailbreaks, and indirect injection vectors.', 'LAB', 40, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Production Retrieval Augmented Generation (RAG)', '3 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Chunking Strategies', 'Semantic chunking, recursive character splitting, and overlap optimization.', 'LECTURE', 35, false, 1, NULL),
        (v_mod_id, 'Dense & Sparse Embeddings', 'Hybrid retrieval combining BM25 keyword matching and vector search.', 'LECTURE', 45, false, 2, NULL),
        (v_mod_id, 'Vector Indexing (HNSW / IVFFlat)', 'Approximate nearest neighbors, cosine distance, and scaling indexes.', 'LAB', 50, false, 3, NULL),
        (v_mod_id, 'Reranking with Cohere', 'Cross-encoder re-ranking for top-k contextual retrieval precision.', 'LAB', 40, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Autonomous Agents & Multi-Agent Orchestration', '3 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Tool Use & Function Calling', 'Defining JSON schemas for external API calls and database execution.', 'LECTURE', 45, false, 1, NULL),
        (v_mod_id, 'LangGraph Stateful Cycles', 'Graph-based agent loops with state persistence and branching paths.', 'LAB', 50, false, 2, NULL),
        (v_mod_id, 'Human-in-the-Loop Guards', 'Interrupting autonomous execution for human confirmation.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Evaluation with Ragas', 'Automated faithfulness, answer relevance, and context recall benchmarks.', 'LECTURE', 45, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Model Fine-Tuning & High-Throughput Deployment', '2 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'LoRA & QLoRA Quantization', 'Parameter-efficient fine-tuning on 4-bit and 8-bit quantized weights.', 'LAB', 60, false, 1, NULL),
        (v_mod_id, 'FastAPI Streaming Endpoints', 'Server-sent events (SSE) for token-by-token responsive streaming.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Token Cost & Latency Budgets', 'Production monitoring, rate limits, caching, and failover fallbacks.', 'LECTURE', 40, false, 3, NULL),
        (v_mod_id, 'Live Capstone Project', 'End-to-end deployment of an autonomous enterprise customer support agent.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_ai IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_ai, v_c2_id, 1) ON CONFLICT DO NOTHING;
        END IF;
        IF v_prog_ds IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_ds, v_c2_id, 2) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 3: Enterprise Figma & High-Fidelity UI/UX Design Systems
    SELECT id INTO v_c3_id FROM courses WHERE slug = 'figma-enterprise-design-systems';
    IF v_c3_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'figma-enterprise-design-systems',
            'Enterprise Figma & High-Fidelity UI/UX Design Systems',
            'UI/UX Design', 'All Levels', 6, 38, 3,
            '3,240+', 4.94, 840, 'Meera Nair', 'Adobe',
            'courseUiuxImg', 'High Demand',
            'Master design tokens, variables, component properties, accessibility audits, and seamless developer handoff.',
            'Learn the exact methodologies used by top design teams to build cohesive, scalable design systems. From color palettes to typography scales, interactive micro-animations, and token sync with GitHub.',
            '["Figma", "Design Tokens", "Variables", "WCAG 2.1 AAA", "FigJam", "Tokens Studio"]',
            '["Create multi-brand tokenized design systems supporting dynamic dark/light themes", "Master advanced Figma variables, component slot properties, and interactive variants", "Ensure WCAG 2.1 AAA accessibility compliance across color contrast and touch targets", "Automate developer handoffs with design-token-to-code pipelines (Style Dictionary)"]',
            'No prior design experience required. Familiarity with basic web applications is helpful.',
            false, 3, 'PUBLISHED'
        ) RETURNING id INTO v_c3_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c3_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c3_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Design Foundations & Typography Hierarchy', '1.5 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Modular Scale Grids', 'Mathematical baseline grids and fluid layout geometry.', 'LECTURE', 30, true, 1, 'Public Preview: Architectural walkthrough of 8pt grid systems, responsive layout columns, and typography scale ratios.'),
        (v_mod_id, '8pt Spacing Systems', 'Consistent spatial rhythms for layouts and component padding.', 'LECTURE', 30, false, 2, NULL),
        (v_mod_id, 'Harmonious Color Palettes', 'Tailored HSL color ramps, semantic tokens, and accessibility contrast.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Visual Hierarchy Principles', 'F-patterns, Z-patterns, typographic weights, and optical balancing.', 'LECTURE', 35, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Atomic Components & Figma Variables Architecture', '1.5 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Atoms to Organisms', 'Brad Frost atomic design methodology applied to design libraries.', 'LECTURE', 35, false, 1, NULL),
        (v_mod_id, 'Primitive vs Semantic Tokens', 'Alias token architecture for brand theming and dark mode.', 'LAB', 40, false, 2, NULL),
        (v_mod_id, 'Auto Layout Mastery', 'Advanced constraints, min/max dimensions, and wrap layouts.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Interactive Component States', 'Hover, active, focused, disabled, and loading variants.', 'LAB', 40, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Micro-Interactions, Usability Testing & Accessibility', '1.5 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Smart Animate Transitions', 'Spring physics, easing curves, and delightful UI transitions.', 'LAB', 40, false, 1, NULL),
        (v_mod_id, 'Screen Reader Navigation Audits', 'ARIA landmarks, screen reader focus order, and accessible naming.', 'LECTURE', 35, false, 2, NULL),
        (v_mod_id, 'Usability Testing Playbooks', 'Moderated and unmoderated user testing scripts and metrics.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Accessibility Checklists', 'WCAG 2.1 AAA automated validation and manual audit procedures.', 'LECTURE', 35, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Comprehensive Portfolio Capstone & Dev Handoff', '1.5 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Design Specification Docs', 'Component redlines, edge-case documentation, and API contracts.', 'LECTURE', 40, false, 1, NULL),
        (v_mod_id, 'Tokens-to-CSS Generation', 'Style Dictionary pipelines generating CSS and TypeScript tokens.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Portfolio Presentation Case Study', 'Structuring design problem statements, metrics, and outcomes.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Mentor Critique', 'Live design review with design directors from Adobe and Figma.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_ui IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_ui, v_c3_id, 1) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 4: Cloud DevOps, Docker, Kubernetes & AWS Automation
    SELECT id INTO v_c4_id FROM courses WHERE slug = 'cloud-devops-kubernetes';
    IF v_c4_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'cloud-devops-kubernetes',
            'Cloud DevOps, Docker, Kubernetes & AWS Automation',
            'Cloud & DevOps', 'Intermediate', 8, 44, 4,
            '3,480+', 4.92, 910, 'Rishi Kapoor', 'Atlassian',
            'courseFullstackImg', 'Bestseller',
            'Containerize microservices, orchestrate high-availability clusters with K8s, and build automated CI/CD with Terraform.',
            'Step into the shoes of a Cloud Platform Engineer. Learn how high-traffic internet companies achieve five-nines uptime, zero-downtime rolling deployments, automated autoscaling, and secure secrets management.',
            '["Docker", "Kubernetes", "AWS", "Terraform", "Helm", "Prometheus"]',
            '["Write production-grade multi-stage Dockerfiles with minimal footprint and security scanning", "Deploy and scale resilient Kubernetes deployments, statefulsets, ingress, and services", "Automate cloud infrastructure as code (IaC) across AWS with reusable Terraform modules", "Set up full observability stacks using Prometheus, Grafana, and automated alerts"]',
            'Familiarity with basic Linux terminal commands and basic networking concepts.',
            true, 4, 'PUBLISHED'
        ) RETURNING id INTO v_c4_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c4_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c4_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Linux Fundamentals & Docker Containerization', '2 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Linux CLI Deep Dive', 'Processes, permissions, bash scripting, and networking tools.', 'LECTURE', 35, true, 1, 'Public Preview: Essential Linux administration commands, systemd services, SSH key hardening, and firewall rules.'),
        (v_mod_id, 'Docker Engine & Layers', 'Namespaces, cgroups, image caching, and union filesystems.', 'LECTURE', 40, false, 2, NULL),
        (v_mod_id, 'Multi-Stage Optimization', 'Shrinking production Docker containers with multi-stage scratch builds.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Docker Compose Microservices', 'Local multi-container networks, volume binds, and environment secrets.', 'LAB', 40, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Kubernetes Cluster Architecture & Orchestration', '2 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Pods, Deployments & ReplicaSets', 'Control plane components, kubelet, and declarative self-healing.', 'LECTURE', 45, false, 1, NULL),
        (v_mod_id, 'Cluster Networking & Ingress Controllers', 'CoreDNS, service meshes, NGINX Ingress, and TLS cert-manager.', 'LAB', 50, false, 2, NULL),
        (v_mod_id, 'ConfigMaps & Secrets', 'Externalized configuration, HashiCorp Vault integration, and KMS.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Persistent Volumes', 'CSI drivers, storage classes, and stateful database workloads.', 'LECTURE', 45, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Infrastructure as Code (IaC) with Terraform & AWS', '2 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'VPC, Subnets & Security Groups', 'Public/private network segmentation, NAT Gateways, and route tables.', 'LECTURE', 40, false, 1, NULL),
        (v_mod_id, 'Terraform State Management', 'Remote state locking with S3 and DynamoDB, workspaces, and modules.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'EKS Provisioning', 'Managed Kubernetes node groups, OIDC role bindings, and cluster autoscaler.', 'LAB', 55, false, 3, NULL),
        (v_mod_id, 'IAM Least Privilege Security', 'Service account IAM policies (IRSA), trust relationships, and SCPs.', 'LECTURE', 40, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'CI/CD Pipelines & Site Reliability Engineering (SRE)', '2 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'GitHub Actions Workflows', 'Reusable workflows, matrix builds, automated vulnerability scans, and OIDC.', 'LAB', 45, false, 1, NULL),
        (v_mod_id, 'Helm Charts Packaging', 'Templatized manifests, value overrides, and Helm release rollback.', 'LAB', 40, false, 2, NULL),
        (v_mod_id, 'Prometheus & Grafana Monitoring', 'PromQL metrics, custom dashboards, alertmanager, and SLA/SLO tracking.', 'LAB', 50, false, 3, NULL),
        (v_mod_id, 'Chaos Testing Capstone', 'Simulating node failures, pod evictions, and rolling upgrades defense.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_cld IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_cld, v_c4_id, 1) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 5: High-Throughput Microservices & Event-Driven Architecture
    SELECT id INTO v_c5_id FROM courses WHERE slug = 'backend-microservices-kafka';
    IF v_c5_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'backend-microservices-kafka',
            'High-Throughput Microservices & Event-Driven Architecture',
            'Web & Full Stack', 'Advanced', 8, 42, 4,
            '2,950+', 4.94, 760, 'Vikram Singhania', 'Razorpay',
            'courseDatascienceImg', 'High Demand',
            'Build event-driven distributed systems using Apache Kafka, Redis pub/sub, PostgreSQL sharding, and gRPC.',
            'Architect systems that scale to millions of concurrent users. Learn database transaction isolation levels, distributed locking, idempotent payment webhooks, and fault-tolerant event streams.',
            '["Node.js", "Go", "Apache Kafka", "PostgreSQL", "Redis", "gRPC"]',
            '["Design event-driven systems using Apache Kafka topics, consumer groups, and partitions", "Optimize database queries, B-tree indexes, connection pools, and read replicas in Postgres", "Build lightning-fast inter-service communication with gRPC and Protocol Buffers", "Implement distributed rate limiters, circuit breakers, and idempotent retry queues"]',
            'Proficiency in backend JavaScript/TypeScript or Go, and relational database basics.',
            false, 5, 'PUBLISHED'
        ) RETURNING id INTO v_c5_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c5_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c5_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Distributed Systems Patterns & Concurrency', '2 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'CAP Theorem Realities', 'Consistency vs Availability trade-offs in distributed financial systems.', 'LECTURE', 40, true, 1, 'Public Preview: Architectural breakdown of PACELC theorem, eventual consistency, and quorum reads.'),
        (v_mod_id, 'ACID vs BASE Models', 'Saga pattern, two-phase commit pitfalls, and compensating transactions.', 'LECTURE', 40, false, 2, NULL),
        (v_mod_id, 'Connection Pooling', 'HikariCP sizing, max lifetime tuning, and socket timeouts under load.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Deadlock Diagnostics', 'PostgreSQL lock monitoring, transaction ordering, and deadlock mitigation.', 'LAB', 40, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Event Streaming with Apache Kafka', '2 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Kafka Cluster Anatomy', 'Brokers, KRaft consensus, partition leaders, and in-sync replicas (ISR).', 'LECTURE', 45, false, 1, NULL),
        (v_mod_id, 'Message Retention & Partitions', 'Partition keys, consumer lag, rebalancing, and compacted topics.', 'LAB', 50, false, 2, NULL),
        (v_mod_id, 'Exactly-Once Processing', 'Idempotent producers, transactional outbox pattern, and Kafka Streams.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Schema Registry', 'Avro serialization, backward/forward compatibility, and contract testing.', 'LECTURE', 40, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Caching, Sharding & High-Speed Data Stores', '2 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Redis Cluster Caching Strategies', 'Cache-aside, write-through, cache stampede prevention with probabilistic early expiration.', 'LECTURE', 40, false, 1, NULL),
        (v_mod_id, 'Distributed Locks with Redlock', 'Safe multi-node distributed locking algorithms and TTL fencing tokens.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'PostgreSQL Partitioning', 'Declarative range/hash table partitioning, indexing, and query plan pruning.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Read-Write Splitting', 'Streaming replication, replication lag handling, and replica connection pools.', 'LECTURE', 40, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Fintech Capstone: Real-Time Payment Settlement Engine', '2 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Idempotency Keys', 'Deduplication tables, unique request tokens, and race condition prevention.', 'LAB', 45, false, 1, NULL),
        (v_mod_id, 'Webhook Delivery Pipeline', 'Exponential backoff retries, dead-letter queues, and HMAC signature verification.', 'LAB', 50, false, 2, NULL),
        (v_mod_id, 'Stress Testing with k6', 'Load testing at 20,000 requests per second and p99 latency profiling.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Architectural Defense', 'Defending the payment processing design in front of a staff engineer panel.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_fs IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_fs, v_c5_id, 2) ON CONFLICT DO NOTHING;
        END IF;
        IF v_prog_cld IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_cld, v_c5_id, 2) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 6: Data Analytics, BigQuery SQL & Power BI Dashboards
    SELECT id INTO v_c6_id FROM courses WHERE slug = 'data-analytics-bi-sql';
    IF v_c6_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'data-analytics-bi-sql',
            'Data Analytics, BigQuery SQL & Power BI Dashboards',
            'Data & Analytics', 'Beginner', 6, 36, 3,
            '4,120+', 4.90, 1100, 'Rahul Verma', 'Amazon',
            'courseMarketingImg', 'Bestseller',
            'From complex SQL window functions to Google BigQuery, ETL pipelines, and executive dashboards.',
            'Turn messy, high-volume enterprise data into actionable business intelligence. Master window functions, CTEs, automated data cleaning with Python Pandas, and interactive executive reporting.',
            '["Advanced SQL", "Google BigQuery", "Python Pandas", "Power BI", "Tableau"]',
            '["Write high-performance SQL queries with window functions, CTEs, and self-joins", "Analyze multi-gigabyte datasets inside Google BigQuery with partitioned tables", "Build automated ETL pipelines using Python Pandas and scheduled cron jobs", "Design interactive C-suite executive dashboards in Power BI with drill-down KPIs"]',
            'No prior programming experience required. Basic familiarity with spreadsheet software is helpful.',
            false, 6, 'PUBLISHED'
        ) RETURNING id INTO v_c6_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c6_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c6_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Advanced SQL Querying & Relational Databases', '1.5 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Multi-Table Joins', 'Inner, outer, cross, and self joins with query plan optimization.', 'LECTURE', 35, true, 1, 'Public Preview: Master relational algebra, relational joins, indexing strategies, and execution plan benchmarks.'),
        (v_mod_id, 'Aggregations & Grouping', 'GROUP BY, HAVING, GROUPING SETS, ROLLUP, and CUBE aggregations.', 'LECTURE', 35, false, 2, NULL),
        (v_mod_id, 'Window Functions (RANK, LAG, LEAD)', 'Running totals, moving averages, and analytical ranking functions.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Subqueries & CTEs', 'Correlated subqueries, recursive common table expressions, and views.', 'LAB', 40, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Cloud Data Warehousing with Google BigQuery', '1.5 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Data Lake vs Data Warehouse', 'Columnar storage formats, OLTP vs OLAP architectures, and schema design.', 'LECTURE', 35, false, 1, NULL),
        (v_mod_id, 'Partitioned & Clustered Tables', 'Optimizing byte scanning, query costs, and clustering keys.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Cost Optimization', 'Slot reservation, caching, query dry-runs, and resource quotas.', 'LECTURE', 35, false, 3, NULL),
        (v_mod_id, 'Public Datasets Mining', 'Querying multi-terabyte public datasets with BigQuery SQL.', 'LAB', 45, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Python for Exploratory Data Analysis (EDA)', '1.5 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Pandas Data Wrangling', 'DataFrame slicing, groupby, pivot tables, and string manipulations.', 'LAB', 45, false, 1, NULL),
        (v_mod_id, 'Missing Value Imputation', 'Handling null values with statistical means, medians, and forward fills.', 'LAB', 40, false, 2, NULL),
        (v_mod_id, 'Statistical Outlier Detection', 'Z-score and IQR anomaly detection techniques.', 'LECTURE', 35, false, 3, NULL),
        (v_mod_id, 'Seaborn Visualizations', 'Heatmaps, pairplots, distribution plots, and correlation matrices.', 'LAB', 40, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Business Intelligence Dashboard Capstone', '1.5 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'DAX Expressions in Power BI', 'Calculated columns, measures, and time intelligence functions in DAX.', 'LAB', 45, false, 1, NULL),
        (v_mod_id, 'Executive KPI Cards', 'Interactive slicers, drill-through pages, and executive visual hierarchy.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Automated Refresh Schedules', 'Power BI Gateway configuration and scheduled ETL orchestration.', 'LECTURE', 35, false, 3, NULL),
        (v_mod_id, 'Stakeholder Presentation', 'Delivering executive business insights from real corporate data.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_ds IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_ds, v_c6_id, 1) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 7: Omnichannel Performance Marketing & Attribution Modeling
    SELECT id INTO v_c7_id FROM courses WHERE slug = 'growth-marketing-attribution';
    IF v_c7_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'growth-marketing-attribution',
            'Omnichannel Performance Marketing & Attribution Modeling',
            'Marketing', 'Beginner', 5, 32, 3,
            '2,640+', 4.88, 650, 'Tanvi Joshi', 'Stripe',
            'courseMarketingImg', 'New',
            'Master full-funnel customer acquisition, Meta & Google Ads optimization, GA4, and marketing attribution modeling.',
            'Stop wasting ad budgets. Learn rigorous data-backed growth frameworks, conversion rate optimization (CRO), custom audience lookalikes, multi-touch attribution, and retention loops.',
            '["Google Ads", "Meta Ads Manager", "Google Analytics 4", "Looker Studio", "AppsFlyer"]',
            '["Run profitable performance marketing campaigns on Google Search, YouTube, and Meta", "Configure GA4 custom event tracking, conversion funnels, and retention cohorts", "Implement multi-touch attribution models to allocate marketing spend effectively", "Conduct rigorous A/B split tests with statistical significance calculations"]',
            'No prior technical experience required. Interest in growth and data-driven marketing.',
            false, 7, 'PUBLISHED'
        ) RETURNING id INTO v_c7_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c7_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c7_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Paid Acquisition Engines (Meta & Google Ads)', '1.5 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Auction Mechanics', 'Ad rank formulas, quality scores, bid strategies, and cost-per-click optimization.', 'LECTURE', 35, true, 1, 'Public Preview: Understand ad auction algorithms, CTR multipliers, and budget pacing mechanics.'),
        (v_mod_id, 'Audience Segmentation', 'Custom audiences, lookalikes, affinity segments, and exclusions.', 'LAB', 40, false, 2, NULL),
        (v_mod_id, 'Creative Fatigue Management', 'Ad creative testing matrices, hook rates, and asset rotation cycles.', 'LECTURE', 30, false, 3, NULL),
        (v_mod_id, 'Target CPA & ROAS Bidding', 'Automated smart bidding strategies and conversion value rules.', 'LAB', 40, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Conversion Rate Optimization (CRO) & Funnels', '1 Week', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Landing Page Teardowns', 'Conversion copy principles, call-to-action placement, and form friction.', 'LECTURE', 30, false, 1, NULL),
        (v_mod_id, 'Heatmaps & User Recording Analysis', 'Click tracking, scroll depth maps, and user drop-off diagnostic sessions.', 'LAB', 35, false, 2, NULL),
        (v_mod_id, 'A/B Test Design', 'Sample size estimation, statistical power, and hypothesis construction.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Friction Point Removal', 'Checkout optimization, page speed impact, and mobile UX refinement.', 'LECTURE', 30, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'GA4 Tracking & Marketing Attribution', '1.5 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Tag Manager Deployment', 'Google Tag Manager triggers, variables, and dataLayer events.', 'LAB', 45, false, 1, NULL),
        (v_mod_id, 'Custom Dimension Configuration', 'Event-scoped parameters, user properties, and reporting views.', 'LAB', 40, false, 2, NULL),
        (v_mod_id, 'First-Touch vs Data-Driven Attribution', 'Evaluating linear, position-based, and algorithmic attribution models.', 'LECTURE', 35, false, 3, NULL),
        (v_mod_id, 'Looker Dashboards', 'Building automated marketing performance dashboards in Looker Studio.', 'LAB', 40, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Full-Funnel Capstone: $50,000 Campaign Simulation', '1 Week', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Budget Allocation Matrix', 'Balancing top-of-funnel discovery with bottom-of-funnel retargeting.', 'LAB', 40, false, 1, NULL),
        (v_mod_id, 'Unit Economics & LTV/CAC Ratio', 'Customer acquisition cost payback periods and cohort retention models.', 'LECTURE', 35, false, 2, NULL),
        (v_mod_id, 'Executive Report Defense', 'Presenting multi-channel ad spend recommendations to leadership.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'Mentor Feedback', 'Review and scorecard evaluation with Stripe growth marketing mentors.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_mkt IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_mkt, v_c7_id, 1) ON CONFLICT DO NOTHING;
        END IF;
    END IF;

    -- Course 8: Applied Cyber Security & Secure Software Architecture
    SELECT id INTO v_c8_id FROM courses WHERE slug = 'cyber-security-secure-coding';
    IF v_c8_id IS NULL THEN
        INSERT INTO courses (
            slug, title, category, level, duration_weeks, lessons_count, projects_count,
            enrolled_students, rating, reviews_count, instructor_name, instructor_company,
            image_asset_key, badge, short_description, detailed_description,
            tech_stack, learning_outcomes, prerequisites, is_featured, display_order, status
        ) VALUES (
            'cyber-security-secure-coding',
            'Applied Cyber Security & Secure Software Architecture',
            'Cyber Security', 'Intermediate', 7, 38, 4,
            '2,150+', 4.93, 520, 'Kunal Roy', 'AWS',
            'courseFullstackImg', 'Trending',
            'Master the OWASP Top 10 vulnerabilities, penetration testing, OAuth 2.0 / OpenID Connect, and secure code audits.',
            'Build software that stands up against sophisticated cyber attacks. Hands-on labs simulating SQL injection, cross-site scripting (XSS), CSRF, cryptographic misconfigurations, and supply-chain vulnerabilities.',
            '["OWASP Top 10", "Burp Suite", "OAuth 2.0", "JWT Security", "SonarQube", "Linux"]',
            '["Identify, exploit, and remediate all OWASP Top 10 vulnerabilities in real web apps", "Implement enterprise authentication flows with OAuth 2.0, PKCE, and signed JWT tokens", "Perform automated static and dynamic application security testing (SAST/DAST)", "Design zero-trust backend architectures with end-to-end encryption and audit logging"]',
            'Familiarity with web application development (HTTP, cookies, databases).',
            false, 8, 'PUBLISHED'
        ) RETURNING id INTO v_c8_id;

        INSERT INTO curriculum_versions (course_id, version_number, version_label, status, changelog)
        VALUES (v_c8_id, 1, 'v1.0.0', 'PUBLISHED', 'Initial approved curriculum baseline')
        RETURNING id INTO v_ver_id;

        UPDATE courses SET published_version_id = v_ver_id WHERE id = v_c8_id;

        -- Module 01
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '01', 'Web Vulnerabilities & The OWASP Top 10', '2 Weeks', 1)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'SQLi & NoSQL Injections', 'Prepared statements, parameterized queries, and input sanitization.', 'LECTURE', 40, true, 1, 'Public Preview: Comprehensive deep dive into SQL injection vectors, blind SQLi, and defensive ORM configurations.'),
        (v_mod_id, 'Stored & Reflected XSS', 'Context-aware output encoding, DOM XSS, and Content Security Policy (CSP).', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'CSRF Tokens & SameSite Cookies', 'Synchronizer token pattern, SameSite cookie attributes, and Double Submit Cookie defense.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'SSRF Exploitation', 'Server-side request forgery mitigation, IP allowlisting, and cloud metadata defense.', 'LECTURE', 40, false, 4, NULL);

        -- Module 02
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '02', 'Modern Identity, Authentication & Cryptography', '1.5 Weeks', 2)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'OAuth 2.0 & OpenID Connect', 'Authorization Code flow with PKCE, token refresh rotation, and scopes.', 'LECTURE', 45, false, 1, NULL),
        (v_mod_id, 'JWT Signature Verification', 'HMAC vs RSA/ECDSA signing keys, jti replay tracking, and alg=none attacks.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Bcrypt & Argon2 Hashing', 'Memory-hard password hashing parameters, salt generation, and dictionary resistance.', 'LAB', 40, false, 3, NULL),
        (v_mod_id, 'TLS Handshakes & HTTPS', 'Cipher suites, forward secrecy, certificate authorities, and HSTS headers.', 'LECTURE', 35, false, 4, NULL);

        -- Module 03
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '03', 'Security Auditing, Penetration Testing & Burp Suite', '2 Weeks', 3)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Burp Suite Proxy Interception', 'HTTP request tampering, Repeater, Intruder, and spidering application targets.', 'LAB', 50, false, 1, NULL),
        (v_mod_id, 'API Vulnerability Testing', 'Broken object level authorization (BOLA/IDOR) and mass assignment detection.', 'LAB', 45, false, 2, NULL),
        (v_mod_id, 'Automated SAST with SonarQube', 'Static code analysis, vulnerability rulesets, and pull request gating.', 'LECTURE', 35, false, 3, NULL),
        (v_mod_id, 'Dependency Vulnerabilities', 'Software bill of materials (SBOM), Dependabot, and CVE remediation.', 'LAB', 40, false, 4, NULL);

        -- Module 04
        INSERT INTO curriculum_modules (version_id, module_number, title, duration_label, display_order)
        VALUES (v_ver_id, '04', 'Defensive Architecture Capstone: Vulnerable App Hardening', '1.5 Weeks', 4)
        RETURNING id INTO v_mod_id;
        INSERT INTO lessons (module_id, lesson_title, summary, lesson_type, duration_minutes, is_previewable, display_order, preview_content)
        VALUES
        (v_mod_id, 'Codebase Security Audit', 'Auditing legacy applications for architectural vulnerabilities and hardcoded secrets.', 'LAB', 45, false, 1, NULL),
        (v_mod_id, 'Patching Exploits', 'Refactoring insecure queries, adding CSRF validation, and updating hashing algorithms.', 'LAB', 50, false, 2, NULL),
        (v_mod_id, 'Zero-Trust Policy Deployment', 'Mutual TLS, least-privilege role-based access, and cryptographic audit logs.', 'LAB', 45, false, 3, NULL),
        (v_mod_id, 'Security Defense Report', 'Delivering a penetration testing and remediation audit defense report.', 'CAPSTONE', 60, false, 4, NULL);

        IF v_prog_cld IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_cld, v_c8_id, 3) ON CONFLICT DO NOTHING;
        END IF;
        IF v_prog_fs IS NOT NULL THEN
            INSERT INTO program_courses (program_id, course_id, display_order) VALUES (v_prog_fs, v_c8_id, 3) ON CONFLICT DO NOTHING;
        END IF;
    END IF;
END
$$;
