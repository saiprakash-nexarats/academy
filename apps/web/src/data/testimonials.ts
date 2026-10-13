import type { TestimonialItem } from '@/types'
import studentPriyaImg from '@/assets/student-priya.jpg'
import studentRahulImg from '@/assets/student-rahul.jpg'
import studentSnehaImg from '@/assets/student-sneha.jpg'
import studentAnanyaImg from '@/assets/student-ananya.jpg'

export const TESTIMONIALS_DATA: TestimonialItem[] = [
  {
    id: 'priya',
    name: 'Priya Sharma',
    role: 'Software Engineer',
    company: 'Microsoft',
    companyBrand: 'microsoft',
    quote:
      'Nexarats Academy gave me the skills and confidence to switch to a tech career. The mentors are very supportive!',
    avatar: studentPriyaImg,
    rating: 5,
  },
  {
    id: 'rahul',
    name: 'Rahul Verma',
    role: 'Data Analyst',
    company: 'Amazon',
    companyBrand: 'amazon',
    quote:
      'The hands-on projects and live classes made learning so engaging. I got placed within 3 months!',
    avatar: studentRahulImg,
    rating: 5,
  },
  {
    id: 'sneha',
    name: 'Sneha Kapoor',
    role: 'Product Designer',
    company: 'Adobe',
    companyBrand: 'adobe',
    quote:
      'Great learning experience! The career guidance and mentor support are truly helpful.',
    avatar: studentSnehaImg,
    rating: 5,
  },
  {
    id: 'ananya',
    name: 'Ananya Deshmukh',
    role: 'Growth Specialist',
    company: 'Google Partner',
    companyBrand: 'google',
    quote:
      'The live budget simulations and campaign execution projects gave me practical confidence that no textbook ever could.',
    avatar: studentAnanyaImg,
    rating: 5,
  },
  {
    id: 'karthik',
    name: 'Karthik Sundaram',
    role: 'Backend Engineer',
    company: 'Razorpay',
    companyBrand: 'razorpay',
    quote:
      'The deep dives into distributed architectures and database concurrency gave me an edge during fintech interview rounds.',
    avatar: '',
    rating: 5,
  },
  {
    id: 'divya',
    name: 'Divya Nair',
    role: 'ML Engineer',
    company: 'Swiggy',
    companyBrand: 'swiggy',
    quote:
      'Transitioned into core AI within 5 months. The mentor guidance on deploying Transformer models on AWS was pure gold.',
    avatar: studentAnanyaImg,
    rating: 5,
  },
  {
    id: 'aravind',
    name: 'Aravind Krishnan',
    role: 'Design Systems Lead',
    company: 'Uber',
    companyBrand: 'uber',
    quote:
      'The emphasis on design tokens, accessibility, and collaboration with developers set me apart from thousands of applicants.',
    avatar: studentRahulImg,
    rating: 5,
  },
  {
    id: 'rohan',
    name: 'Rohan Mehta',
    role: 'Full Stack Engineer',
    company: 'Atlassian',
    companyBrand: 'atlassian',
    quote:
      'The sprint-based curriculum feels like working inside a real tech team from day one. Landed my dream offer!',
    avatar: studentPriyaImg,
    rating: 5,
  },
]

export interface CaseStudyFull {
  startingPackage: string
  placedPackage: string
  salaryMultiplier: string
  backgroundStory: string
  learningTimeline: {
    phase: string
    title: string
    duration: string
    details: string
  }[]
  capstoneDetails: {
    title: string
    overview: string
    techStack: string[]
    challengesSolved: string[]
    architectureHighlights: string[]
  }
  interviewRounds: {
    round: string
    title: string
    focus: string
    keyQuestion: string
  }[]
  mentorGuidanceImpact: string
  studentAdvice: string
}

export interface SuccessStoryExtended extends TestimonialItem {
  previousRole: string
  salaryHike: string
  programCompleted: string
  category: 'Software Engineering' | 'Artificial Intelligence' | 'Design' | 'Marketing'
  cohortYear: string
  storyBadge: 'Career Switcher' | 'College Graduate' | 'Fast Track Placed' | 'Domain Pivot'
  capstoneProject: string
  credentialId: string
  interviewHighlight: string
  featured?: boolean
  caseStudy: CaseStudyFull
}

