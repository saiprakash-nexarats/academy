# NEXARATS ACADEMY — REPOSITORY INVENTORY
## Complete Inventory of Existing Files, Components, Routes, and Assets

- **Document ID:** `01_EXISTING_REPOSITORY_INVENTORY`
- **Execution Date:** 2026-10-13
- **Classification:** Architectural Inventory & Baseline Manifest

---

## 1. Directory Structure Overview

```text
academy/
├── .gitignore                    # Standard Node/Vite git ignore definitions
├── .oxlintrc.json                # Oxlint rules configuration
├── README.md                     # Project README
├── index.html                    # HTML entry point with Plus Jakarta Sans & Inter
├── package.json                  # Root npm package definition
├── package-lock.json             # Root npm lockfile
├── postcss.config.js             # PostCSS with Tailwind & Autoprefixer
├── tailwind.config.js            # Tailwind design system configuration
├── tsconfig.json                 # Root TypeScript references
├── tsconfig.app.json             # App TypeScript compiler configuration
├── tsconfig.node.json            # Node/Vite TypeScript compiler configuration
├── vite.config.ts                # Vite build & alias configuration
│
├── dist/                         # Production build output directory
│   ├── index.html
│   └── assets/
│
├── public/                       # Static public assets
│   ├── favicon.svg
│   └── og-image.png
│
├── server/                       # Backend configuration directory (Skeleton)
│   ├── package.json              # Express / Prisma package manifest (no src)
│   └── tsconfig.json             # TypeScript configuration for server
│
└── src/                          # Application source directory
    ├── App.tsx                   # Main React component & React Router setup
    ├── index.css                 # Global styles, Tailwind layers & utility classes
    ├── main.tsx                  # React DOM entry point
    │
    ├── assets/                   # High-resolution raster and vector images (15 files)
    ├── components/               # Reusable UI & Modal components
    │   ├── common/               # Logos, avatars, scroll utility
    │   ├── modals/               # Global modal dialogs (Login, Enroll, Demo, Cert)
    │   └── ui/                   # Atomic UI primitives (Badge, Button, Card)
    │
    ├── context/                  # React Context providers (ModalContext)
    ├── data/                     # Mock data models & catalogs (11 files)
    ├── hooks/                    # Custom React hooks (useMediaQuery)
    ├── layouts/                  # Structural page layouts (Header, Footer, MainLayout)
    ├── lib/                      # Utility functions (cn / clsx / twMerge)
    ├── pages/                    # Route page views (8 pages + index)
    ├── sections/                 # Landing page & modular content sections (14 folders)
    └── types/                    # TypeScript interfaces & types
```

---

## 2. Configuration & Build Files

| File Path | Size | Description & Key Settings |
| :--- | :--- | :--- |
| `package.json` | 887 B | Declares React 19, Vite 8, Tailwind 3.4, React Router 7, Zod 4, Framer Motion 14. Scripts: `dev`, `build`, `lint`, `preview`. |
| `vite.config.ts` | 387 B | Configures `@vitejs/plugin-react` and `@` path alias mapped to `./src`. |
| `tailwind.config.js` | 2,496 B | Custom color palettes (`brand`, `navy`, `slate`, `surface`), shadows (`card`, `card-hover`, `button-glow`, `elevated`, `floating`), fonts, and keyframes. |
| `postcss.config.js` | 80 B | Configures Tailwind CSS and Autoprefixer plugins. |
| `tsconfig.json` | 119 B | Project reference coordinator pointing to `tsconfig.app.json` and `tsconfig.node.json`. |
| `tsconfig.app.json` | 754 B | Target `ES2022`, module `ESNext`, strict type checking, path alias `@/*` -> `./src/*`. |
| `tsconfig.node.json` | 558 B | Target `ES2022` for Vite bundler scripts. |
| `.oxlintrc.json` | 245 B | Rust-based linter settings enforcing React and TypeScript best practices. |
| `index.html` | 1,081 B | HTML skeleton preconnecting Google Fonts (`Plus Jakarta Sans`, `Inter`), favicon, viewport meta tags. |

