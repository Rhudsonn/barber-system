-- Run once as postgres in the intended production project after reviewing
-- 00_inventory_read_only.sql. All changes below are scoped to Zheiry objects.
BEGIN;

-- Verify the assumptions required by the ownership and grant operations.
DO $$
DECLARE
    postgres_oid oid;
    db_owner_oid oid;
    migrator_oid oid;
    runtime_oid oid;
    schema_owner_name text;
    mismatched_tables integer;
    table_name text;
    sequence_name text;
    sequence_owner_name text;
BEGIN
    IF current_setting('server_version_num')::integer < 160000 THEN
        RAISE EXCEPTION 'This script requires PostgreSQL 16 or later';
    END IF;

    IF session_user <> 'postgres' OR current_user <> 'postgres' THEN
        RAISE EXCEPTION 'Run this transaction as postgres without an active role switch';
    END IF;

    SELECT oid INTO postgres_oid FROM pg_roles WHERE rolname = 'postgres';
    SELECT oid INTO migrator_oid FROM pg_roles WHERE rolname = 'barber_migrator';
    SELECT oid INTO runtime_oid FROM pg_roles WHERE rolname = 'barber_runtime';

    IF postgres_oid IS NULL OR migrator_oid IS NULL OR runtime_oid IS NULL THEN
        RAISE EXCEPTION 'An expected role is missing';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname IN ('barber_runtime', 'barber_migrator')
          AND (
              NOT rolcanlogin
              OR rolinherit
              OR rolsuper
              OR rolcreatedb
              OR rolcreaterole
              OR rolreplication
              OR rolbypassrls
          )
    ) THEN
        RAISE EXCEPTION 'An application role has an administrative or bypass attribute';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_auth_members m
        WHERE m.member = postgres_oid
          AND m.roleid = migrator_oid
          AND m.admin_option
    ) THEN
        RAISE EXCEPTION
            'postgres lacks ADMIN OPTION on barber_migrator for the temporary SET grant';
    END IF;

    IF pg_has_role('postgres', 'barber_migrator', 'SET') THEN
        RAISE EXCEPTION
            'postgres already has SET on barber_migrator; review existing memberships first';
    END IF;

    IF NOT pg_has_role('postgres', 'pg_database_owner', 'SET') THEN
        RAISE EXCEPTION 'postgres cannot assume pg_database_owner';
    END IF;

    -- GRANT CONNECT is executed as postgres below. Require database ownership
    -- or an explicit grant option for CONNECT.
    SELECT datdba INTO db_owner_oid
    FROM pg_database
    WHERE datname = current_database();

    IF db_owner_oid <> postgres_oid
       AND NOT EXISTS (
           SELECT 1
           FROM pg_database d
           CROSS JOIN LATERAL aclexplode(
               COALESCE(d.datacl, acldefault('d', d.datdba))
           ) acl
           WHERE d.datname = current_database()
             AND acl.grantee = postgres_oid
             AND acl.privilege_type = 'CONNECT'
             AND acl.is_grantable
       )
    THEN
        RAISE EXCEPTION
            'postgres is neither database owner nor grant-option holder for CONNECT';
    END IF;

    -- Schema grants below are executed as pg_database_owner.
    SELECT pg_get_userbyid(n.nspowner)
      INTO schema_owner_name
    FROM pg_namespace n
    WHERE n.nspname = 'public';

    IF schema_owner_name IS DISTINCT FROM 'pg_database_owner' THEN
        RAISE EXCEPTION
            'public is not owned by pg_database_owner; review the schema ACL before continuing';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_auth_members
        WHERE member = runtime_oid
    ) THEN
        RAISE EXCEPTION
            'barber_runtime has role memberships; review inherited or assumable privileges first';
    END IF;

    -- Confirm all expected tables exist and are currently owned by postgres.
    WITH expected(table_name) AS (
        VALUES
            ('administrador'),
            ('barbearia'),
            ('barbeiros'),
            ('clientes'),
            ('servicos'),
            ('horario_funcionamento'),
            ('bloqueio'),
            ('agendamentos'),
            ('comissoes'),
            ('notificacoes'),
            ('recuperacoes_senha'),
            ('historico_agendamentos'),
            ('flyway_schema_history')
    )
    SELECT count(*)
      INTO mismatched_tables
    FROM expected e
    LEFT JOIN pg_namespace n ON n.nspname = 'public'
    LEFT JOIN pg_class c
           ON c.relnamespace = n.oid
          AND c.relname = e.table_name
          AND c.relkind IN ('r', 'p')
    WHERE c.oid IS NULL
       OR c.relowner <> postgres_oid;

    IF mismatched_tables <> 0 THEN
        RAISE EXCEPTION
            'An expected table is missing or is not owned by postgres';
    END IF;

    -- Confirm the 12 application identity sequences exist and are owned by postgres.
    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
    ] LOOP
        sequence_name := pg_get_serial_sequence(
            format('%I.%I', 'public', table_name),
            'id'
        );

        IF sequence_name IS NULL THEN
            RAISE EXCEPTION
                'Identity sequence is missing for public.%', table_name;
        END IF;

        SELECT pg_get_userbyid(c.relowner)
          INTO sequence_owner_name
        FROM pg_class c
        WHERE c.oid = sequence_name::regclass;

        IF sequence_owner_name IS DISTINCT FROM 'postgres' THEN
            RAISE EXCEPTION
                'Unexpected sequence owner for public.%', table_name;
        END IF;
    END LOOP;
