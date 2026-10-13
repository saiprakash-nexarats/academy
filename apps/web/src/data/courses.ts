import courseFullstackImg from '@/assets/course-fullstack.jpg'
import courseDatascienceImg from '@/assets/course-datascience.jpg'
import courseUiuxImg from '@/assets/course-uiux.jpg'
import courseMarketingImg from '@/assets/course-marketing.jpg'

export interface CourseModule {
  number: string
  title: string
  duration: string
  topics: string[]
}

export interface CourseItem {
  id: string
  title: string
  category: 'Web & Full Stack' | 'AI & Machine Learning' | 'UI/UX Design' | 'Cloud & DevOps' | 'Data & Analytics' | 'Marketing' | 'Cyber Security'
  level: 'Beginner' | 'Intermediate' | 'Advanced' | 'All Levels'
  lessonsCount: number
  durationWeeks: number
  enrolledStudents: string
  rating: number
  reviewsCount: number
  instructor: {
    name: string
    company: string
  }
  image: string
  shortDescription: string
  detailedDescription: string
  techStack: string[]
  badge?: 'Bestseller' | 'Trending' | 'New' | 'High Demand'
  projectsCount: number
  modules: CourseModule[]
  learningOutcomes: string[]
  prerequisites: string
}

export const COURSES_CATALOG: CourseItem[] = [
  {
    id: 'react-nextjs-architecture',
    title: 'Advanced React 19, Next.js 15 & Modern Web Architecture',
    category: 'Web & Full Stack',
    level: 'Intermediate',
    lessonsCount: 48,
    durationWeeks: 8,
    enrolledStudents: '4,850+',
    rating: 4.95,
    reviewsCount: 1240,
    instructor: {
      name: 'Arjun Mehta',
      company: 'Google',
    },
    image: courseFullstackImg,
    badge: 'Bestseller',
    projectsCount: 4,
    shortDescription: 'Master modern server components, streaming architecture, TanStack Query, concurrency, and scalable frontend state machines.',
    detailedDescription: 'Go beyond boilerplate tutorials. Build high-concurrency, enterprise-ready web apps with Next.js 15 App Router, React 19 compiler patterns, optimistic UI updates, and production Docker containerization.',
    techStack: ['React 19', 'Next.js 15', 'TypeScript', 'Tailwind CSS', 'TanStack Query', 'Zustand'],
    learningOutcomes: [
      'Architect full-scale production apps using Next.js 15 App Router & Server Actions',
      'Implement concurrent rendering, streaming Suspense, and edge functions',
      'Master client-side state caching with TanStack Query and optimistic rollbacks',
      'Optimize Web Vitals (LCP, FID, CLS) to achieve consistent 95+ Lighthouse scores',
    ],
    prerequisites: 'Foundational knowledge of JavaScript (ES6+) and basic React component mechanics.',
    modules: [
      {
        number: '01',
        title: 'Core React 19 Internals & Rendering Lifecycles',
        duration: '2 Weeks',
        topics: ['Virtual DOM Reconciliation', 'Fiber Architecture', 'Custom Hook Optimization', 'Memory Leak Profiling'],
      },
      {
        number: '02',
        title: 'Next.js 15 App Router & Hybrid Server/Client Components',
        duration: '2 Weeks',
        topics: ['RSC Serialization Boundaries', 'Dynamic Route Segments', 'Streaming SSR with Suspense', 'Server Actions & Mutations'],
      },
      {
        number: '03',
        title: 'Enterprise State Management & Network Caching',
        duration: '2 Weeks',
        topics: ['Zustand Scalable Stores', 'TanStack Query Infinite Pagination', 'Optimistic UI Updates', 'Offline First Patterns'],
      },
      {
        number: '04',
        title: 'Production Capstone: Real-Time Collaborative Workspace',
        duration: '2 Weeks',
        topics: ['WebSockets & CRDTs', 'Dockerized Multi-Stage Builds', 'Automated CI/CD with GitHub Actions', 'Mentor Code Defense'],
      },
    ],
  },
  {
    id: 'genai-llm-agents',
    title: 'Generative AI, LangChain & Autonomous Production Agents',
    category: 'AI & Machine Learning',
    level: 'Advanced',
    lessonsCount: 54,
    durationWeeks: 10,
    enrolledStudents: '3,920+',
    rating: 4.98,
    reviewsCount: 980,
    instructor: {
      name: 'Dr. Devika Sen',
      company: 'Amazon AWS',
    },
    image: courseDatascienceImg,
    badge: 'Trending',
    projectsCount: 5,
    shortDescription: 'Build production RAG pipelines, fine-tune open-source models with LoRA, and deploy multi-agent autonomous frameworks.',
    detailedDescription: 'Bridge theoretical machine learning and cutting-edge GenAI engineering. Master prompt engineering, vector databases (Pinecone/Milvus), LangGraph multi-agent loops, and production LLMOps on AWS.',
    techStack: ['Python', 'LangChain', 'Llama 3', 'Pinecone', 'PyTorch', 'FastAPI'],
    learningOutcomes: [
      'Build end-to-end multi-tenant RAG systems with hybrid vector search and re-ranking',
      'Fine-tune open-weight models (Llama 3 / Mistral) using PEFT/LoRA techniques',
      'Construct stateful multi-agent autonomous loops with LangGraph and function calling',
      'Deploy production LLM inference endpoints with token streaming and latency guards',
    ],
    prerequisites: 'Basic Python programming and familiarity with linear algebra and REST APIs.',
    modules: [
      {
        number: '01',
        title: 'Foundations of Modern LLMs & Prompt Engineering',
        duration: '2 Weeks',
        topics: ['Transformer Architecture Overview', 'Tokenization & Attention', 'Advanced Few-Shot Prompting', 'System Prompt Security'],
      },
      {
        number: '02',
        title: 'Production Retrieval Augmented Generation (RAG)',
        duration: '3 Weeks',
        topics: ['Chunking Strategies', 'Dense & Sparse Embeddings', 'Vector Indexing (HNSW / IVFFlat)', 'Reranking with Cohere'],
      },
      {
        number: '03',
        title: 'Autonomous Agents & Multi-Agent Orchestration',
        duration: '3 Weeks',
        topics: ['Tool Use & Function Calling', 'LangGraph Stateful Cycles', 'Human-in-the-Loop Guards', 'Evaluation with Ragas'],
      },
      {
        number: '04',
        title: 'Model Fine-Tuning & High-Throughput Deployment',
        duration: '2 Weeks',
        topics: ['LoRA & QLoRA Quantization', 'FastAPI Streaming Endpoints', 'Token Cost & Latency Budgets', 'Live Capstone Project'],
      },
    ],
  },
  {
    id: 'figma-enterprise-design-systems',
    title: 'Enterprise Figma & High-Fidelity UI/UX Design Systems',
    category: 'UI/UX Design',
    level: 'All Levels',
    lessonsCount: 38,
    durationWeeks: 6,
    enrolledStudents: '3,240+',
    rating: 4.94,
    reviewsCount: 840,
    instructor: {
      name: 'Meera Nair',
      company: 'Adobe',
    },
    image: courseUiuxImg,
    badge: 'High Demand',
    projectsCount: 3,
    shortDescription: 'Master design tokens, variables, component properties, accessibility audits, and seamless developer handoff.',
    detailedDescription: 'Learn the exact methodologies used by top design teams to build cohesive, scalable design systems. From color palettes to typography scales, interactive micro-animations, and token sync with GitHub.',
    techStack: ['Figma', 'Design Tokens', 'Variables', 'WCAG 2.1 AAA', 'FigJam', 'Tokens Studio'],
    learningOutcomes: [
      'Create multi-brand tokenized design systems supporting dynamic dark/light themes',
      'Master advanced Figma variables, component slot properties, and interactive variants',
      'Ensure WCAG 2.1 AAA accessibility compliance across color contrast and touch targets',
      'Automate developer handoffs with design-token-to-code pipelines (Style Dictionary)',
    ],
    prerequisites: 'No prior design experience required. Familiarity with basic web applications is helpful.',
    modules: [
      {
        number: '01',
        title: 'Design Foundations & Typography Hierarchy',
        duration: '1.5 Weeks',
        topics: ['Modular Scale Grids', '8pt Spacing Systems', 'Harmonious Color Palettes', 'Visual Hierarchy Principles'],
      },
      {
        number: '02',
        title: 'Atomic Components & Figma Variables Architecture',
        duration: '1.5 Weeks',
        topics: ['Atoms to Organisms', 'Primitive vs Semantic Tokens', 'Auto Layout Mastery', 'Interactive Component States'],
      },
      {
        number: '03',
        title: 'Micro-Interactions, Usability Testing & Accessibility',
        duration: '1.5 Weeks',
        topics: ['Smart Animate Transitions', 'Screen Reader Navigation Audits', 'Usability Testing Playbooks', 'Accessibility Checklists'],
      },
      {
        number: '04',
        title: 'Comprehensive Portfolio Capstone & Dev Handoff',
        duration: '1.5 Weeks',
        topics: ['Design Specification Docs', 'Tokens-to-CSS Generation', 'Portfolio Presentation Case Study', 'Mentor Critique'],
      },
    ],
  },
  {
    id: 'cloud-devops-kubernetes',
    title: 'Cloud DevOps, Docker, Kubernetes & AWS Automation',
    category: 'Cloud & DevOps',
    level: 'Intermediate',
    lessonsCount: 44,
    durationWeeks: 8,
    enrolledStudents: '3,480+',
    rating: 4.92,
    reviewsCount: 910,
    instructor: {
      name: 'Rishi Kapoor',
      company: 'Atlassian',
    },
    image: courseFullstackImg,
    badge: 'Bestseller',
    projectsCount: 4,
    shortDescription: 'Containerize microservices, orchestrate high-availability clusters with K8s, and build automated CI/CD with Terraform.',
    detailedDescription: 'Step into the shoes of a Cloud Platform Engineer. Learn how high-traffic internet companies achieve five-nines uptime, zero-downtime rolling deployments, automated autoscaling, and secure secrets management.',
    techStack: ['Docker', 'Kubernetes', 'AWS', 'Terraform', 'Helm', 'Prometheus'],
    learningOutcomes: [
      'Write production-grade multi-stage Dockerfiles with minimal footprint and security scanning',
      'Deploy and scale resilient Kubernetes deployments, statefulsets, ingress, and services',
      'Automate cloud infrastructure as code (IaC) across AWS with reusable Terraform modules',
      'Set up full observability stacks using Prometheus, Grafana, and automated alerts',
    ],
    prerequisites: 'Familiarity with basic Linux terminal commands and basic networking concepts.',
    modules: [
      {
        number: '01',
        title: 'Linux Fundamentals & Docker Containerization',
        duration: '2 Weeks',
        topics: ['Linux CLI Deep Dive', 'Docker Engine & Layers', 'Multi-Stage Optimization', 'Docker Compose Microservices'],
      },
      {
        number: '02',
        title: 'Kubernetes Cluster Architecture & Orchestration',
        duration: '2 Weeks',
        topics: ['Pods, Deployments & ReplicaSets', 'Cluster Networking & Ingress Controllers', 'ConfigMaps & Secrets', 'Persistent Volumes'],
      },
      {
        number: '03',
        title: 'Infrastructure as Code (IaC) with Terraform & AWS',
        duration: '2 Weeks',
        topics: ['VPC, Subnets & Security Groups', 'Terraform State Management', 'EKS Provisioning', 'IAM Least Privilege Security'],
      },
      {
        number: '04',
        title: 'CI/CD Pipelines & Site Reliability Engineering (SRE)',
        duration: '2 Weeks',
        topics: ['GitHub Actions Workflows', 'Helm Charts Packaging', 'Prometheus & Grafana Monitoring', 'Chaos Testing Capstone'],
      },
    ],
  },
  {
    id: 'backend-microservices-kafka',
    title: 'High-Throughput Microservices & Event-Driven Architecture',
    category: 'Web & Full Stack',
    level: 'Advanced',
    lessonsCount: 42,
    durationWeeks: 8,
    enrolledStudents: '2,950+',
    rating: 4.94,
    reviewsCount: 760,
    instructor: {
      name: 'Vikram Singhania',
      company: 'Razorpay',
    },
    image: courseDatascienceImg,
    badge: 'High Demand',
    projectsCount: 4,
    shortDescription: 'Build event-driven distributed systems using Apache Kafka, Redis pub/sub, PostgreSQL sharding, and gRPC.',
    detailedDescription: 'Architect systems that scale to millions of concurrent users. Learn database transaction isolation levels, distributed locking, idempotent payment webhooks, and fault-tolerant event streams.',
    techStack: ['Node.js', 'Go', 'Apache Kafka', 'PostgreSQL', 'Redis', 'gRPC'],
    learningOutcomes: [
      'Design event-driven systems using Apache Kafka topics, consumer groups, and partitions',
      'Optimize database queries, B-tree indexes, connection pools, and read replicas in Postgres',
      'Build lightning-fast inter-service communication with gRPC and Protocol Buffers',
      'Implement distributed rate limiters, circuit breakers, and idempotent retry queues',
    ],
    prerequisites: 'Proficiency in backend JavaScript/TypeScript or Go, and relational database basics.',
    modules: [
      {
        number: '01',
        title: 'Distributed Systems Patterns & Concurrency',
        duration: '2 Weeks',
        topics: ['CAP Theorem Realities', 'ACID vs BASE Models', 'Connection Pooling', 'Deadlock Diagnostics'],
      },
      {
        number: '02',
        title: 'Event Streaming with Apache Kafka',
        duration: '2 Weeks',
        topics: ['Kafka Cluster Anatomy', 'Message Retention & Partitions', 'Exactly-Once Processing', 'Schema Registry'],
      },
      {
        number: '03',
        title: 'Caching, Sharding & High-Speed Data Stores',
        duration: '2 Weeks',
        topics: ['Redis Cluster Caching Strategies', 'Distributed Locks with Redlock', 'PostgreSQL Partitioning', 'Read-Write Splitting'],
      },
      {
        number: '04',
        title: 'Fintech Capstone: Real-Time Payment Settlement Engine',
        duration: '2 Weeks',
        topics: ['Idempotency Keys', 'Webhook Delivery Pipeline', 'Stress Testing with k6', 'Architectural Defense'],
      },
    ],
  },
  {
    id: 'data-analytics-bi-sql',
    title: 'Data Analytics, BigQuery SQL & Power BI Dashboards',
    category: 'Data & Analytics',
    level: 'Beginner',
    lessonsCount: 36,
    durationWeeks: 6,
    enrolledStudents: '4,120+',
    rating: 4.90,
    reviewsCount: 1100,
    instructor: {
      name: 'Rahul Verma',
      company: 'Amazon',
    },
    image: courseMarketingImg,
    badge: 'Bestseller',
    projectsCount: 3,
    shortDescription: 'From complex SQL window functions to Google BigQuery, ETL pipelines, and executive dashboards.',
    detailedDescription: 'Turn messy, high-volume enterprise data into actionable business intelligence. Master window functions, CTEs, automated data cleaning with Python Pandas, and interactive executive reporting.',
    techStack: ['Advanced SQL', 'Google BigQuery', 'Python Pandas', 'Power BI', 'Tableau'],
    learningOutcomes: [
      'Write high-performance SQL queries with window functions, CTEs, and self-joins',
      'Analyze multi-gigabyte datasets inside Google BigQuery with partitioned tables',
      'Build automated ETL pipelines using Python Pandas and scheduled cron jobs',
      'Design interactive C-suite executive dashboards in Power BI with drill-down KPIs',
    ],
    prerequisites: 'No prior programming experience required. Basic familiarity with spreadsheet software is helpful.',
    modules: [
      {
        number: '01',
        title: 'Advanced SQL Querying & Relational Databases',
        duration: '1.5 Weeks',
        topics: ['Multi-Table Joins', 'Aggregations & Grouping', 'Window Functions (RANK, LAG, LEAD)', 'Subqueries & CTEs'],
      },
      {
        number: '02',
        title: 'Cloud Data Warehousing with Google BigQuery',
        duration: '1.5 Weeks',
        topics: ['Data Lake vs Data Warehouse', 'Partitioned & Clustered Tables', 'Cost Optimization', 'Public Datasets Mining'],
      },
      {
        number: '03',
        title: 'Python for Exploratory Data Analysis (EDA)',
        duration: '1.5 Weeks',
        topics: ['Pandas Data Wrangling', 'Missing Value Imputation', 'Statistical Outlier Detection', 'Seaborn Visualizations'],
      },
      {
        number: '04',
        title: 'Business Intelligence Dashboard Capstone',
        duration: '1.5 Weeks',
        topics: ['DAX Expressions in Power BI', 'Executive KPI Cards', 'Automated Refresh Schedules', 'Stakeholder Presentation'],
      },
    ],
  },
  {
    id: 'growth-marketing-attribution',
    title: 'Omnichannel Performance Marketing & Attribution Modeling',
    category: 'Marketing',
    level: 'Beginner',
    lessonsCount: 32,
    durationWeeks: 5,
    enrolledStudents: '2,640+',
    rating: 4.88,
    reviewsCount: 650,
    instructor: {
      name: 'Tanvi Joshi',
      company: 'Stripe',
    },
    image: courseMarketingImg,
    badge: 'New',
    projectsCount: 3,
    shortDescription: 'Master full-funnel customer acquisition, Meta & Google Ads optimization, GA4, and marketing attribution modeling.',
    detailedDescription: 'Stop wasting ad budgets. Learn rigorous data-backed growth frameworks, conversion rate optimization (CRO), custom audience lookalikes, multi-touch attribution, and retention loops.',
    techStack: ['Google Ads', 'Meta Ads Manager', 'Google Analytics 4', 'Looker Studio', 'AppsFlyer'],
    learningOutcomes: [
      'Run profitable performance marketing campaigns on Google Search, YouTube, and Meta',
      'Configure GA4 custom event tracking, conversion funnels, and retention cohorts',
      'Implement multi-touch attribution models to allocate marketing spend effectively',
      'Conduct rigorous A/B split tests with statistical significance calculations',
    ],
    prerequisites: 'No prior technical experience required. Interest in growth and data-driven marketing.',
    modules: [
      {
        number: '01',
        title: 'Paid Acquisition Engines (Meta & Google Ads)',
        duration: '1.5 Weeks',
        topics: ['Auction Mechanics', 'Audience Segmentation', 'Creative Fatigue Management', 'Target CPA & ROAS Bidding'],
      },
      {
        number: '02',
        title: 'Conversion Rate Optimization (CRO) & Funnels',
        duration: '1 Week',
        topics: ['Landing Page Teardowns', 'Heatmaps & User Recording Analysis', 'A/B Test Design', 'Friction Point Removal'],
      },
      {
        number: '03',
        title: 'GA4 Tracking & Marketing Attribution',
        duration: '1.5 Weeks',
        topics: ['Tag Manager Deployment', 'Custom Dimension Configuration', 'First-Touch vs Data-Driven Attribution', 'Looker Dashboards'],
      },
      {
        number: '04',
        title: 'Full-Funnel Capstone: $50,000 Campaign Simulation',
        duration: '1 Week',
        topics: ['Budget Allocation Matrix', 'Unit Economics & LTV/CAC Ratio', 'Executive Report Defense', 'Mentor Feedback'],
      },
    ],
  },
  {
    id: 'cyber-security-secure-coding',
    title: 'Applied Cyber Security & Secure Software Architecture',
    category: 'Cyber Security',
    level: 'Intermediate',
    lessonsCount: 38,
    durationWeeks: 7,
    enrolledStudents: '2,150+',
    rating: 4.93,
    reviewsCount: 520,
    instructor: {
      name: 'Kunal Roy',
      company: 'AWS',
    },
    image: courseFullstackImg,
    badge: 'Trending',
    projectsCount: 4,
    shortDescription: 'Master the OWASP Top 10 vulnerabilities, penetration testing, OAuth 2.0 / OpenID Connect, and secure code audits.',
    detailedDescription: 'Build software that stands up against sophisticated cyber attacks. Hands-on labs simulating SQL injection, cross-site scripting (XSS), CSRF, cryptographic misconfigurations, and supply-chain vulnerabilities.',
    techStack: ['OWASP Top 10', 'Burp Suite', 'OAuth 2.0', 'JWT Security', 'SonarQube', 'Linux'],
    learningOutcomes: [
      'Identify, exploit, and remediate all OWASP Top 10 vulnerabilities in real web apps',
      'Implement enterprise authentication flows with OAuth 2.0, PKCE, and signed JWT tokens',
      'Perform automated static and dynamic application security testing (SAST/DAST)',
      'Design zero-trust backend architectures with end-to-end encryption and audit logging',
    ],
    prerequisites: 'Familiarity with web application development (HTTP, cookies, databases).',
    modules: [
      {
        number: '01',
        title: 'Web Vulnerabilities & The OWASP Top 10',
        duration: '2 Weeks',
        topics: ['SQLi & NoSQL Injections', 'Stored & Reflected XSS', 'CSRF Tokens & SameSite Cookies', 'SSRF Exploitation'],
      },
      {
        number: '02',
        title: 'Modern Identity, Authentication & Cryptography',
        duration: '1.5 Weeks',
        topics: ['OAuth 2.0 & OpenID Connect', 'JWT Signature Verification', 'Bcrypt & Argon2 Hashing', 'TLS Handshakes & HTTPS'],
      },
      {
        number: '03',
        title: 'Security Auditing, Penetration Testing & Burp Suite',
        duration: '2 Weeks',
        topics: ['Burp Suite Proxy Interception', 'API Vulnerability Testing', 'Automated SAST with SonarQube', 'Dependency Vulnerabilities'],
      },
      {
        number: '04',
        title: 'Defensive Architecture Capstone: Vulnerable App Hardening',
        duration: '1.5 Weeks',
        topics: ['Codebase Security Audit', 'Patching Exploits', 'Zero-Trust Policy Deployment', 'Security Defense Report'],
      },
    ],
  },
]