---

## 3. Source Code Inventory (`src/`)

### 3.1 Application Entry & Layouts
| File Path | Size | Purpose & Components Exported |
| :--- | :--- | :--- |
| `src/main.tsx` | 226 B | Application mount point. Renders `<App />` inside `StrictMode` at `#root`. |
| `src/App.tsx` | 1,236 B | Root router. Configures `<BrowserRouter>`, `<ScrollToTop>`, `<ModalProvider>`, and routes for `/`, `/programs`, `/courses`, `/mentors`, `/success-stories`, `/about`, `/contact`, `*`. |
| `src/index.css` | 2,806 B | Imports Tailwind utilities, defines CSS root variables, `.academy-container`, `.academy-card`, `.gradient-brand-text`, `.btn-brand-primary`, custom scrollbar, and `@keyframes marqueeSlow`. |
| `src/layouts/MainLayout.tsx` | 468 B | Standard shell wrapping `<Header />`, `<Outlet />`, and `<Footer />`. |
| `src/layouts/Header.tsx` | 12,054 B | Sticky desktop and mobile responsive navigation bar, search input popover, active route indicator, mobile drawer, and CTA triggers. |
| `src/layouts/Footer.tsx` | 5,688 B | Enterprise 5-column footer with address, admissions contact, curriculum links, legal links, and optional pre-footer CTA banner. |
| `src/layouts/index.ts` | 79 B | Barrel export for layouts. |

### 3.2 Pages (`src/pages/`)
| File Path | Size | Route | Core Functionality |
| :--- | :--- | :--- | :--- |
| `HomePage.tsx` | 1,645 B | `/` | Composes Hero, Stats, Programs, Job Assurance, Why Choose, Platform Experience, Testimonials, and CTA sections. |
| `ProgramsPage.tsx` | 4,429 B | `/programs`, `/programs/:programId` | Career program catalog with slug-based category tabs (Software Engineering, AI, Design, Marketing) and search filtering. |
| `CoursesPage.tsx` | 27,433 B | `/courses` | Modular technical course library with 8 domain categories, level filters, interactive syllabus modal viewer, and FAQ. |
| `MentorsPage.tsx` | 23,530 B | `/mentors` | Directory of 80+ industry mentors with domain filters, bio modals, and interactive 3-step 1:1 scheduling flow. |
| `SuccessStoriesPage.tsx` | 33,622 B | `/success-stories` | Placement outcomes dashboard, salary growth metrics, alumni case studies, capstone project highlights, and detailed story modals. |
| `AboutPage.tsx` | 7,128 B | `/about` | Institutional mission, global impact stats, 4 educational pillars, 6-month methodology pathway, and accreditation FAQs. |
| `ContactPage.tsx` | 2,084 B | `/contact` | Academic counselor advisory contact page with direct helpline, email, campus location, and admissions FAQs. |
| `NotFoundPage.tsx` | 906 B | `*` | 404 error page with branding and button to navigate back to Home. |
| `index.ts` | 244 B | N/A | Barrel export for all 8 pages. |

### 3.3 Reusable Components (`src/components/`)
#### UI Primitives (`src/components/ui/`)
- `Button.tsx` (2,545 B): Highly customizable button primitive supporting variants (`primary`, `secondary`, `outline`, `ghost`, `soft`), sizes (`sm`, `md`, `lg`), loading spinner states, and icon slots.
- `Badge.tsx` (1,280 B): Tag/pill component with variants (`primary`, `secondary`, `success`, `neutral`) and optional icons.
- `Card.tsx` (673 B): Standard container with card shadow and border styling.

