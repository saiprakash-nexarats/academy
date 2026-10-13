# NEXARATS ACADEMY — DESIGN SYSTEM PRESERVATION & ENTERPRISE EXPANSION
## Immutable Core Tokens, Component Primitives, Dark Mode, and Multi-Portal Extension

- **Document ID:** `05_DESIGN_SYSTEM_PRESERVATION`
- **Execution Date:** 2026-10-13
- **Classification:** Authoritative Design System Specification & Token Extension

---

## 1. Immutable Baseline Design Tokens

These values are extracted directly from the authoritative configuration files (`tailwind.config.js`, `src/index.css`, `index.html`) and must be strictly preserved across all portals, mobile screens, and phases.

### 1.1 Color Palettes

#### Brand Royal Blue Palette (Primary Action & Accent)
| Token | Hex Value | Application in UI |
| :--- | :--- | :--- |
| `brand-50` | `#EFF6FF` | Soft badge backgrounds, subtle active state fills |
| `brand-100` | `#DBEAFE` | Badge borders, active tab backgrounds |
| `brand-200` | `#BFDBFE` | Interactive border accents, focus rings |
| `brand-300` | `#93C5FD` | Card hover border glow, illustration elements |
| `brand-400` | `#60A5FA` | Star accents, secondary visual highlights |
| `brand-500` | `#3B82F6` | Secondary buttons, selection highlights |
| `brand-600` | `#2563EB` | **Core Brand Color** — Primary buttons, links, active pills, logo |
| `brand-700` | `#1D4ED8` | **Deep Royal Blue** — Button hover state, primary text accents |
| `brand-800` | `#1E40AF` | Button active state, high-contrast dark accents |
| `brand-900` | `#1E3A8A` | Deep navy headers, dark section titles |
| `brand-950` | `#0F1D40` | **Dark Navy** — Deep text contrast, hero accents |

#### Navy & Slate Palettes (Neutral Surfaces & Typography)
| Token | Hex Value | Application in UI |
| :--- | :--- | :--- |
| `slate-50` | `#F8FAFC` | **Global Page Background**, subtle input backgrounds |
| `slate-100` | `#F1F5F9` | Divider lines, card sub-panels, scrollbar tracks |
| `slate-200` | `#E2E8F0` | Default card borders, input borders |
| `slate-300` | `#CBD5E1` | Secondary borders, scrollbar thumbs |
| `slate-400` | `#94A3B8` | Search icons, disabled text |
| `slate-500` | `#64748B` | Secondary captions, timestamps, durations |
| `slate-600` | `#475569` | Body paragraphs, description copy |
| `slate-700` | `#334155` | Form labels, secondary heading copy |
| `slate-800` | `#1E293B` | Default high-contrast body text |
| `slate-900` | `#0F172A` | Primary headings, card titles |
| `navy-800` | `#102A43` | Dark sidebar panel accents |
| `navy-900` | `#0B1A30` | Footer background (`#111C33` composite), dark cards |
| `navy-950` | `#060E1C` | Deepest dark mode background surface |

#### Surface Semantic Tokens
- `surface.background`: `#F8FAFC`
- `surface.card`: `#FFFFFF`
- `surface.subtle`: `#F1F5F9`
- `surface.border`: `#E2E8F0`
- `surface.borderLight`: `#F1F5F9`
- `surface.borderBlue`: `#DBEAFE`

### 1.2 Typography System
- **Primary Typeface:** `Plus Jakarta Sans` (Google Fonts: 400, 500, 600, 700, 800)
- **Secondary Typeface:** `Inter` (Google Fonts: 400, 500, 600, 700)
- **Monospace Typeface:** `JetBrains Mono` / `Fira Code` (for coding sandbox & terminal)
- **Type Hierarchy:**
  - `Hero Display`: 48px – 56px (Desktop), 32px – 40px (Mobile), Weight: 800, Tracking: `-0.025em`, Leading: `1.12`
  - `Heading 1 (H1)`: 36px – 48px, Weight: 800, Tracking: `-0.02em`, Leading: `1.2`
  - `Heading 2 (H2)`: 28px – 36px, Weight: 800, Tracking: `-0.015em`, Leading: `1.25`
  - `Heading 3 (H3)`: 20px – 24px, Weight: 700, Tracking: `-0.01em`, Leading: `1.3`
  - `Heading 4 (H4)`: 16px – 18px, Weight: 700, Tracking: `0`, Leading: `1.4`
  - `Body Standard`: 14px – 16px, Weight: 400 / 500, Leading: `1.6`
  - `Body Small`: 12px – 13px, Weight: 400 / 500, Leading: `1.5`
  - `Caption / Badge`: 10px – 11px, Weight: 700, Tracking: `0.05em` (Uppercase)

