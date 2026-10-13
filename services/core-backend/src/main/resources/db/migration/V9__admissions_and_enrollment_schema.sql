-- Nexarats Academy Phase 5 Migration
-- Enterprise Admissions, Student Applications, Enrollment Engine, Seat Management, Tuition Pricing & Invoices

-- 1. Cohorts / Intakes Table
CREATE TABLE IF NOT EXISTS cohorts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    program_id UUID REFERENCES programs(id) ON DELETE CASCADE,
    course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
    cohort_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    enrollment_start_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enrollment_end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    occupied_seats INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN_FOR_ENROLLMENT' CHECK (status IN ('UPCOMING', 'OPEN_FOR_ENROLLMENT', 'FULL', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cohorts_tenant ON cohorts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_cohorts_program ON cohorts(program_id);
CREATE INDEX IF NOT EXISTS idx_cohorts_course ON cohorts(course_id);
CREATE INDEX IF NOT EXISTS idx_cohorts_status ON cohorts(status);

-- 2. Authoritative Tuition Pricing Table
CREATE TABLE IF NOT EXISTS tuition_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    program_id UUID REFERENCES programs(id) ON DELETE CASCADE,
    course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0),
    display_price VARCHAR(64) NOT NULL,
    tier VARCHAR(32) NOT NULL DEFAULT 'STANDARD' CHECK (tier IN ('STANDARD', 'EARLY_BIRD', 'SCHOLARSHIP', 'CORPORATE')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tuition_prices_program ON tuition_prices(program_id, currency, is_active);
CREATE INDEX IF NOT EXISTS idx_tuition_prices_course ON tuition_prices(course_id, currency, is_active);

-- 3. Student Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_number VARCHAR(64) NOT NULL UNIQUE,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    applicant_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    program_id UUID REFERENCES programs(id) ON DELETE SET NULL,
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    cohort_id UUID REFERENCES cohorts(id) ON DELETE SET NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    experience_level VARCHAR(64) NOT NULL DEFAULT 'beginner',
    statement_of_purpose TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'WITHDRAWN')),
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reviewed_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_decision VARCHAR(32),
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_applications_user ON applications(applicant_user_id);
CREATE INDEX IF NOT EXISTS idx_applications_email ON applications(email);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_applications_tenant ON applications(tenant_id);

-- 4. Application Status History (Immutable Audit Log)
CREATE TABLE IF NOT EXISTS application_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    changed_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_history_app ON application_status_history(application_id);

-- 5. Admission Offers Table
CREATE TABLE IF NOT EXISTS admission_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_number VARCHAR(64) NOT NULL UNIQUE,
    application_id UUID NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    cohort_id UUID NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
    tuition_price_id UUID REFERENCES tuition_prices(id) ON DELETE SET NULL,
    offered_amount_cents BIGINT NOT NULL CHECK (offered_amount_cents >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(32) NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('ISSUED', 'ACCEPTED', 'DECLINED', 'EXPIRED')),
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    issued_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_offers_cohort ON admission_offers(cohort_id, status);

-- 6. Seat Reservations Table (Transactional Capacity Control)
CREATE TABLE IF NOT EXISTS seat_reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cohort_id UUID NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
    applicant_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    offer_id UUID REFERENCES admission_offers(id) ON DELETE SET NULL,
    reservation_token VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'RESERVED' CHECK (status IN ('RESERVED', 'CONFIRMED', 'EXPIRED', 'CANCELLED')),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_seat_reservations_cohort_status ON seat_reservations(cohort_id, status, expires_at);

-- 7. Payment Orders Table
CREATE TABLE IF NOT EXISTS payment_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(64) NOT NULL UNIQUE,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    learner_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    offer_id UUID REFERENCES admission_offers(id) ON DELETE SET NULL,
    cohort_id UUID REFERENCES cohorts(id) ON DELETE SET NULL,
    reservation_id UUID REFERENCES seat_reservations(id) ON DELETE SET NULL,
    amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    provider VARCHAR(32) NOT NULL DEFAULT 'TEST_GATEWAY' CHECK (provider IN ('TEST_GATEWAY', 'STRIPE', 'RAZORPAY')),
    provider_order_id VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' CHECK (status IN ('CREATED', 'PENDING', 'AUTHORIZED', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED')),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    paid_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_orders_learner ON payment_orders(learner_user_id);