#### Common Utilities (`src/components/common/`)
- `Logo.tsx` (1,297 B): SVG and typography logo for Nexarats Academy with inverted dark-mode variant support.
- `CompanyLogo.tsx` (4,305 B): Vector brand icons for hiring partners (Microsoft, Google, Amazon, Meta, Apple, Netflix, Swiggy, etc.).
- `MentorAvatar.tsx` (2,824 B): Avatar image component with fallback initials and company badge overlay.
- `StudentAvatar.tsx` (2,220 B): Student profile image component with status indicators.
- `ScrollToTop.tsx` (768 B): Utility component listening to location path changes to reset window scroll position.
- `index.ts` (147 B): Barrel export for common components.

#### Interactive Modals (`src/components/modals/`)
- `LoginModal.tsx` (5,742 B): Modal for student login with mock credentials helper button and simulated successful redirect state.
- `EnrollModal.tsx` (9,058 B): Prospective student application modal featuring multi-field form, Zod validation, and simulated submission.
- `DemoModal.tsx` (5,628 B): Interactive platform demo modal with video player mockup and simulated 1-on-1 session scheduler.
- `CertificateModal.tsx` (4,375 B): Accredited credential inspection card with download and share simulation.
- `index.ts` (122 B): Barrel export for modals.

### 3.4 Page Sections (`src/sections/`)
| Section Directory | Primary Files | Functionality |
| :--- | :--- | :--- |
| `hero/` & `HeroSection.tsx` | `HeroSection.tsx`, `HeroText.tsx`, `HeroVisual.tsx`, `HeroProgressCard.tsx`, `HeroLiveClassCard.tsx`, `HeroMentorCard.tsx`, `HeroCertificateCard.tsx`, `HeroFeatureRow.tsx` | Interactive hero banner featuring headline, CTA buttons, background glows, student portrait, and 4 floating metric overlay cards. |
| `stats/` | `StatsStrip.tsx` | Metric banner showcasing 50,000+ graduates, 100% placement rate, 80+ mentors, and 4.9/5 satisfaction. |
| `programs/` | `PopularProgramsSection.tsx`, `ProgramCard.tsx` | Grid displaying top 4 flagship career programs with job assurance trust pills, durations, and enroll triggers. |
| `job-assurance/` | `JobAssuranceSection.tsx` | 4 pillars of the 100% Job Assurance Guarantee (500+ partners, mock interviews, portfolios, unlimited drives). |
| `why-choose/` | `WhyChooseSection.tsx` | 8 core competitive advantages divided across 2 balanced columns with hover elevations. |
| `experience/` | `ExperienceSection.tsx`, `PlatformCard.tsx`, `PlatformSidebar.tsx`, `PlatformMainContent.tsx`, `ConnectedFeatureNodes.tsx` | Simulated interactive LMS dashboard preview displaying sidebar navigation, progress bar, active courses, and live sessions. |
| `testimonials/` | `TestimonialsSection.tsx`, `TestimonialCard.tsx` | Smooth auto-scrolling slow-motion marquee of student testimonials with play/pause and manual navigation controls. |
| `cta/` | `CtaBannerSection.tsx` | High-impact gradient conversion banner with graduation cap icon and "Get Started" enrollment trigger. |
| `middle/` | `UnifiedMiddleSection.tsx` | Alternate high-density 2-column layout uniting programs, platform experience, and why choose us. |
| `mentors/` | `MentorsSection.tsx` | Featured mentor highlights grid showcasing ratings, bios, and company affiliations. |
| `about/` | `AboutSection.tsx` | Story and mission summary card with core educational pillars and impact metrics. |
| `contact/` | `ContactSection.tsx` | 2-column contact section featuring admissions office details and multi-field inquiry submission form. |

### 3.5 State, Hooks & Utilities
- `src/context/ModalContext.tsx` & `modalContextBase.ts` (2,077 B): Global React Context managing open/close states and parameters for all 4 global modals.
- `src/hooks/useMediaQuery.ts` (687 B): Responsive breakpoint hook tracking viewport media queries.
- `src/lib/utils.ts` (268 B): Standard `cn()` class merging helper combining `clsx` and `tailwind-merge`.
- `src/types/index.ts` (2,170 B): TypeScript interface definitions for nav items, metrics, programs, mentors, testimonials, courses, and forms.