### 1.3 Elevation, Shadows & Radii
- **Box Shadows:**
  - `shadow-card`: `0 4px 20px -2px rgba(15, 23, 42, 0.05), 0 2px 6px -1px rgba(15, 23, 42, 0.02)`
  - `shadow-card-hover`: `0 12px 30px -4px rgba(37, 99, 235, 0.12), 0 4px 10px -2px rgba(15, 23, 42, 0.04)`
  - `shadow-elevated`: `0 10px 30px -5px rgba(37, 99, 235, 0.08), 0 4px 8px -2px rgba(15, 23, 42, 0.04)`
  - `shadow-button-glow`: `0 4px 14px 0 rgba(37, 99, 235, 0.35)`
  - `shadow-floating`: `0 20px 40px -10px rgba(15, 23, 42, 0.12), 0 8px 16px -4px rgba(15, 23, 42, 0.06)`
- **Border Radii:**
  - `rounded-card`: `1rem` (16px)
  - `rounded-panel`: `1.25rem` (20px)
  - `rounded-3xl`: `1.5rem` (24px)
  - `rounded-full`: `9999px` (Pills & Circular Badges)

---

## 2. Design System Extension Across 7 Portals

To maintain a unified visual identity across the enterprise platform while accommodating distinct operational workloads, the core design system extends into 7 tailored portal skins sharing the same tokens:

| Portal | Layout Structure | Dominant Visual Tone | Key Component Patterns |
| :--- | :--- | :--- | :--- |
| **Public Landing** *(Preserved)* | Fluid centered max-w-1440, sticky header, full footer | Clean White & Slate with Blue ambient glows | Floating hero cards, marquee carousel, interactive demo cards, modal dialogs |
| **Student LMS** | Collapsible left sidebar (`#131F37`), sticky breadcrumb header, wide content stage | High-contrast learning stage with distraction-free video focus | Course curriculum tree, Monaco coding split-pane, streak counters, progress donuts |
| **Mentor Portal** | Structured 3-column workbench (Schedule / Cohort / Review) | Technical slate with emerald success accents | Side-by-side Git diff viewer, inline rubric scoring, video call overlay, slot scheduler |
| **Academy Admin** | Dense administrative dashboard with top navigation + left sub-menu | Professional executive navy with tabular data density | Filterable data tables, Kanban CRM pipelines, bulk action bars, audit diff cards |
| **Corporate Admin** | Multi-level tree navigation, department switcher, metrics header | Enterprise corporate navy with compliance indicator gauges | Skill radar charts, license allocation sliders, department tree views, SCIM status tags |
| **Employee Portal** | Simplified single-column track view with task checklist | Clean, structured corporate upskilling layout | Mandatory deadline countdowns, skill badge grid, 1-click manager approval requests |
| **Super Admin** | Ultra-dense system operations layout with live telemetry indicators | Dark navy operations console with live status pills | Tenant health gauges, real-time log stream monitors, feature flag toggle switches |

---

## 3. Dark Mode Architecture Strategy

The enterprise platform will implement a dual-mode CSS variable token layer supporting clean transition between Light Mode and Dark Mode without breaking existing utility classes:

### 3.1 Dark Mode Semantic Mapping
```css
/* Light Mode (Default) */
:root {
  --bg-page: #F8FAFC;
  --bg-surface: #FFFFFF;
  --bg-surface-elevated: #FFFFFF;
  --text-primary: #0F172A;
  --text-secondary: #475569;
  --text-muted: #94A3B8;
  --border-default: #E2E8F0;
  --border-subtle: #F1F5F9;
  --brand-primary: #2563EB;
  --brand-hover: #1D4ED8;
}

/* Dark Mode (.dark class on <html>) */
.dark {
  --bg-page: #0B132B;
  --bg-surface: #111C33;
  --bg-surface-elevated: #162444;
  --text-primary: #F8FAFC;
  --text-secondary: #CBD5E1;
  --text-muted: #64748B;
  --border-default: #1E2D4A;
  --border-subtle: #18243C;
  --brand-primary: #3B82F6;
  --brand-hover: #60A5FA;
}
```

