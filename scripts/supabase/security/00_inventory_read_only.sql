-- Somente leitura. Executar sob autorização no projeto alvo, nunca contra staging por engano.
SELECT current_database() AS database_name, current_user, session_user;

SELECT ssl, version
FROM pg_stat_ssl
WHERE pid = pg_backend_pid();

SELECT rolname, rolcanlogin, rolsuper, rolinherit, rolcreatedb,
       rolcreaterole, rolreplication, rolbypassrls
FROM pg_roles
WHERE rolname IN ('anon', 'authenticated', 'service_role', 'barber_runtime', 'barber_migrator')
ORDER BY rolname;

SELECT member.rolname AS member, granted.rolname AS granted_role, m.admin_option
FROM pg_auth_members m
JOIN pg_roles member ON member.oid = m.member
JOIN pg_roles granted ON granted.oid = m.roleid
WHERE member.rolname IN ('barber_runtime', 'barber_migrator')
   OR granted.rolname IN ('barber_runtime', 'barber_migrator')
ORDER BY member.rolname, granted.rolname;

SELECT c.relname AS object_name, pg_get_userbyid(c.relowner) AS object_owner,
       c.relkind, c.relrowsecurity AS rls_enabled, c.relforcerowsecurity AS force_rls
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE n.nspname = 'public'
  AND c.relkind IN ('r', 'p', 'v', 'm', 'S', 'f')
ORDER BY c.relkind, c.relname;

SELECT table_name, grantee, privilege_type
FROM information_schema.table_privileges
WHERE table_schema = 'public'
ORDER BY table_name, grantee, privilege_type;

SELECT object_name AS sequence_name, grantee, privilege_type
FROM information_schema.usage_privileges
WHERE object_schema = 'public'
ORDER BY object_name, grantee, privilege_type;

SELECT schemaname, tablename, policyname, roles, cmd, qual, with_check
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, policyname;

SELECT owner.rolname AS creator, n.nspname AS schema_name,
       d.defaclobjtype AS object_type, d.defaclacl AS acl
FROM pg_default_acl d
JOIN pg_roles owner ON owner.oid = d.defaclrole
LEFT JOIN pg_namespace n ON n.oid = d.defaclnamespace
ORDER BY creator, schema_name, object_type;

SELECT routine_name, grantee, privilege_type
FROM information_schema.routine_privileges
WHERE specific_schema = 'public'
ORDER BY routine_name, grantee, privilege_type;

SELECT installed_rank, version, description, success
FROM public.flyway_schema_history
ORDER BY installed_rank;

-- Schema-level ACLs are reported separately; do not revoke them as a substitute for
-- object-scoped grants on the application tables above.
SELECT n.nspname AS schema_name,
       CASE WHEN acl.grantee = 0 THEN 'PUBLIC' ELSE grantee.rolname END AS grantee,
       acl.privilege_type, acl.is_grantable
FROM pg_namespace n
CROSS JOIN LATERAL aclexplode(COALESCE(n.nspacl, acldefault('n', n.nspowner))) acl
LEFT JOIN pg_roles grantee ON grantee.oid = acl.grantee
WHERE n.nspname = 'public'
ORDER BY grantee, acl.privilege_type;