export const ALL_SUCCESS_STORIES: SuccessStoryExtended[] = [
  {
    id: 'priya',
    name: 'Priya Sharma',
    role: 'Software Engineer II',
    company: 'Microsoft',
    companyBrand: 'microsoft',
    quote:
      'Coming from a non-CS background, Nexarats structured cohorts and direct mentor reviews bridged every conceptual gap. Within 4 months of graduation, I landed my dream offer at Microsoft!',
    avatar: studentPriyaImg,
    rating: 5,
    previousRole: 'QA Manual Tester',
    salaryHike: '+140% Hike',
    programCompleted: 'Full Stack Development',
    category: 'Software Engineering',
    cohortYear: 'Cohort 2025',
    storyBadge: 'Career Switcher',
    capstoneProject: 'Multi-tenant SaaS Cloud Platform with Redis Caching',
    credentialId: 'NX-89241',
    interviewHighlight: 'Cleared 4 rounds of System Design & Coding assessments effortlessly.',
    featured: true,
    caseStudy: {
      startingPackage: '₹4.8 LPA',
      placedPackage: '₹14.5 LPA',
      salaryMultiplier: '3.0x Package Increase',
      backgroundStory:
        'Priya worked as a manual quality assurance tester for 2.5 years. Despite having a strong passion for coding, she lacked formal computer science credentials and experienced imposter syndrome when trying to apply for core software engineering roles. She joined Nexarats Academy to build a rock-solid, verifiable software engineering portfolio.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'Foundations & Data Structures',
          duration: 'Months 1–2',
          details: 'Mastered modern ES6+, TypeScript typings, asymptotic time complexities, and foundational algorithm problem solving with weekly 1:1 live problem walkthroughs.',
        },
        {
          phase: 'Phase 2',
          title: 'Full Stack Production Architecture',
          duration: 'Months 3–4',
          details: 'Built full stack services with Next.js App Router, Express/Node.js, PostgreSQL schema migrations, and Redis caching layers under mentor Arjun Mehta.',
        },
        {
          phase: 'Phase 3',
          title: 'System Design & Mock Crucible',
          duration: 'Month 5',
          details: 'Underwent 6 high-pressure mock interviews evaluating distributed systems design, CAP theorem trade-offs, and behavioral STAR-method storytelling.',
        },
        {
          phase: 'Phase 4',
          title: 'Admissions Referral & Placement',
          duration: 'Month 6',
          details: 'Received direct internal referrals to Microsoft Azure teams, completed 4 technical interview rounds, and accepted an offer with a 140% salary hike.',
        },
      ],
      capstoneDetails: {
        title: 'Multi-Tenant SaaS Cloud Monitoring & Incident Platform',
        overview: 'A scalable distributed monitoring platform featuring real-time server health metrics, automated alert thresholds, and multi-tenant organization workspaces.',
        techStack: ['Next.js 15', 'TypeScript', 'Node.js', 'PostgreSQL', 'Redis', 'Docker', 'AWS ECS'],
        challengesSolved: [
          'Eliminated redundant database queries by implementing write-through Redis caching with automated TTL invalidation.',
          'Maintained sub-120ms response times while handling 1,500 simultaneous WebSocket telemetry event streams.',
          'Built strict tenant data isolation using PostgreSQL row-level security (RLS) policies.',
        ],
        architectureHighlights: [
          'Event-driven asynchronous background job workers with BullMQ',
          'JWT authentication with rotating refresh tokens and OAuth 2.0 Google login',
          'Automated CI/CD pipeline running Jest unit tests and Docker image builds',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'DSA & Algorithmic Problem Solving (60 Mins)',
          focus: 'Tree Traversals & Dynamic Programming',
          keyQuestion: 'Serialize and deserialize a binary search tree, followed by finding the maximum path sum across branches.',
        },
        {
          round: 'Round 2',
          title: 'Frontend Architecture & React Internals (60 Mins)',
          focus: 'React 19 Rendering & Network Caching',
          keyQuestion: 'Design an infinite scrolling feed that prevents memory leaks and implements optimistic updates with error rollback.',
        },
        {
          round: 'Round 3',
          title: 'High-Level System Design (60 Mins)',
          focus: 'Distributed Telemetry Ingestion Service',
          keyQuestion: 'Design a metric collection pipeline capable of handling 50,000 requests per second with high availability.',
        },
        {
          round: 'Round 4',
          title: 'Director Behavioral & Engineering Culture (45 Mins)',
          focus: 'Conflict Resolution & Technical Decision Making',
          keyQuestion: 'Describe a situation where you challenged a technical decision and how you used data to justify your alternative approach.',
        },
      ],
      mentorGuidanceImpact:
        'My mentor, Arjun Mehta from Google, personally reviewed every line of my capstone pull requests. In our mock interviews, he pushed me on cache stampedes and database deadlocks—that exact topic came up during my Round 3 Microsoft system design interview!',
      studentAdvice:
        'Do not wait until you feel "ready" to start building. Build production projects from week one, understand why you chose every library, and treat mock interviews with the utmost seriousness.',
    },
  },
  {
    id: 'rahul',
    name: 'Rahul Verma',
    role: 'Data Analyst & BI Lead',
    company: 'Amazon',
    companyBrand: 'amazon',
    quote:
      'The hands-on projects using real enterprise datasets gave me the exact portfolio evidence recruiters wanted. The mock interviews were directly aligned with Big Tech hiring bars.',
    avatar: studentRahulImg,
    rating: 5,
    previousRole: 'Operations Executive',
    salaryHike: '+115% Hike',
    programCompleted: 'Data Science & AI',
    category: 'Artificial Intelligence',
    cohortYear: 'Cohort 2025',
    storyBadge: 'Fast Track Placed',
    capstoneProject: 'Customer Churn Prediction Model & Real-time BI Pipeline',
    credentialId: 'NX-91024',
    interviewHighlight: 'Stood out due to production-grade SQL and ML dashboard deployments.',
    featured: true,
    caseStudy: {
      startingPackage: '₹5.2 LPA',
      placedPackage: '₹12.0 LPA',
      salaryMultiplier: '2.3x Package Increase',
      backgroundStory:
        'Rahul spent 3 years managing logistics spreadsheets in an operations role. He knew there was immense leverage in data analytics, but lacked the structured SQL, Python, and statistical modeling training needed to qualify for tech-first business intelligence teams.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'Advanced SQL & Data Warehousing',
          duration: 'Months 1–2',
          details: 'Mastered multi-table complex joins, window functions (LEAD, LAG, DENSE_RANK), and query optimization in BigQuery.',
        },
        {
          phase: 'Phase 2',
          title: 'Python for Exploratory Data Analysis & ML',
          duration: 'Months 3–4',
          details: 'Built statistical hypothesis tests, regression models, and gradient boosting classifiers using Scikit-Learn and Pandas.',
        },
        {
          phase: 'Phase 3',
          title: 'Executive Dashboards & Business Impact',
          duration: 'Month 5',
          details: 'Created interactive Power BI and Tableau storyboards translating technical model findings into actionable executive decisions.',
        },
        {
          phase: 'Phase 4',
          title: 'Placement & Amazon Bar Raiser',
          duration: 'Month 6',
          details: 'Cleared Amazon Business Intelligence interviews with high praise on customer obsession and metrics rigor.',
        },
      ],
      capstoneDetails: {
        title: 'Predictive E-Commerce Churn Intelligence Engine',
        overview: 'An end-to-end analytics and machine learning pipeline that identifies high-value customers at risk of churn with 89% accuracy.',
        techStack: ['Python', 'PostgreSQL', 'Google BigQuery', 'Scikit-Learn', 'Power BI', 'FastAPI'],
        challengesSolved: [
          'Handled severe dataset class imbalance (only 4% churn rate) using SMOTE oversampling and PR-AUC metric tuning.',
          'Engineered 34 behavioral features including rolling purchase frequency and customer support ticket sentiment.',
        ],
        architectureHighlights: [
          'Automated daily data ingestion pipeline with scheduled cron triggers',
          'Executive KPI dashboard with interactive cohort retention heatmaps',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Live SQL Querying Test (60 Mins)',
          focus: 'Complex Aggregations & Window Functions',
          keyQuestion: 'Calculate 7-day rolling active customer counts and identify drop-off cohorts from raw clickstream logs.',
        },
        {
          round: 'Round 2',
          title: 'Statistical Modeling & Case Study (60 Mins)',
          focus: 'A/B Testing & Metric Definition',
          keyQuestion: 'How would you measure the success of Amazon Prime 1-day delivery expansion, accounting for seasonal cannibalization?',
        },
        {
          round: 'Round 3',
          title: 'Amazon Leadership Principles & Bar Raiser (60 Mins)',
          focus: 'Customer Obsession & Bias for Action',
          keyQuestion: 'Tell me about a time you made a critical business recommendation using incomplete or conflicting data.',
        },
      ],
      mentorGuidanceImpact:
        'Dr. Devika Sen taught me to never present raw code to executives without framing the monetary business impact. That mindset completely changed how I handled the Amazon case study round.',
      studentAdvice:
        'SQL is the bedrock of analytics. Don’t just know how to write a query—know how much computing cost and memory it uses on big cloud datasets.',
    },
  },
  {
    id: 'sneha',
    name: 'Sneha Kapoor',
    role: 'Staff Product Designer',
    company: 'Adobe',
    companyBrand: 'adobe',
    quote:
      'The design critiques and 1-on-1 portfolio teardowns by senior leaders transformed my work from junior mockups to production-ready design systems.',
    avatar: studentSnehaImg,
    rating: 5,
    previousRole: 'Architectural Drafter',
    salaryHike: '+160% Hike',
    programCompleted: 'UI/UX Design',
    category: 'Design',
    cohortYear: 'Cohort 2024',
    storyBadge: 'Domain Pivot',
    capstoneProject: 'Accessible Design System & FinTech Micro-interactions Prototype',
    credentialId: 'NX-78432',
    interviewHighlight: 'Hired after presenting her interactive Figma component architecture.',
    featured: true,
    caseStudy: {
      startingPackage: '₹4.2 LPA',
      placedPackage: '₹13.5 LPA',
      salaryMultiplier: '3.2x Package Increase',
      backgroundStory:
        'Sneha spent 2 years drafting CAD architectural floor plans. While she possessed an innate spatial and aesthetic sensibility, she had zero experience with digital product wireframing, WCAG accessibility, or component tokens. Nexarats helped her redirect her design instincts into tech.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'UX Research & Information Architecture',
          duration: 'Months 1–2',
          details: 'Conducted user interviews, mapped complex customer journey funnels, and drafted modular wireframes.',
        },
        {
          phase: 'Phase 2',
          title: 'Advanced Figma & Token Architectures',
          duration: 'Months 3–4',
          details: 'Built multi-tier design systems leveraging primitive and semantic Figma variables, auto-layout, and interactive component variants.',
        },
        {
          phase: 'Phase 3',
          title: 'Prototyping & Usability Audits',
          duration: 'Month 5',
          details: 'Conducted moderated usability tests on prototypes with micro-interactions, refining touch targets and contrast for WCAG 2.1 AAA compliance.',
        },
        {
          phase: 'Phase 4',
          title: 'Portfolio Presentation & Hiring',
          duration: 'Month 6',
          details: 'Presented her capstone design system to Adobe design directors and received a Staff Product Designer offer.',
        },
      ],
      capstoneDetails: {
        title: 'Aura: Multi-Brand Accessible Design System for FinTech',
        overview: 'A complete enterprise design system built for complex financial trading interfaces, featuring light/dark mode variables and 85+ production-ready interactive components.',
        techStack: ['Figma', 'Tokens Studio', 'WCAG 2.1 AAA', 'Zeroheight', 'FigJam'],
        challengesSolved: [
          'Created automated token syncing between Figma variables and GitHub repositories using Style Dictionary.',
          'Audited color contrast ratios for high-density financial data charts to ensure 100% color-blind friendliness.',
        ],
        architectureHighlights: [
          'Comprehensive design token specification docs for engineers',
          'Micro-interaction prototypes with Smart Animate and keyboard accessibility',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Portfolio Walkthrough & Deep Dive (60 Mins)',
          focus: 'Design Thinking & Decision Rationale',
          keyQuestion: 'Walk us through your toughest design compromise and explain how user feedback guided the iteration.',
        },
        {
          round: 'Round 2',
          title: 'Live Whiteboard Design Challenge (60 Mins)',
          focus: 'Problem Framing & Wireframing on the Fly',
          keyQuestion: 'Design an onboarding experience for an enterprise creative tool targeted at first-time non-technical creators.',
        },
        {
          round: 'Round 3',
          title: 'Cross-Functional Collaboration with Engineering (45 Mins)',
          focus: 'Design Handoff & Edge Cases',
          keyQuestion: 'How do you handle disagreements when engineers push back on custom UI animations citing technical debt?',
        },
      ],
      mentorGuidanceImpact:
        'Meera Nair from Adobe grilled me on my portfolio storytelling. She taught me to stop showing pretty pictures and instead demonstrate the business metrics and user problems my designs solved.',
      studentAdvice:
        'A design portfolio should not look like Dribbble eye candy. Top tech companies want to see your messy wireframes, user research evidence, and the reasons why you rejected certain ideas.',
    },
  },
  {
    id: 'ananya',
    name: 'Ananya Deshmukh',
    role: 'Growth Marketing Specialist',
    company: 'Google Partner Agency',
    companyBrand: 'microsoft',
    quote:
      'The live budget simulations and campaign execution projects gave me practical confidence that no theoretical textbook ever could. Truly transformed my career trajectory.',
    avatar: studentAnanyaImg,
    rating: 5,
    previousRole: 'Junior Content Writer',
    salaryHike: '+125% Hike',
    programCompleted: 'Digital Marketing',
    category: 'Marketing',
    cohortYear: 'Cohort 2025',
    storyBadge: 'Fast Track Placed',
    capstoneProject: 'Multi-Channel Omnichannel Funnel & Attribution Modeling',
    credentialId: 'NX-94318',
    interviewHighlight: 'Generated 4.8x simulated ROI on capstone media spend test.',
    caseStudy: {
      startingPackage: '₹3.8 LPA',
      placedPackage: '₹8.6 LPA',
      salaryMultiplier: '2.25x Package Increase',
      backgroundStory:
        'Ananya started as a freelance content writer producing blog posts for low fees. She wanted to transition into data-driven performance marketing where decisions are tied directly to revenue attribution and large ad spend budgets.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'Search & Paid Media Foundations',
          duration: 'Month 1',
          details: 'Mastered Google Search bidding strategies, keyword match types, and Meta Ads auction mechanics.',
        },
        {
          phase: 'Phase 2',
          title: 'Analytics & GA4 Tracking Infrastructure',
          duration: 'Month 2',
          details: 'Configured Google Tag Manager, custom event tracking, and Looker Studio live performance dashboards.',
        },
        {
          phase: 'Phase 3',
          title: 'Conversion Rate Optimization (CRO)',
          duration: 'Month 3',
          details: 'Conducted A/B tests on landing pages, heat map click analysis, and friction removal in checkout funnels.',
        },
        {
          phase: 'Phase 4',
          title: 'Hiring & Client Portfolio Defense',
          duration: 'Month 4',
          details: 'Presented her simulated $50k spend attribution model to hiring managers and secured an agency lead role.',
        },
      ],
      capstoneDetails: {
        title: 'Full-Funnel SaaS Omnichannel Growth & Attribution Framework',
        overview: 'A simulated full-funnel customer acquisition campaign across Google, Meta, and LinkedIn targeting B2B tech decision makers.',
        techStack: ['Google Ads', 'Meta Ads Manager', 'GA4', 'Looker Studio', 'Hotjar'],
        challengesSolved: [
          'Reduced simulated Customer Acquisition Cost (CAC) by 38% through audience exclusions and high-intent long-tail keywords.',
          'Built multi-touch data-driven attribution reports reconciling discrepancies between Meta and GA4.',
        ],
        architectureHighlights: [
          'High-converting landing page prototype built with CRO best practices',
          'Automated weekly performance reporting in Looker Studio',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Paid Media Strategy & Auction Mechanics (45 Mins)',
          focus: 'Bidding Strategies & Campaign Structures',
          keyQuestion: 'When would you choose Target ROAS over Maximize Conversions, and how do you handle creative fatigue?',
        },
        {
          round: 'Round 2',
          title: 'Live Analytical Case Study (60 Mins)',
          focus: 'Attribution & Budget Reallocation',
          keyQuestion: 'Given a dataset of multi-touch user journeys, where should a client invest an additional $20,000 monthly budget?',
        },
      ],
      mentorGuidanceImpact:
        'Tanvi Joshi from Stripe taught me the mathematical foundations of unit economics: LTV, CAC, and payback periods. Speaking the language of finance made me stand out from every other marketing applicant.',
      studentAdvice:
        'Marketing today is 80% analytics and psychology. Learn Google Analytics 4 inside and out, and always tie your experiments to bottom-line revenue.',
    },
  },
  {
    id: 'karthik',
    name: 'Karthik Sundaram',
    role: 'Backend Systems Engineer',
    company: 'Razorpay',
    companyBrand: 'amazon',
    quote:
      'The deep dives into distributed architectures, database indexes, and concurrency gave me an edge during system design rounds. Nexarats mentors are world class.',
    avatar: '',
    rating: 5,
    previousRole: 'Technical Support Associate',
    salaryHike: '+180% Hike',
    programCompleted: 'Full Stack Development',
    category: 'Software Engineering',
    cohortYear: 'Cohort 2024',
    storyBadge: 'Career Switcher',
    capstoneProject: 'Distributed Event Broker with Kafka & WebSockets',
    credentialId: 'NX-67129',
    interviewHighlight: 'Aced high-concurrency architecture and database optimization rounds.',
    caseStudy: {
      startingPackage: '₹4.0 LPA',
      placedPackage: '₹14.0 LPA',
      salaryMultiplier: '3.5x Package Increase',
      backgroundStory:
        'Karthik spent 2 years responding to customer support tickets for an IT services firm. He taught himself basic JavaScript at night but struggled to bridge the gap into distributed architectures and database tuning.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'Backend Fundamentals & Relational Databases',
          duration: 'Months 1–2',
          details: 'Mastered Node.js asynchronous event loops, PostgreSQL indexing, and transaction isolation levels.',
        },
        {
          phase: 'Phase 2',
          title: 'Microservices & Message Streaming',
          duration: 'Months 3–4',
          details: 'Built event-driven microservices using Apache Kafka, Redis distributed locks, and Docker.',
        },
        {
          phase: 'Phase 3',
          title: 'System Design & Load Testing',
          duration: 'Month 5',
          details: 'Stress-tested backend services using k6 to discover bottlenecks and tune connection pools.',
        },
        {
          phase: 'Phase 4',
          title: 'Fintech Hiring & Offer Acceptance',
          duration: 'Month 6',
          details: 'Cleared 4 rounds of technical evaluations at Razorpay, securing a 180% salary increase.',
        },
      ],
      capstoneDetails: {
        title: 'High-Concurrency Event-Driven Message Broker',
        overview: 'A distributed event streaming engine capable of ingesting 25,000 messages per second with guaranteed delivery semantics.',
        techStack: ['Node.js', 'Go', 'Apache Kafka', 'PostgreSQL', 'Redis', 'Docker'],
        challengesSolved: [
          'Prevented double-spending in simulated transactions using Redis distributed mutex locks (Redlock).',
          'Optimized PostgreSQL B-Tree indexes, decreasing query latency by 82% under peak simulated loads.',
        ],
        architectureHighlights: [
          'Dead letter queues for failed message reprocessing',
          'Prometheus metrics endpoint monitoring consumer lag and p99 latency',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Data Structures & Concurrency (60 Mins)',
          focus: 'Locks, Threads & Queues',
          keyQuestion: 'Implement an in-memory priority queue with thread-safe reads and concurrent writes.',
        },
        {
          round: 'Round 2',
          title: 'Low-Level Design & Database Optimization (60 Mins)',
          focus: 'Schema Design & Transactions',
          keyQuestion: 'Design the schema and locking strategy for an e-commerce flash sale inventory reservation system.',
        },
        {
          round: 'Round 3',
          title: 'High-Level Distributed System Design (60 Mins)',
          focus: 'Payment Webhook Processing Engine',
          keyQuestion: 'Architect an idempotent webhook delivery system with exponential backoff and persistent retries.',
        },
      ],
      mentorGuidanceImpact:
        'Vikram Singhania from Razorpay shared real-world war stories of production outages. Preparing for those exact edge cases gave me supreme confidence during the technical interview.',
      studentAdvice:
        'Don’t just learn how to make an API work on localhost. Learn what happens when 10,000 people hit that API at the exact same second.',
    },
  },
  {
    id: 'divya',
    name: 'Divya Nair',
    role: 'ML Engineer',
    company: 'Swiggy',
    companyBrand: 'amazon',
    quote:
      'Transitioned into core AI within 5 months. The mentor guidance on deploying Transformer models and fine-tuning LLMs on AWS was pure gold.',
    avatar: studentAnanyaImg,
    rating: 5,
    previousRole: 'College Graduate (ECE)',
    salaryHike: '+135% Hike',
    programCompleted: 'Data Science & AI',
    category: 'Artificial Intelligence',
    cohortYear: 'Cohort 2025',
    storyBadge: 'College Graduate',
    capstoneProject: 'Hyperlocal Demand Forecasting Engine using Neural Networks',
    credentialId: 'NX-83912',
    interviewHighlight: 'Cracked technical coding and algorithm defense with top cohort rank.',
    caseStudy: {
      startingPackage: '₹4.5 LPA',
      placedPackage: '₹12.5 LPA',
      salaryMultiplier: '2.8x Package Increase',
      backgroundStory:
        'Fresh out of an electronics engineering college program, Divya found university textbooks outdated compared to modern PyTorch and LLMOps practices. She enrolled at Nexarats to break into top-tier food-tech AI teams.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'Deep Learning & Neural Network Foundations',
          duration: 'Months 1–2',
          details: 'Mastered backpropagation mathematics, PyTorch tensors, and loss landscape optimization.',
        },
        {
          phase: 'Phase 2',
          title: 'Time Series & Spatial Modeling',
          duration: 'Months 3–4',
          details: 'Trained LSTM and Transformer networks on spatio-temporal geospatial datasets.',
        },
        {
          phase: 'Phase 3',
          title: 'Model Deployment & Latency Optimization',
          duration: 'Month 5',
          details: 'Serialized models using ONNX and served real-time predictions via FastAPI Docker containers.',
        },
        {
          phase: 'Phase 4',
          title: 'Swiggy AI Team Interview & Selection',
          duration: 'Month 6',
          details: 'Aced machine learning algorithm rounds and received an ML Engineer offer.',
        },
      ],
      capstoneDetails: {
        title: 'Hyperlocal Spatio-Temporal Order Volume Predictor',
        overview: 'A deep learning forecasting system that predicts delivery demand surges 45 minutes in advance for hyperlocal zones.',
        techStack: ['Python', 'PyTorch', 'FastAPI', 'Docker', 'AWS S3', 'PostGIS'],
        challengesSolved: [
          'Engineered weather, festival, and traffic lag features that boosted prediction accuracy by 19%.',
          'Reduced model inference latency from 320ms to 24ms using TensorRT acceleration.',
        ],
        architectureHighlights: [
          'Real-time streaming inference server with Redis pub/sub',
          'Automated drift monitoring using Evidently AI',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Machine Learning Math & Coding (60 Mins)',
          focus: 'Gradient Descent & Tensor Operations',
          keyQuestion: 'Derive the backpropagation gradient for layer normalization and implement it in pure NumPy.',
        },
        {
          round: 'Round 2',
          title: 'Applied ML Architecture Defense (60 Mins)',
          focus: 'Delivery Driver Routing & Demand Surge',
          keyQuestion: 'How would you architect a machine learning system to allocate delivery partners dynamically during unexpected rain storms?',
        },
      ],
      mentorGuidanceImpact:
        'Dr. Devika Sen guided my capstone architecture and pushed me to benchmark against realistic baselines. That level of scientific rigor directly impressed the Swiggy interview panel.',
      studentAdvice:
        'Always start with simple baseline models before jumping to complex neural networks. Recruiters want to know WHY you chose a model, not just that you imported a library.',
    },
  },
  {
    id: 'aravind',
    name: 'Aravind Krishnan',
    role: 'Design Systems Lead',
    company: 'Uber',
    companyBrand: 'microsoft',
    quote:
      'The emphasis on design tokens, cross-platform accessibility, and collaboration with developers set me apart from thousands of applicants.',
    avatar: studentRahulImg,
    rating: 5,
    previousRole: 'Graphic Designer',
    salaryHike: '+150% Hike',
    programCompleted: 'UI/UX Design',
    category: 'Design',
    cohortYear: 'Cohort 2024',
    storyBadge: 'Domain Pivot',
    capstoneProject: 'Global Multi-Brand Design System with WCAG 2.1 AAA Compliance',
    credentialId: 'NX-75210',
    interviewHighlight: 'Recruited directly after presenting his Capstone defense showcase.',
    caseStudy: {
      startingPackage: '₹4.5 LPA',
      placedPackage: '₹13.5 LPA',
      salaryMultiplier: '3.0x Package Increase',
      backgroundStory:
        'Aravind worked as a print graphic designer creating brochures and posters. He saw the creative industry shifting towards digital interfaces and wanted to master enterprise design token engineering.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'User Interface Foundations',
          duration: 'Months 1–2',
          details: 'Transitioned from raster layouts to responsive 8pt grids, modular typography, and digital accessibility.',
        },
        {
          phase: 'Phase 2',
          title: 'Design System Engineering in Figma',
          duration: 'Months 3–4',
          details: 'Built scalable token libraries with nested components, variables, and cross-platform specs.',
        },
        {
          phase: 'Phase 3',
          title: 'Interactive Prototyping & User Testing',
          duration: 'Month 5',
          details: 'Conducted live user testing sessions to iterate on navigation discoverability.',
        },
        {
          phase: 'Phase 4',
          title: 'Uber Design System Team Placement',
          duration: 'Month 6',
          details: 'Presented his multi-brand design tokens to Uber leads and secured the role.',
        },
      ],
      capstoneDetails: {
        title: 'Nexus: Multi-Brand Global Design System',
        overview: 'An enterprise-scale tokenized design system supporting 3 subsidiary brands across iOS, Android, and Web platforms.',
        techStack: ['Figma', 'Tokens Studio', 'Style Dictionary', 'Storybook', 'WCAG AAA'],
        challengesSolved: [
          'Engineered automated token synchronization into React Tailwind variables.',
          'Maintained 100% WCAG 2.1 AAA accessibility across 90+ components.',
        ],
        architectureHighlights: [
          'Live interactive Storybook documentation for frontend engineers',
          'Dynamic multi-brand theme switching in Figma with variables',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Design Portfolio Walkthrough (60 Mins)',
          focus: 'Token Strategy & Component Architecture',
          keyQuestion: 'How would you structure design tokens to support dark mode, high contrast mode, and multiple brand acquisitions?',
        },
        {
          round: 'Round 2',
          title: 'Developer Collaboration & Critique (60 Mins)',
          focus: 'Handoff & Component Specifications',
          keyQuestion: 'Walk us through your component specification documentation and how you prevent styling drift across engineering teams.',
        },
      ],
      mentorGuidanceImpact:
        'Meera Nair taught me how to communicate with software engineers effectively. Knowing how CSS flexbox and component props work under the hood made me an unstoppable design systems hire.',
      studentAdvice:
        'Learn how developers implement your designs. The best product designers are the ones who speak the language of code and understand implementation constraints.',
    },
  },
  {
    id: 'rohan',
    name: 'Rohan Mehta',
    role: 'Full Stack Cloud Engineer',
    company: 'Atlassian',
    companyBrand: 'microsoft',
    quote:
      'Never wrote React or Node.js before joining. The sprint-based curriculum feels like working inside a real tech team from day one.',
    avatar: studentPriyaImg,
    rating: 5,
    previousRole: 'Fresh Engineering Graduate',
    salaryHike: '+170% Hike',
    programCompleted: 'Full Stack Development',
    category: 'Software Engineering',
    cohortYear: 'Cohort 2025',
    storyBadge: 'College Graduate',
    capstoneProject: 'Collaborative Real-time Kanban Workspace with CRDTs',
    credentialId: 'NX-96104',
    interviewHighlight: 'Secured Tier-1 product company offer with ₹22 LPA package.',
    caseStudy: {
      startingPackage: '₹4.5 LPA',
      placedPackage: '₹15.5 LPA',
      salaryMultiplier: '3.4x Package Increase',
      backgroundStory:
        'A fresh graduate from a tier-3 college, Rohan faced limited on-campus placement opportunities with service companies offering low starting salaries. He wanted to break into tier-1 global product engineering companies directly out of college.',
      learningTimeline: [
        {
          phase: 'Phase 1',
          title: 'Full Stack Development Intensive',
          duration: 'Months 1–2',
          details: 'Coded daily in TypeScript, Next.js, and Node.js, building interactive web apps from day one.',
        },
        {
          phase: 'Phase 2',
          title: 'Cloud DevOps & Containerization',
          duration: 'Months 3–4',
          details: 'Deployed microservices to AWS with Docker, Kubernetes, and automated GitHub Actions CI/CD.',
        },
        {
          phase: 'Phase 3',
          title: 'System Design & High-Throughput WebSockets',
          duration: 'Month 5',
          details: 'Built real-time collaborative state synchronization engines using conflict-free replicated data types (CRDTs).',
        },
        {
          phase: 'Phase 4',
          title: 'Atlassian Engineering Placement',
          duration: 'Month 6',
          details: 'Cleared all 4 rounds of Atlassian interviews, securing an offer with a starting package of ₹15.5 LPA.',
        },
      ],
      capstoneDetails: {
        title: 'TaskFlow: Real-Time Collaborative Team Workspace',
        overview: 'A real-time project management and Kanban workspace allowing multiple teammates to edit simultaneously with zero merge conflicts.',
        techStack: ['Next.js 15', 'TypeScript', 'Node.js', 'WebSockets', 'Yjs (CRDTs)', 'PostgreSQL', 'Docker'],
        challengesSolved: [
          'Resolved concurrent state editing conflicts without central server locking using Yjs CRDTs.',
          'Reduced WebSocket reconnect bandwidth by 74% using binary message compression.',
        ],
        architectureHighlights: [
          'Multi-stage Docker builds running on AWS ECS Fargate',
          'Comprehensive end-to-end testing with Playwright',
        ],
      },
      interviewRounds: [
        {
          round: 'Round 1',
          title: 'Coding & Algorithmic Problem Solving (60 Mins)',
          focus: 'Graphs & Hash Maps',
          keyQuestion: 'Detect cycles in task dependency graphs and output the optimal topological build order.',
        },
        {
          round: 'Round 2',
          title: 'Full Stack Web Architecture (60 Mins)',
          focus: 'Real-Time State Sync & WebSockets',
          keyQuestion: 'How would you architect a live document collaboration feature to prevent data loss during network disconnection?',
        },
        {
          round: 'Round 3',
          title: 'Values & Engineering Culture (45 Mins)',
          focus: 'Team Collaboration & Open Honest Feedback',
          keyQuestion: 'Tell us about a time you gave constructive feedback on a peer pull request that was initially received defensively.',
        },
      ],
      mentorGuidanceImpact:
        'Rishi Kapoor from Atlassian coached me on Atlassian’s unique engineering values and system design expectations. His mentorship was the catalyst for my success.',
      studentAdvice:
        'Don’t be discouraged by your college tier. In top product tech companies, your demonstrable GitHub code, system design knowledge, and communication skills speak 100 times louder than your college name.',
    },
  },
]