### 3.6 Data Repository (`src/data/`)
| Data File | Size | Data Objects Defined |
| :--- | :--- | :--- |
| `courses.ts` | 20,926 B | `COURSES_CATALOG`: 12 comprehensive technical courses with full module breakdowns, instructors, durations, and skills. |
| `mentors.ts` | 8,869 B | `MENTORS_DATA`, `MENTORSHIP_BENEFITS`, `MENTOR_FAQS`: 8 detailed mentor profiles with available time slots and topics. |
| `programs.ts` | 2,909 B | `POPULAR_PROGRAMS`, `ALL_PROGRAMS`: 8 flagship career programs with fees, durations, and capstone descriptions. |
| `testimonials.ts` | 41,267 B | `TESTIMONIALS_DATA`, `ALL_SUCCESS_STORIES`, `PLACEMENT_STATISTICS`: 20+ rich alumni placement transformation case studies. |
| `platform.ts` | 2,499 B | `PLATFORM_MENU_ITEMS`, `ANANYA_STUDENT_PROGRESS`, `UPCOMING_LIVE_CLASSES`, `CONNECTED_FEATURE_NODES`. |
| `company.ts` | 3,190 B | `COMPANY_ABOUT_DATA`, `FAQ_DATA`, `CONTACT_INFO_DATA`. |
| `whyChoose.ts` | 1,648 B | `WHY_CHOOSE_LEFT`, `WHY_CHOOSE_RIGHT`: 8 competitive differentiators. |
| `navigation.ts` | 1,338 B | `PRIMARY_NAV_ITEMS`, `FOOTER_QUICK_LINKS`. |
| `stats.ts` | 618 B | `STATS_DATA`: 4 primary institutional impact metrics. |
| `mockData.ts` | 293 B | Backward-compatibility alias exports. |

### 3.7 Static Media Assets (`src/assets/`)
| File Name | Format | Size | Visual Content |
| :--- | :--- | :--- | :--- |
| `hero-student-clean.png` | PNG | 1.48 MB | Primary hero student transparent cutout (used in `HeroVisual.tsx`) |
| `hero-student-seamless.png`| PNG | 1.58 MB | Alternative high-resolution cutout asset |
| `hero-student.jpg` | JPG | 696 KB | Original raw hero photo |
| `course-fullstack.jpg` | JPG | 730 KB | Full Stack Development course card banner |
| `course-datascience.jpg` | JPG | 67 KB | Data Science & AI course card banner |
| `course-uiux.jpg` | JPG | 53 KB | UI/UX Design course card banner |
| `course-marketing.jpg` | JPG | 68 KB | Digital Marketing course card banner |
| `mentor-arjun.jpg` | JPG | 645 KB | Arjun Mehta portrait image |
| `student-ananya.jpg` | JPG | 43 KB | Ananya Sharma portrait image |
| `student-priya.jpg` | JPG | 40 KB | Priya Nair portrait image |
| `student-rahul.jpg` | JPG | 50 KB | Rahul Verma portrait image |
| `student-sneha.jpg` | JPG | 27 KB | Sneha Patel portrait image |
| `hero.png` | PNG | 13 KB | Legacy hero graphics asset |
| `react.svg` | SVG | 4.1 KB | Official React logo vector |
| `vite.svg` | SVG | 8.7 KB | Official Vite logo vector |

---

## 4. Server Directory Manifest (`server/`)

| File Path | Size | Real Content Status |
| :--- | :--- | :--- |
| `server/package.json` | 756 B | Skeleton package manifest. Declares dependencies on `@prisma/client`, `cors`, `dotenv`, `express`, `zod`. |
| `server/tsconfig.json` | 335 B | TypeScript options targeting `src/**/*`. |
| `server/src/` | N/A | **Does not exist.** |
| `server/prisma/` | N/A | **Does not exist.** |
| `server/node_modules/` | N/A | **Does not exist.** |

**Conclusion:** The backend directory is completely empty of functional code. There are zero running microservices, zero API endpoints, and zero database connectors.
