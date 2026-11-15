-- Run as postgres in the intended production project after script 02.
-- This changes defaults for future objects created by barber_migrator only;
-- it does not change ACLs on existing tables, sequences, or functions.
BEGIN;

-- Verify the temporary role switch is authorized and has not already been enabled.
DO $$
BEGIN
    IF current_user <> 'postgres' OR session_user <> 'postgres' THEN
        RAISE EXCEPTION 'Run this transaction as postgres without an active role switch';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_auth_members m
        JOIN pg_roles member ON member.oid = m.member
        JOIN pg_roles granted ON granted.oid = m.roleid
        WHERE member.rolname = 'postgres'
          AND granted.rolname = 'barber_migrator'
          AND m.admin_option
    ) THEN
        RAISE EXCEPTION 'postgres does not have ADMIN OPTION on barber_migrator';
    END IF;

    IF pg_has_role('postgres', 'barber_migrator', 'SET') THEN
        RAISE EXCEPTION 'postgres already has SET on barber_migrator; review memberships';
    END IF;
END
$$;

-- Temporary SET only; INHERIT remains false and ADMIN is not enabled here.
GRANT barber_migrator TO postgres
    WITH SET TRUE, INHERIT FALSE;

SET LOCAL ROLE barber_migrator;

-- Global revokes apply to all schemas for future objects created by this
-- dedicated role. barber_migrator must remain dedicated to application DDL.
ALTER DEFAULT PRIVILEGES
    REVOKE ALL ON TABLES
    FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

ALTER DEFAULT PRIVILEGES
    REVOKE ALL ON SEQUENCES
    FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

ALTER DEFAULT PRIVILEGES
    REVOKE EXECUTE ON FUNCTIONS
    FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

-- Schema defaults are additive to global defaults, so revoke separately in public.
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    REVOKE ALL ON TABLES
    FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    REVOKE ALL ON SEQUENCES
    FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    REVOKE EXECUTE ON FUNCTIONS
    FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

RESET ROLE;

-- Remove the temporary SET option while preserving ADMIN OPTION.
REVOKE SET OPTION FOR barber_migrator FROM postgres;

-- Validate membership restoration and effective global + public defaults.
DO $$
DECLARE
    migrator_oid oid;
    public_oid oid;
BEGIN
    IF pg_has_role('postgres', 'barber_migrator', 'SET') THEN
        RAISE EXCEPTION 'postgres still has SET on barber_migrator';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_auth_members m
        JOIN pg_roles member ON member.oid = m.member
        JOIN pg_roles granted ON granted.oid = m.roleid
        WHERE member.rolname = 'postgres'
          AND granted.rolname = 'barber_migrator'
          AND m.admin_option
    ) THEN
        RAISE EXCEPTION 'ADMIN OPTION on barber_migrator was not preserved';
    END IF;

    SELECT oid INTO migrator_oid
    FROM pg_roles
    WHERE rolname = 'barber_migrator';

    SELECT oid INTO public_oid
    FROM pg_namespace
    WHERE nspname = 'public';

    IF migrator_oid IS NULL OR public_oid IS NULL THEN
        RAISE EXCEPTION 'barber_migrator or schema public is missing';
    END IF;

    IF EXISTS (
        WITH target_grantees(grantee) AS (
            SELECT 0::oid
            UNION ALL
            SELECT oid
            FROM pg_roles
            WHERE rolname IN (
                'anon', 'authenticated', 'service_role', 'barber_runtime'
            )
        ),
        object_types(object_type) AS (
            VALUES ('r'::"char"), ('S'::"char"), ('f'::"char")
        ),
        global_defaults AS (
            SELECT
                t.object_type,
                COALESCE(d.defaclacl, acldefault(t.object_type, migrator_oid)) AS acl
            FROM object_types t
            LEFT JOIN pg_default_acl d
                   ON d.defaclrole = migrator_oid
                  AND d.defaclnamespace = 0
                  AND d.defaclobjtype = t.object_type
        ),
        effective_default_entries AS (
            SELECT acl.grantee
            FROM global_defaults g
            CROSS JOIN LATERAL aclexplode(g.acl) acl

            UNION ALL

            SELECT acl.grantee
            FROM pg_default_acl d
            CROSS JOIN LATERAL aclexplode(d.defaclacl) acl
            WHERE d.defaclrole = migrator_oid
              AND d.defaclnamespace = public_oid
              AND d.defaclobjtype IN ('r', 'S', 'f')
        )
        SELECT 1
        FROM effective_default_entries e
        JOIN target_grantees t ON t.grantee = e.grantee
    ) THEN
        RAISE EXCEPTION
            'Unexpected default privileges remain for PUBLIC or an API/runtime role';
    END IF;
END
$$;

COMMIT;