export const HIRING_PARTNERS_LIST = [
  { name: 'Microsoft', color: '#00A4EF' },
  { name: 'Amazon', color: '#FF9900' },
  { name: 'Google', color: '#4285F4' },
  { name: 'Adobe', color: '#FA0F00' },
  { name: 'Razorpay', color: '#0C2340' },
  { name: 'Swiggy', color: '#FC8019' },
  { name: 'Uber', color: '#000000' },
  { name: 'Atlassian', color: '#0052CC' },
]

export const PLACEMENT_STATISTICS = [
  { value: '95%', label: 'Placement Success Rate', sub: 'Within 6 months of cohort completion' },
  { value: '140%', label: 'Average Salary Hike', sub: 'Calculated across all graduated alumni' },
  { value: '₹18.5 L', label: 'Average Starting Package', sub: 'For top 30% cohort graduates' },
  { value: '500+', label: 'Hiring Tech Partners', sub: 'Global tech enterprises & funded scaleups' },
]

export const PLACEMENT_FAQS = [
  {
    q: 'How does the Nexarats career placement team assist graduates?',
    a: 'Every student receives an assigned career coach, customized resume reviews, 5+ 1-on-1 mock technical interviews with Big Tech staff engineers, and direct referral pushes to our network of 500+ hiring partners.',
  },
  {
    q: 'Can students from non-technical backgrounds really switch careers?',
    a: 'Yes! Over 42% of our graduates have non-CS backgrounds (including QA, sales, operations, commerce, and mechanical engineering). Our structured curriculum starts from core fundamentals and builds up to production-grade system designs.',
  },
  {
    q: 'Are the salary hike numbers verified?',
    a: 'All reported salary hikes and offer letters are audited and verified through student offer documentation prior to publication.',
  },
  {
    q: 'Do I get access to alumni networks after graduation?',
    a: 'Yes, all alumni receive lifetime access to the Nexarats Global Alumni Community, exclusive hiring postings, and recurring tech webinars.',
  },
]