CREATE INDEX IF NOT EXISTS idx_payment_orders_provider_id ON payment_orders(provider_order_id);
CREATE INDEX IF NOT EXISTS idx_payment_orders_status ON payment_orders(status);

-- 8. Payment Transactions Table (Charge verification log)
CREATE TABLE IF NOT EXISTS payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_order_id UUID NOT NULL REFERENCES payment_orders(id) ON DELETE CASCADE,
    provider_transaction_id VARCHAR(255) NOT NULL UNIQUE,
    amount_cents BIGINT NOT NULL CHECK (amount_cents >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),
    payment_method VARCHAR(64),
    response_payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_tx_order ON payment_transactions(payment_order_id);

-- 9. Payment Webhook Events (Replay defense & Idempotency)
CREATE TABLE IF NOT EXISTS payment_webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(32) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload_hash VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PROCESSED' CHECK (status IN ('PROCESSED', 'IGNORED', 'FAILED')),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_provider_event_id UNIQUE (provider, event_id)
);

-- 10. Enrollments Table
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_number VARCHAR(64) NOT NULL UNIQUE,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    program_id UUID REFERENCES programs(id) ON DELETE SET NULL,
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    cohort_id UUID NOT NULL REFERENCES cohorts(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    payment_order_id UUID REFERENCES payment_orders(id) ON DELETE SET NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENROLLED' CHECK (status IN ('PENDING', 'ENROLLED', 'ACTIVE', 'COMPLETED', 'WITHDRAWN', 'SUSPENDED')),
    enrolled_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_learner_cohort_enrollment UNIQUE (learner_user_id, cohort_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_learner ON enrollments(learner_user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_cohort ON enrollments(cohort_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_tenant ON enrollments(tenant_id);

-- 11. Enrollment Status History
CREATE TABLE IF NOT EXISTS enrollment_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    changed_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_enrollment_history ON enrollment_status_history(enrollment_id);

-- 12. Invoices & Receipts Table
CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(64) NOT NULL UNIQUE,
    tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL,
    learner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    payment_order_id UUID NOT NULL UNIQUE REFERENCES payment_orders(id) ON DELETE CASCADE,
    enrollment_id UUID REFERENCES enrollments(id) ON DELETE SET NULL,
    subtotal_cents BIGINT NOT NULL CHECK (subtotal_cents >= 0),
    tax_cents BIGINT NOT NULL DEFAULT 0 CHECK (tax_cents >= 0),
    total_cents BIGINT NOT NULL CHECK (total_cents >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(32) NOT NULL DEFAULT 'PAID' CHECK (status IN ('ISSUED', 'PAID', 'VOID', 'REFUNDED')),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP WITH TIME ZONE,
    pdf_receipt_url VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_invoices_learner ON invoices(learner_user_id);
CREATE INDEX IF NOT EXISTS idx_invoices_tenant ON invoices(tenant_id);

-- 13. Grant Permissions to Application User
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'academy_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON cohorts TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON tuition_prices TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON applications TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON application_status_history TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON admission_offers TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON seat_reservations TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON payment_orders TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON payment_transactions TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON payment_webhook_events TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON enrollments TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON enrollment_status_history TO academy_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON invoices TO academy_app_user;
    END IF;
END
$$;

-- 14. Idempotent Seed Data for Initial Cohorts & Pricing
DO $$
DECLARE
    v_prog_fs UUID;
    v_prog_ds UUID;
    v_prog_ui UUID;
    v_prog_mkt UUID;
    v_c_react UUID;
BEGIN
    SELECT id INTO v_prog_fs FROM programs WHERE slug = 'full-stack' LIMIT 1;
    SELECT id INTO v_prog_ds FROM programs WHERE slug = 'data-science' LIMIT 1;
    SELECT id INTO v_prog_ui FROM programs WHERE slug = 'ui-ux' LIMIT 1;
    SELECT id INTO v_prog_mkt FROM programs WHERE slug = 'digital-marketing' LIMIT 1;
    SELECT id INTO v_c_react FROM courses WHERE slug = 'react-nextjs-architecture' LIMIT 1;

    -- Cohort 1: Full Stack 2026 Cohort A
    IF v_prog_fs IS NOT NULL AND NOT EXISTS (SELECT 1 FROM cohorts WHERE cohort_code = 'COHORT-FS-2026A') THEN
        INSERT INTO cohorts (program_id, cohort_code, name, start_date, end_date, enrollment_end_date, capacity, status)
        VALUES (v_prog_fs, 'COHORT-FS-2026A', 'Full Stack Engineering Cohort 2026-A', '2026-11-01', '2027-05-01', '2026-10-31 23:59:59Z', 30, 'OPEN_FOR_ENROLLMENT');

        INSERT INTO tuition_prices (program_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_prog_fs, 'USD', 249900, '$2,499', 'STANDARD', TRUE);
        INSERT INTO tuition_prices (program_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_prog_fs, 'INR', 19900000, '₹1,99,000', 'STANDARD', TRUE);
    END IF;

    -- Cohort 2: Data Science 2026 Cohort A
    IF v_prog_ds IS NOT NULL AND NOT EXISTS (SELECT 1 FROM cohorts WHERE cohort_code = 'COHORT-DS-2026A') THEN
        INSERT INTO cohorts (program_id, cohort_code, name, start_date, end_date, enrollment_end_date, capacity, status)
        VALUES (v_prog_ds, 'COHORT-DS-2026A', 'Data Science & AI Cohort 2026-A', '2026-11-15', '2027-05-15', '2026-11-10 23:59:59Z', 25, 'OPEN_FOR_ENROLLMENT');

        INSERT INTO tuition_prices (program_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_prog_ds, 'USD', 279900, '$2,799', 'STANDARD', TRUE);
        INSERT INTO tuition_prices (program_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_prog_ds, 'INR', 21900000, '₹2,19,000', 'STANDARD', TRUE);
    END IF;

    -- Cohort 3: UI/UX Design 2026 Cohort A
    IF v_prog_ui IS NOT NULL AND NOT EXISTS (SELECT 1 FROM cohorts WHERE cohort_code = 'COHORT-UI-2026A') THEN
        INSERT INTO cohorts (program_id, cohort_code, name, start_date, end_date, enrollment_end_date, capacity, status)
        VALUES (v_prog_ui, 'COHORT-UI-2026A', 'UI/UX Design Masterclass Cohort 2026-A', '2026-11-01', '2027-05-01', '2026-10-31 23:59:59Z', 25, 'OPEN_FOR_ENROLLMENT');

        INSERT INTO tuition_prices (program_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_prog_ui, 'USD', 199900, '$1,999', 'STANDARD', TRUE);
    END IF;

    -- Cohort 4: React & Next.js Modular Course Cohort
    IF v_c_react IS NOT NULL AND NOT EXISTS (SELECT 1 FROM cohorts WHERE cohort_code = 'COHORT-REACT-2026A') THEN
        INSERT INTO cohorts (course_id, cohort_code, name, start_date, end_date, enrollment_end_date, capacity, status)
        VALUES (v_c_react, 'COHORT-REACT-2026A', 'Advanced React 19 & Next.js 15 Intensive', '2026-10-25', '2026-12-20', '2026-10-24 23:59:59Z', 50, 'OPEN_FOR_ENROLLMENT');

        INSERT INTO tuition_prices (course_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_c_react, 'USD', 49900, '$499', 'STANDARD', TRUE);
        INSERT INTO tuition_prices (course_id, currency, amount_cents, display_price, tier, is_active)
        VALUES (v_c_react, 'INR', 3999900, '₹39,999', 'STANDARD', TRUE);
    END IF;
END
$$;
