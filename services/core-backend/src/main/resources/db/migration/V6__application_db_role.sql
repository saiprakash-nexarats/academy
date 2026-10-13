-- Flyway Migration V6: Non-Superuser Application Database Role for Row-Level Security
-- In PostgreSQL, superuser roles (such as the default container admin) bypass RLS.
-- This dedicated unprivileged application role enforces Row-Level Security policies.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'academy_app_user') THEN
        CREATE ROLE academy_app_user NOLOGIN;
    END IF;
END
$$;

GRANT USAGE ON SCHEMA public TO academy_app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO academy_app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO academy_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO academy_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO academy_app_user;