END
$$;

-- Temporary SET permission only; INHERIT remains false and ADMIN is not enabled.
GRANT barber_migrator TO postgres
    WITH SET TRUE, INHERIT FALSE;

-- Keep CONNECT grant under postgres, the validated database owner/grantor.
DO $$
BEGIN
    EXECUTE format(
        'GRANT CONNECT ON DATABASE %I TO barber_runtime, barber_migrator',
        current_database()
    );
END
$$;

-- pg_database_owner is the validated owner of public in this project.
SET LOCAL ROLE pg_database_owner;

GRANT USAGE ON SCHEMA public TO barber_runtime;
GRANT USAGE, CREATE ON SCHEMA public TO barber_migrator;
REVOKE CREATE ON SCHEMA public FROM barber_runtime;

RESET ROLE;

-- postgres is still the owner of the existing tables and can SET to migrator.
ALTER TABLE public.administrador OWNER TO barber_migrator;
ALTER TABLE public.barbearia OWNER TO barber_migrator;
ALTER TABLE public.barbeiros OWNER TO barber_migrator;
ALTER TABLE public.clientes OWNER TO barber_migrator;
ALTER TABLE public.servicos OWNER TO barber_migrator;
ALTER TABLE public.horario_funcionamento OWNER TO barber_migrator;
ALTER TABLE public.bloqueio OWNER TO barber_migrator;
ALTER TABLE public.agendamentos OWNER TO barber_migrator;
ALTER TABLE public.comissoes OWNER TO barber_migrator;
ALTER TABLE public.notificacoes OWNER TO barber_migrator;
ALTER TABLE public.recuperacoes_senha OWNER TO barber_migrator;
ALTER TABLE public.historico_agendamentos OWNER TO barber_migrator;
ALTER TABLE public.flyway_schema_history OWNER TO barber_migrator;

-- Ownership changes above also transfer sequences owned by table columns.
-- Perform all table and sequence ACL operations as their new owner.
SET LOCAL ROLE barber_migrator;

REVOKE ALL PRIVILEGES ON TABLE
    public.administrador, public.barbearia, public.barbeiros, public.clientes,
    public.servicos, public.horario_funcionamento, public.bloqueio, public.agendamentos,
    public.comissoes, public.notificacoes, public.recuperacoes_senha,
    public.historico_agendamentos, public.flyway_schema_history
FROM PUBLIC, anon, authenticated, service_role, barber_runtime;

GRANT SELECT ON TABLE
    public.administrador, public.barbearia, public.barbeiros, public.clientes,
    public.servicos, public.horario_funcionamento, public.bloqueio, public.agendamentos,
    public.comissoes, public.notificacoes, public.recuperacoes_senha,
    public.historico_agendamentos
TO barber_runtime;

GRANT INSERT ON TABLE
    public.barbearia, public.barbeiros, public.clientes, public.servicos,
    public.horario_funcionamento, public.bloqueio, public.agendamentos,
    public.comissoes, public.notificacoes, public.recuperacoes_senha,
    public.historico_agendamentos
TO barber_runtime;

GRANT UPDATE ON TABLE
    public.administrador, public.barbearia, public.barbeiros, public.clientes,
    public.servicos, public.horario_funcionamento, public.bloqueio, public.agendamentos,
    public.comissoes, public.notificacoes, public.recuperacoes_senha
TO barber_runtime;

GRANT DELETE ON TABLE
    public.horario_funcionamento, public.bloqueio, public.comissoes,
    public.recuperacoes_senha
TO barber_runtime;

-- Bootstrap remains disabled; runtime receives no INSERT on administrador.
DO $$
DECLARE
    table_name text;
    sequence_name text;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
    ] LOOP
        sequence_name := pg_get_serial_sequence(
            format('%I.%I', 'public', table_name),
            'id'
        );

        EXECUTE format(
            'REVOKE ALL PRIVILEGES ON SEQUENCE %s FROM PUBLIC, anon, authenticated, service_role, barber_runtime',
            sequence_name::regclass
        );

        EXECUTE format(
            'GRANT USAGE ON SEQUENCE %s TO barber_runtime',
            sequence_name::regclass
        );
    END LOOP;
