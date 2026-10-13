-- Nexarats Academy Phase 3 Migration
-- Public Catalog, Dynamic Programs Schema, Idempotent Seed Data & Application Grants

-- 1. Program Categories Table
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

-- 2. Programs Table
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

-- 3. Grant Privileges to Non-Superuser Role (Row-Level Security & Least-Privilege Enforced)
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'academy_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE program_categories, programs TO academy_app_user;
    END IF;
END
$$;

-- 4. Idempotent Seed Data: Program Categories
INSERT INTO program_categories (slug, name, description, display_order, is_active)
VALUES
    ('software-engineering', 'Software Engineering', 'Modern web, backend, and full stack systems engineering', 1, TRUE),
    ('artificial-intelligence', 'Artificial Intelligence', 'Data science, machine learning models, and Generative AI systems', 2, TRUE),
    ('design', 'Design', 'User experience, product design, prototyping, and modern interaction systems', 3, TRUE),
    ('marketing', 'Marketing', 'Growth strategy, performance marketing, brand expansion, and tech product management', 4, TRUE)
ON CONFLICT (slug) DO NOTHING;

-- 5. Idempotent Seed Data: 8 Approved Catalog Programs
INSERT INTO programs (
    slug,
    category_id,
    title,
    short_description,
    detailed_description,
    image_asset_key,
    duration_value,
    duration_unit,
    duration_label,
    level,
    badge,
    rating,
    curriculum_overview,
    career_outcomes,
    status,
    is_featured,
    display_order,
    published_at
)
VALUES
    (
        'full-stack',
        (SELECT id FROM program_categories WHERE slug = 'software-engineering'),
        'Full Stack Development',
        'Master modern web technologies and build real-world applications.',
        'Comprehensive industry program covering modern React, TypeScript, Java/Node backend microservices, SQL databases, distributed caching, and end-to-end cloud deployments.',
        'course-fullstack',
        6,
        'MONTHS',
        '6 Months',
        'Beginner to Advanced',
        'Bestseller',
        4.90,
        'Frontend Architecture, Java/Node Microservices, PostgreSQL & Redis, CI/CD Cloud Deployment, Capstone Enterprise Project',
        'Full Stack Engineer, Frontend Engineer, Backend Developer, Software Engineer',
        'PUBLISHED',
        TRUE,
        1,
        CURRENT_TIMESTAMP
    ),
    (
        'data-science',
        (SELECT id FROM program_categories WHERE slug = 'artificial-intelligence'),
        'Data Science & AI',
        'Learn data analysis, machine learning and AI with hands-on projects.',
        'Deep dive into Python data engineering, statistical modeling, machine learning pipelines, deep learning frameworks, and practical ML deployment in enterprise settings.',
        'course-datascience',
        6,
        'MONTHS',
        '6 Months',
        'Beginner to Advanced',
        NULL,
        4.85,
        'Python Data Science, Statistical Modeling, Supervised & Unsupervised ML, Deep Learning & NLP, Real-time Production Inference',
        'Data Scientist, Machine Learning Engineer, Data Analyst, AI Specialist',
        'PUBLISHED',
        TRUE,
        2,
        CURRENT_TIMESTAMP
    ),
    (
        'ui-ux',
        (SELECT id FROM program_categories WHERE slug = 'design'),
        'UI/UX Design',
        'Create beautiful and user-friendly digital experiences.',
        'End-to-end design curriculum covering user research, information architecture, wireframing, high-fidelity Figma prototyping, interaction systems, and developer handoffs.',
        'course-uiux',
        6,
        'MONTHS',
        '6 Months',
        'Beginner to Advanced',
        NULL,
        4.92,
        'Design Thinking & Research, Wireframing & Information Architecture, Advanced Figma Systems, Usability Evaluation, Portfolio Case Studies',
        'Product Designer, UI/UX Designer, Interaction Designer, Visual Systems Designer',
        'PUBLISHED',
        TRUE,
        3,
        CURRENT_TIMESTAMP
    ),
    (
        'digital-marketing',
        (SELECT id FROM program_categories WHERE slug = 'marketing'),
        'Digital Marketing',
        'Learn modern marketing strategies to grow brands online.',
        'Modern performance marketing program focused on growth funnels, organic search optimization, paid acquisition channels, content marketing engines, and conversion analytics.',
        'course-marketing',
        4,
        'MONTHS',
        '4 Months',
        'Beginner to Advanced',
        NULL,
        4.80,
        'Growth Strategy & Funnels, SEO & Content Engines, Paid Acquisition Channels, Social Brand Authority, Web & Attribution Analytics',
        'Growth Marketer, Performance Marketing Specialist, Digital Marketing Manager, SEO Strategist',
        'PUBLISHED',
        TRUE,
        4,
        CURRENT_TIMESTAMP
    ),
    (
        'cloud-devops',
        (SELECT id FROM program_categories WHERE slug = 'software-engineering'),
        'Cloud & DevOps Engineering',
        'Master AWS, Docker, Kubernetes, and automated CI/CD deployment pipelines.',
        'Hands-on DevOps track covering Infrastructure as Code (Terraform), Docker containerization, Kubernetes cluster orchestration, AWS cloud foundations, and automated GitOps pipelines.',
        'course-fullstack',
        5,
        'MONTHS',
        '5 Months',
        'Intermediate to Advanced',
        'High Demand',
        4.88,
        'Linux & Cloud Architecture, Docker & Containerization, Kubernetes Orchestration, Terraform Infrastructure as Code, GitOps CI/CD Pipelines',
        'DevOps Engineer, Cloud Architect, Site Reliability Engineer (SRE), Platform Engineer',
        'PUBLISHED',
        FALSE,
        5,
        CURRENT_TIMESTAMP
    ),
    (
        'mobile-app',
        (SELECT id FROM program_categories WHERE slug = 'software-engineering'),
        'Mobile App Development (React Native)',
        'Build native iOS and Android apps with cross-platform excellence.',
        'Comprehensive mobile engineering program mastering React Native, TypeScript, device hardware APIs, mobile state management, offline-first data, and app store deployment.',
        'course-uiux',
        4,
        'MONTHS',
        '4 Months',
        'Beginner to Intermediate',
        NULL,
        4.82,
        'Mobile UI Components, React Native Fundamentals, Native Device Bridges, Offline Data Persistence, App Store & Play Store Publishing',
        'Mobile App Developer, React Native Specialist, Cross-Platform Engineer, iOS/Android Developer',
        'PUBLISHED',
        FALSE,
        6,
        CURRENT_TIMESTAMP
    ),
    (
        'gen-ai-engineers',
        (SELECT id FROM program_categories WHERE slug = 'artificial-intelligence'),
        'Generative AI & LLM Systems',
        'Fine-tune LLMs, build RAG pipelines, and deploy production AI agents.',
        'Advanced AI systems engineering curriculum focusing on retrieval-augmented generation (RAG), vector databases, parameter-efficient fine-tuning (LoRA), prompt engineering architectures, and autonomous agent systems.',
        'course-datascience',
        4,
        'MONTHS',
        '4 Months',
        'Intermediate to Advanced',
        'Trending',
        4.96,
        'Prompt Architecture & Foundation Models, Vector Embeddings & RAG, LLM Evaluation & Guardrails, Parameter-Efficient Fine-Tuning, Multi-Agent Autonomous Systems',
        'Generative AI Engineer, LLM Systems Architect, AI Solutions Engineer, Applied AI Researcher',
        'PUBLISHED',
        FALSE,
        7,
        CURRENT_TIMESTAMP
    ),
    (
        'product-management',
        (SELECT id FROM program_categories WHERE slug = 'marketing'),
        'Tech Product Management',
        'Lead roadmaps, agile sprints, user stories, and high-impact product launches.',
        'Strategic product management program training future tech PMs on user discovery, problem framing, agile sprint cycles, data-informed prioritization metrics, and cross-functional engineering leadership.',
        'course-marketing',
        4,
        'MONTHS',
        '4 Months',
        'Beginner to Advanced',
        NULL,
        4.79,
        'Product Discovery & Problem Validation, Market Analysis & Competitive Strategy, Agile Roadmapping & User Stories, Product Analytics & Metrics, Go-To-Market Execution',
        'Technical Product Manager, Associate PM, Product Owner, Growth Product Manager',
        'PUBLISHED',
        FALSE,
        8,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (slug) DO NOTHING;
