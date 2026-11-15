-- Read-only preflight. PostgreSQL defaults cannot be narrowed to named application
-- objects; changing defaults for postgres in public could affect Supabase-managed objects.
-- Application DDL must use barber_migrator, whose defaults are restricted in step 04.
SELECT owner.rolname AS creator,
       n.nspname AS schema_name,
       d.defaclobjtype AS object_type,
       d.defaclacl AS acl
FROM pg_default_acl d
JOIN pg_roles owner ON owner.oid = d.defaclrole
LEFT JOIN pg_namespace n ON n.oid = d.defaclnamespace
WHERE owner.rolname IN ('postgres', 'barber_migrator')
  AND (d.defaclnamespace = 0 OR n.nspname = 'public')
ORDER BY creator, schema_name, object_type;