END
$$;

-- Check the effective barber_runtime table matrix before committing.
DO $$
DECLARE
    table_name text;
    privilege_name text;
    expected boolean;
    actual boolean;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos',
        'flyway_schema_history'
    ] LOOP
        FOREACH privilege_name IN ARRAY ARRAY[
            'SELECT', 'INSERT', 'UPDATE', 'DELETE',
            'TRUNCATE', 'REFERENCES', 'TRIGGER', 'MAINTAIN'
        ] LOOP
            expected := CASE privilege_name
                WHEN 'SELECT' THEN table_name <> 'flyway_schema_history'
                WHEN 'INSERT' THEN table_name = ANY (ARRAY[
                    'barbearia', 'barbeiros', 'clientes', 'servicos',
                    'horario_funcionamento', 'bloqueio', 'agendamentos',
                    'comissoes', 'notificacoes', 'recuperacoes_senha',
                    'historico_agendamentos'
                ])
                WHEN 'UPDATE' THEN table_name = ANY (ARRAY[
                    'administrador', 'barbearia', 'barbeiros', 'clientes',
                    'servicos', 'horario_funcionamento', 'bloqueio',
                    'agendamentos', 'comissoes', 'notificacoes',
                    'recuperacoes_senha'
                ])
                WHEN 'DELETE' THEN table_name = ANY (ARRAY[
                    'horario_funcionamento', 'bloqueio',
                    'comissoes', 'recuperacoes_senha'
                ])
                ELSE FALSE
            END;

            actual := has_table_privilege(
                'barber_runtime',
                format('%I.%I', 'public', table_name),
                privilege_name
            );

            IF actual IS DISTINCT FROM expected THEN
                RAISE EXCEPTION
                    'Unexpected effective barber_runtime privilege: table %, privilege %, expected %, got %',
                    table_name, privilege_name, expected, actual;
            END IF;
        END LOOP;
    END LOOP;
END
$$;

-- Ensure anon, authenticated and service_role have no effective DML on these tables.
DO $$
DECLARE
    table_name text;
    role_name text;
    privilege_name text;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos',
        'flyway_schema_history'
    ] LOOP
        FOREACH role_name IN ARRAY ARRAY[
            'anon', 'authenticated', 'service_role'
        ] LOOP
            FOREACH privilege_name IN ARRAY ARRAY[
                'SELECT', 'INSERT', 'UPDATE', 'DELETE',
                'TRUNCATE', 'REFERENCES', 'TRIGGER', 'MAINTAIN'
            ] LOOP
                IF has_table_privilege(
                    role_name,
                    format('%I.%I', 'public', table_name),
                    privilege_name
                ) THEN
                    RAISE EXCEPTION
                        'Unexpected effective grant: role %, table %, privilege %',
                        role_name, table_name, privilege_name;
                END IF;
            END LOOP;
        END LOOP;
    END LOOP;
END
$$;

-- Check the effective sequence matrix before committing.
DO $$
DECLARE
    table_name text;
    sequence_name text;
    role_name text;
    privilege_name text;
    expected boolean;
    actual boolean;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
    ] LOOP
        sequence_name := pg_get_serial_sequence(
            format('%I.%I', 'public', table_name),
            'id'
        );

        FOREACH role_name IN ARRAY ARRAY[
            'barber_runtime', 'anon', 'authenticated', 'service_role'
        ] LOOP
            FOREACH privilege_name IN ARRAY ARRAY['USAGE', 'SELECT', 'UPDATE'] LOOP
                expected :=
                    role_name = 'barber_runtime'
                    AND privilege_name = 'USAGE';

                actual := has_sequence_privilege(
                    role_name,
                    sequence_name::regclass,
                    privilege_name
                );

                IF actual IS DISTINCT FROM expected THEN
                    RAISE EXCEPTION
                        'Unexpected sequence privilege: table %, role %, privilege %, expected %, got %',
                        table_name, role_name, privilege_name, expected, actual;
                END IF;
            END LOOP;
        END LOOP;
    END LOOP;
END
$$;

-- Runtime must not retain effective CREATE on public, including via PUBLIC.
DO $$
BEGIN
    IF has_schema_privilege('barber_runtime', 'public', 'CREATE') THEN
        RAISE EXCEPTION 'barber_runtime still has effective CREATE on public';
    END IF;
END
$$;

-- Return to the original session role before revoking the temporary option.
RESET ROLE;

REVOKE SET OPTION FOR barber_migrator FROM postgres;

DO $$
BEGIN
    IF pg_has_role('postgres', 'barber_migrator', 'SET') THEN
        RAISE EXCEPTION 'postgres can still SET ROLE barber_migrator';
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
        RAISE EXCEPTION
            'postgres ADMIN OPTION on barber_migrator was not preserved';
    END IF;
END
$$;

COMMIT;
