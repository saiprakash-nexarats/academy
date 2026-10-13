-- Nexarats Academy Phase 2 Migration
-- Seeds authoritative system roles and core permissions catalog.

-- 1. Insert Core Permissions
INSERT INTO permissions (code, description) VALUES
    ('user:read', 'View user profiles and account metadata'),
    ('user:write', 'Update user profiles and account settings'),
    ('user:manage', 'Manage account provisioning and administrative states'),
    ('tenant:read', 'View tenant configuration and metadata'),
    ('tenant:write', 'Update tenant settings and branding'),
    ('tenant:admin', 'Full administrative authority over tenant resources'),
    ('course:read', 'Access published course curriculum and learning lessons'),
    ('course:write', 'Create and modify course materials and assignments'),
    ('course:publish', 'Publish or archive academic programs and courses'),
    ('assignment:submit', 'Submit assignments and projects for review'),
    ('assignment:grade', 'Grade student submissions and provide feedback'),
    ('mentorship:book', 'Book 1:1 mentorship sessions with instructors'),
    ('mentorship:conduct', 'Conduct scheduled 1:1 mentorship calls and submit reviews'),
    ('exam:take', 'Participate in proctored academic examinations'),
    ('exam:proctor', 'Create exams, review integrity flags, and score tests'),
    ('finance:read', 'View financial invoices, payment receipts, and balance reports'),
    ('finance:write', 'Manage fee structures, issue refunds, and process transactions'),
    ('crm:read', 'View prospective student leads and counseling pipeline'),
    ('crm:write', 'Update admissions status and issue admission offers'),
    ('audit:read', 'Inspect enterprise security and compliance audit logs')
ON CONFLICT (code) DO NOTHING;

-- 2. Insert Authoritative 11 System Roles
INSERT INTO roles (code, name, description, is_system_role) VALUES
    ('ROLE_STUDENT', 'B2C Learner', 'Enrolled courses, assignments, live sessions, mentor booking', TRUE),
    ('ROLE_EMPLOYEE', 'Corporate Learner', 'Assigned corporate tracks, compliance modules, skill matrix', TRUE),
    ('ROLE_MENTOR', 'Mentor / Instructor', 'Assigned cohorts, scheduled live sessions, assignment grading', TRUE),
    ('ROLE_TA', 'Teaching Assistant', 'Cohort forum, lab queue, homework review queue', TRUE),
    ('ROLE_COUNSELOR', 'Admissions Counselor', 'Prospective leads, inquiry pipeline, counseling calendar', TRUE),
    ('ROLE_EXAMINER', 'Proctor / Examiner', 'Question bank, exam schedules, proctoring telemetry', TRUE),
    ('ROLE_FINANCE', 'Finance Officer', 'Invoices, payment gateways, installment schedules, refunds', TRUE),
    ('ROLE_ACADEMY_ADMIN', 'Academy Administrator', 'All academy programs, batches, mentors, students, certificates', TRUE),
    ('ROLE_CORP_ADMIN', 'Corporate HR / L&D Admin', 'Corporate employees, departments, licenses, compliance reports', TRUE),
    ('ROLE_GUARDIAN', 'Parent / Sponsor', 'Linked student attendance, grades, installments, counselor chat', TRUE),
    ('ROLE_SUPER_ADMIN', 'Platform Super Admin', 'Global fleet of tenants, subscriptions, security policies, logs', TRUE)
ON CONFLICT (code) DO NOTHING;

-- 3. Link Default Role Permissions
-- ROLE_STUDENT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_STUDENT' AND p.code IN ('course:read', 'assignment:submit', 'mentorship:book', 'exam:take')
ON CONFLICT DO NOTHING;

-- ROLE_EMPLOYEE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_EMPLOYEE' AND p.code IN ('course:read', 'assignment:submit', 'exam:take')
ON CONFLICT DO NOTHING;

-- ROLE_MENTOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_MENTOR' AND p.code IN ('course:read', 'assignment:grade', 'mentorship:conduct')
ON CONFLICT DO NOTHING;

-- ROLE_TA
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_TA' AND p.code IN ('course:read', 'assignment:grade')
ON CONFLICT DO NOTHING;

-- ROLE_COUNSELOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_COUNSELOR' AND p.code IN ('crm:read', 'crm:write')
ON CONFLICT DO NOTHING;

-- ROLE_EXAMINER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_EXAMINER' AND p.code IN ('exam:proctor', 'course:read')
ON CONFLICT DO NOTHING;

-- ROLE_FINANCE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_FINANCE' AND p.code IN ('finance:read', 'finance:write')
ON CONFLICT DO NOTHING;

-- ROLE_ACADEMY_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_ACADEMY_ADMIN' AND p.code IN (
    'user:read', 'user:write', 'tenant:read', 'tenant:write',
    'course:read', 'course:write', 'course:publish', 'assignment:grade',
    'mentorship:conduct', 'exam:proctor', 'finance:read', 'crm:read', 'crm:write'
)
ON CONFLICT DO NOTHING;

-- ROLE_CORP_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_CORP_ADMIN' AND p.code IN ('user:read', 'tenant:read', 'course:read')
ON CONFLICT DO NOTHING;

-- ROLE_GUARDIAN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_GUARDIAN' AND p.code IN ('course:read', 'finance:read')
ON CONFLICT DO NOTHING;

-- ROLE_SUPER_ADMIN (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;
