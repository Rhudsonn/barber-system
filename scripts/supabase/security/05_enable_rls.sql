-- Run as postgres in the intended project after scripts 02 and 04 were validated.
-- The project is dedicated to one barbershop; these policies authorize the JDBC roles
-- and intentionally do not implement multi-tenancy or use auth.uid().
-- This script changes RLS/policies only. It does not change table grants, data,
-- ownership, migrations, or function ACLs.
-- Existing functions are outside this script's scope: script 04 controls defaults
-- for future functions created by barber_migrator, not existing function grants.
BEGIN;

-- Preconditions: correct executor, role-switch rights, objects/owners, no policies,
-- and the exact barber_runtime table privilege matrix previously installed by script 02.
DO $$
DECLARE
    table_name text;
    privilege_name text;
    expected boolean;
    actual boolean;
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

    IF EXISTS (
        SELECT 1
        FROM (VALUES ('barber_runtime'), ('barber_migrator')) expected_role(role_name)
        WHERE NOT EXISTS (SELECT 1 FROM pg_roles r WHERE r.rolname = expected_role.role_name)
    ) THEN
        RAISE EXCEPTION 'Required application role is missing';
    END IF;

    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos',
        'flyway_schema_history'
    ] LOOP
        IF to_regclass(format('%I.%I', 'public', table_name)) IS NULL THEN
            RAISE EXCEPTION 'Required table public.% is missing', table_name;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_class c
            JOIN pg_namespace n ON n.oid = c.relnamespace
            JOIN pg_roles owner_role ON owner_role.oid = c.relowner
            WHERE n.nspname = 'public'
              AND c.relname = table_name
              AND owner_role.rolname = 'barber_migrator'
        ) THEN
            RAISE EXCEPTION 'public.% is not owned by barber_migrator', table_name;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM pg_policy p
            JOIN pg_class c ON c.oid = p.polrelid
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE n.nspname = 'public'
              AND c.relname = table_name
        ) THEN
            RAISE EXCEPTION 'Unexpected existing policy on public.%', table_name;
        END IF;
    END LOOP;

    -- Assert the existing grant matrix; this script never issues GRANT/REVOKE on tables.
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
                    'Unexpected barber_runtime privilege on public.%: %, expected %, got %',
                    table_name, privilege_name, expected, actual;
            END IF;
        END LOOP;
    END LOOP;

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
            IF has_table_privilege('anon', format('%I.%I', 'public', table_name), privilege_name)
               OR has_table_privilege('authenticated', format('%I.%I', 'public', table_name), privilege_name)
               OR has_table_privilege('service_role', format('%I.%I', 'public', table_name), privilege_name) THEN
                RAISE EXCEPTION
                    'Unexpected API role table privilege on public.%: %',
                    table_name, privilege_name;
            END IF;
        END LOOP;
    END LOOP;

    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
    ] LOOP
        IF pg_get_serial_sequence(format('%I.%I', 'public', table_name), 'id') IS NULL THEN
            RAISE EXCEPTION 'Expected public.% identity sequence is missing', table_name;
        END IF;

        IF NOT has_sequence_privilege(
                'barber_runtime',
                pg_get_serial_sequence(format('%I.%I', 'public', table_name), 'id'),
                'USAGE'
           )
           OR has_sequence_privilege(
                'barber_runtime',
                pg_get_serial_sequence(format('%I.%I', 'public', table_name), 'id'),
                'SELECT'
           )
           OR has_sequence_privilege(
                'barber_runtime',
                pg_get_serial_sequence(format('%I.%I', 'public', table_name), 'id'),
                'UPDATE'
           ) THEN
            RAISE EXCEPTION 'Expected barber_runtime USAGE on public.% identity sequence', table_name;
        END IF;
    END LOOP;
END
$$;

-- Temporary SET only. Do not add INHERIT or ADMIN OPTION.
GRANT barber_migrator TO postgres WITH SET TRUE, INHERIT FALSE;
SET LOCAL ROLE barber_migrator;

DO $$
DECLARE
    table_name text;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
        'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
        'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
    ] LOOP
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', table_name);
        EXECUTE format('ALTER TABLE public.%I FORCE ROW LEVEL SECURITY', table_name);
        EXECUTE format(
            'CREATE POLICY zheiry_runtime_access ON public.%I FOR ALL TO barber_runtime USING (true) WITH CHECK (true)',
            table_name
        );
        EXECUTE format(
            'CREATE POLICY zheiry_migrator_access ON public.%I FOR ALL TO barber_migrator USING (true) WITH CHECK (true)',
            table_name
        );
    END LOOP;

    ALTER TABLE public.flyway_schema_history ENABLE ROW LEVEL SECURITY;
    ALTER TABLE public.flyway_schema_history FORCE ROW LEVEL SECURITY;
    CREATE POLICY zheiry_migrator_access ON public.flyway_schema_history
        FOR ALL TO barber_migrator USING (true) WITH CHECK (true);