### 3.2 Dark Mode Rules
- The public landing page maintains its iconic crisp white/blue aesthetic by default, with dark mode toggle available in user profile.
- Developer coding sandboxes, terminal consoles, and the Super Admin operations portal default to Dark Mode (`#0B132B` background).
- Contrast ratios for text on dark surfaces must maintain a minimum of **4.5:1** for standard text and **3.0:1** for large text (WCAG 2.1 AA).

---

## 4. Mobile & Tablet Design Strategy (Flutter & Dart)

### 4.1 Cross-Platform Token Translation Strategy
All brand colors, radii, and typography scales defined in `@nexarats/ui-tokens` are mapped natively into Flutter's `ThemeData` (Material 3) without requiring Node.js runtime execution in mobile:
- **Web Application:** Consumes `@nexarats/ui-tokens` via Tailwind CSS (`tailwind.config.js`).
- **Flutter Mobile Application (`apps/mobile`):** Consumes native Dart token constants (`NexaratsColors`, `NexaratsTypography`, `NexaratsShadows`, `NexaratsRadius`) translated into a custom `ThemeExtension<NexaratsTheme>` and Material 3 `ColorScheme`.

#### Flutter Brand Theme Mapping Table
| Web / Token Value | CSS / Hex | Flutter Theme Equivalent |
|---|---|---|
| Primary Royal Blue | `#2563EB` | `ColorScheme.primary = Color(0xFF2563EB)` |
| Deep Blue (Hover/Accent) | `#1D4ED8` | `NexaratsColors.deepBlue = Color(0xFF1D4ED8)` |
| Dark Navy (Hero/Surface) | `#0F1D40` | `ColorScheme.onPrimaryContainer = Color(0xFF0F1D40)` |
| Light Background | `#F8FAFC` | `ColorScheme.surface = Color(0xFFF8FAFC)` |
| Card Surface | `#FFFFFF` | `ColorScheme.surfaceContainerLowest = Color(0xFFFFFFFF)` |
| Primary Typography | Plus Jakarta Sans | `GoogleFonts.plusJakartaSansTextTheme()` |
| Card Border Radius | `1rem` (16px) | `RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))` |
| Elevated Card Shadow | `0 4px 20px rgba(15,23,42,0.05)` | `BoxShadow(color: Color(0x0D0F172A), blurRadius: 20, offset: Offset(0, 4))` |

### 4.2 Mobile Touch & Layout Standards (Native iOS & Android)
- **Touch Target Minimum:** 48dp × 48dp (Material 3 standard) / 44pt × 44pt (Apple Human Interface standard) for all interactive buttons, icon triggers, and tab items.
- **Adaptive Navigation:** Core navigation on phones uses `NavigationBar` with 5 destinations: Home, Learn, Live, Community, Profile. On tablets (>= 720dp), dynamically shifts to `NavigationRail` or persistent drawer.
- **Gesture Interactions:** Flutter `Dismissible` for swipe-to-dismiss notifications, `RefreshIndicator` for pull-to-refresh feeds, and `InteractiveViewer` for pinch-to-zoom diagrams.
- **Tablet Responsive Breakpoint (768dp – 1024dp):** Utilizes `LayoutBuilder` / `AdaptiveScaffold` to adapt single-column mobile views into balanced 2-column master-detail grids.

---

## 5. Accessibility & Inclusivity (WCAG 2.1 AA Standards)

1. **Color Contrast:** All text color pairings against background surfaces must pass automated Lighthouse accessibility checks with a score of 98+.
2. **Keyboard Navigability:** Full keyboard navigation (`Tab`, `Shift+Tab`, `Enter`, `Escape`) supported across all interactive elements, modal dialogs, and dropdowns with visible `:focus-visible` brand rings.
3. **Screen Reader Optimization:** Semantic HTML5 (`<header>`, `<nav>`, `<main>`, `<article>`, `<aside>`, `<footer>`), descriptive `aria-label` attributes on icon-only buttons, and `aria-expanded` attributes on collapsible menus.
4. **Reduced Motion:** Honor `prefers-reduced-motion: reduce` by disabling non-essential Framer Motion translate and scale animations, replacing them with simple opacity cross-fades.