END
$$;

-- Validate all 13 tables and the exact 25 planned policies before restoring postgres.
DO $$
DECLARE
    rls_table_count integer;
    total_policy_count integer;
    application_policy_count integer;
    history_policy_count integer;
BEGIN
    SELECT count(*) INTO rls_table_count
    FROM pg_class c
    JOIN pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_roles owner_role ON owner_role.oid = c.relowner
    WHERE n.nspname = 'public'
      AND c.relname = ANY (ARRAY[
          'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
          'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
          'notificacoes', 'recuperacoes_senha', 'historico_agendamentos',
          'flyway_schema_history'
      ])
      AND c.relrowsecurity
      AND c.relforcerowsecurity
      AND owner_role.rolname = 'barber_migrator';

    IF rls_table_count <> 13 THEN
        RAISE EXCEPTION 'Expected ENABLE and FORCE RLS on 13 owned tables; found %', rls_table_count;
    END IF;

    SELECT count(*) INTO total_policy_count
    FROM pg_policy p
    JOIN pg_class c ON c.oid = p.polrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public'
      AND c.relname = ANY (ARRAY[
          'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
          'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
          'notificacoes', 'recuperacoes_senha', 'historico_agendamentos',
          'flyway_schema_history'
      ]);

    SELECT count(*) INTO application_policy_count
    FROM pg_policy p
    JOIN pg_class c ON c.oid = p.polrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public'
      AND c.relname = ANY (ARRAY[
          'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
          'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
          'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
      ])
      AND p.polname IN ('zheiry_runtime_access', 'zheiry_migrator_access')
      AND p.polcmd = '*'
      AND p.polpermissive
      AND pg_get_expr(p.polqual, p.polrelid) = 'true'
      AND pg_get_expr(p.polwithcheck, p.polrelid) = 'true';

    SELECT count(*) INTO history_policy_count
    FROM pg_policy p
    JOIN pg_class c ON c.oid = p.polrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public'
      AND c.relname = 'flyway_schema_history'
      AND p.polname = 'zheiry_migrator_access'
      AND p.polcmd = '*'
      AND p.polpermissive
      AND pg_get_expr(p.polqual, p.polrelid) = 'true'
      AND pg_get_expr(p.polwithcheck, p.polrelid) = 'true';

    IF total_policy_count <> 25
       OR application_policy_count <> 24
       OR history_policy_count <> 1 THEN
        RAISE EXCEPTION
            'Expected exactly 25 policies (24 application, 1 Flyway); found total %, application %, history %',
            total_policy_count, application_policy_count, history_policy_count;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_policy p
        JOIN pg_class c ON c.oid = p.polrelid
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public'
          AND c.relname = ANY (ARRAY[
              'administrador', 'barbearia', 'barbeiros', 'clientes', 'servicos',
              'horario_funcionamento', 'bloqueio', 'agendamentos', 'comissoes',
              'notificacoes', 'recuperacoes_senha', 'historico_agendamentos'
          ])
          AND (
              (p.polname = 'zheiry_runtime_access' AND p.polroles <> ARRAY[
                  (SELECT oid FROM pg_roles WHERE rolname = 'barber_runtime')
              ]::oid[])
              OR (p.polname = 'zheiry_migrator_access' AND p.polroles <> ARRAY[
                  (SELECT oid FROM pg_roles WHERE rolname = 'barber_migrator')
              ]::oid[])
              OR p.polname NOT IN ('zheiry_runtime_access', 'zheiry_migrator_access')
          )
    ) THEN
        RAISE EXCEPTION 'An application policy has an unexpected name or role';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_policy p
        WHERE p.polrelid = 'public.flyway_schema_history'::regclass
          AND (p.polroles <> ARRAY[
              (SELECT oid FROM pg_roles WHERE rolname = 'barber_migrator')
          ]::oid[])
    ) THEN
        RAISE EXCEPTION 'Flyway policy has an unexpected role';
    END IF;
END
$$;

RESET ROLE;
REVOKE SET OPTION FOR barber_migrator FROM postgres;

-- Confirm postgres cannot SET the role again and its pre-existing ADMIN OPTION remains.
DO $$
BEGIN
    IF current_user <> 'postgres' OR session_user <> 'postgres' THEN
        RAISE EXCEPTION 'Expected restored postgres execution context';
    END IF;

    IF pg_has_role('postgres', 'barber_migrator', 'SET') THEN
        RAISE EXCEPTION 'Temporary SET OPTION remains on barber_migrator';
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
END
$$;

COMMIT;
