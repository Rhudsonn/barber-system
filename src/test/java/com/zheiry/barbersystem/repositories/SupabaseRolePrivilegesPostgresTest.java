package com.zheiry.barbersystem.repositories;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupabaseRolePrivilegesPostgresTest {
    private static final String SECURITY_DIR = "scripts/supabase/security/";
    private static final List<String> APP_TABLES = List.of(
            "administrador", "barbearia", "barbeiros", "clientes", "servicos",
            "horario_funcionamento", "bloqueio", "agendamentos", "comissoes",
            "notificacoes", "recuperacoes_senha", "historico_agendamentos");
    private static final List<String> ALL_TABLES = new ArrayList<>(APP_TABLES);

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("zheiry_security_test");

    @BeforeAll
    static void provisionIsolatedPostgres() throws Exception {
        ALL_TABLES.add("flyway_schema_history");
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .defaultSchema("public")
                .schemas("public")
                .load()
                .migrate();

        execute("CREATE ROLE postgres LOGIN CREATEROLE NOSUPERUSER NOCREATEDB NOBYPASSRLS; " +
                "CREATE ROLE anon NOLOGIN; CREATE ROLE authenticated NOLOGIN; " +
                "CREATE ROLE service_role NOLOGIN BYPASSRLS;");
        execute(readScript("01_create_roles.sql"));
        execute("ALTER DATABASE zheiry_security_test OWNER TO postgres; " +
                "GRANT barber_migrator TO postgres WITH ADMIN TRUE, INHERIT FALSE, SET FALSE;");
        execute(transferOwnershipParaPostgres());
        execute("CREATE SCHEMA auth; " +
                "CREATE TABLE public.unrelated_public_probe (id integer); " +
                "CREATE TABLE auth.internal_probe (id integer); " +
                "GRANT USAGE ON SCHEMA auth TO PUBLIC, anon, authenticated, service_role; " +
                "GRANT SELECT, INSERT, UPDATE, DELETE ON public.unrelated_public_probe, auth.internal_probe " +
                "TO PUBLIC, anon, authenticated, service_role;");
        executeAsPostgres(readScript("02_ownership_and_grants.sql"));
        execute("GRANT INSERT ON TABLE public.administrador TO barber_runtime; " +
                "GRANT UPDATE ON TABLE public.historico_agendamentos TO barber_runtime;");
        execute(readScript("06_revoke_runtime_excess_privileges.sql"));
        execute(readScript("03_default_privileges.sql"));
        executeAsPostgres(readScript("04_default_privileges_migrator.sql"));
        executeAsPostgres(readScript("05_enable_rls.sql"));
        execute(readScript("00_inventory_read_only.sql"));
        execute("SET ROLE barber_migrator; " +
                "CREATE TABLE public.default_acl_probe (id integer); " +
                "CREATE SEQUENCE public.default_acl_probe_sequence; " +
                "CREATE FUNCTION public.default_acl_probe_fn() RETURNS integer " +
                "LANGUAGE sql IMMUTABLE AS 'SELECT 1'; " +
                "ALTER TABLE public.default_acl_probe ADD COLUMN migration_probe integer; " +
                "RESET ROLE;");
    }

    @Test
    void runtimeReceivesOnlyAuditedTableOperations() throws Exception {
        Map<String, List<String>> expected = new LinkedHashMap<>();
        expected.put("administrador", List.of("SELECT", "UPDATE"));
        expected.put("barbearia", List.of("SELECT", "INSERT", "UPDATE"));
        expected.put("barbeiros", List.of("SELECT", "INSERT", "UPDATE"));
        expected.put("clientes", List.of("SELECT", "INSERT", "UPDATE"));
        expected.put("servicos", List.of("SELECT", "INSERT", "UPDATE"));
        expected.put("horario_funcionamento", List.of("SELECT", "INSERT", "UPDATE", "DELETE"));
        expected.put("bloqueio", List.of("SELECT", "INSERT", "UPDATE", "DELETE"));
        expected.put("agendamentos", List.of("SELECT", "INSERT", "UPDATE"));
        expected.put("comissoes", List.of("SELECT", "INSERT", "UPDATE", "DELETE"));
        expected.put("notificacoes", List.of("SELECT", "INSERT", "UPDATE"));
        expected.put("recuperacoes_senha", List.of("SELECT", "INSERT", "UPDATE", "DELETE"));
        expected.put("historico_agendamentos", List.of("SELECT", "INSERT"));

        for (String table : APP_TABLES) {
            for (String privilege : List.of("SELECT", "INSERT", "UPDATE", "DELETE", "TRUNCATE", "REFERENCES", "TRIGGER")) {
                boolean actual = hasTablePrivilege("barber_runtime", table, privilege);
                assertEquals(expected.get(table).contains(privilege), actual,
                        () -> "Unexpected " + privilege + " privilege on " + table);
            }
        }
    }

    @Test
    void publicApiRolesHaveNoPrivilegesOnApplicationOrFlywayTables() throws Exception {
        for (String role : List.of("anon", "authenticated", "service_role")) {
            for (String table : ALL_TABLES) {
                for (String privilege : List.of("SELECT", "INSERT", "UPDATE", "DELETE", "TRUNCATE")) {
                    assertFalse(hasTablePrivilege(role, table, privilege),
                            () -> role + " still has " + privilege + " on " + table);
                }
            }
        }
    }

    @Test
    void grantsOnUnrelatedAndSupabaseInternalObjectsRemainUntouched() throws Exception {
        for (String role : List.of("anon", "authenticated", "service_role")) {
            for (String privilege : List.of("SELECT", "INSERT", "UPDATE", "DELETE")) {
                assertTrue(booleanValue("SELECT has_table_privilege('" + role +
                        "', 'public.unrelated_public_probe', '" + privilege + "')"));
                assertTrue(booleanValue("SELECT has_table_privilege('" + role +
                        "', 'auth.internal_probe', '" + privilege + "')"));
            }
        }
        assertFalse(booleanValue("SELECT relrowsecurity FROM pg_class WHERE oid='auth.internal_probe'::regclass"));
        assertFalse(booleanValue("SELECT relrowsecurity FROM pg_class WHERE oid='public.unrelated_public_probe'::regclass"));
    }

    @Test
    void schemaCreateIsLimitedAndRuntimeCannotReadFlywayHistory() throws Exception {
        assertTrue(booleanValue("SELECT has_schema_privilege('barber_migrator', 'public', 'CREATE')"));
        assertTrue(booleanValue("SELECT has_schema_privilege('barber_runtime', 'public', 'USAGE')"));
        for (String role : List.of("anon", "authenticated", "service_role")) {
            assertFalse(booleanValue("SELECT has_schema_privilege('" + role + "', 'public', 'CREATE')"));
        }
        for (String privilege : List.of("SELECT", "INSERT", "UPDATE", "DELETE")) {
            assertFalse(hasTablePrivilege("barber_runtime", "flyway_schema_history", privilege));
        }
    }

    @Test
    void runtimeAndMigratorAreNotOwnersOrMembersOfPrivilegedRoles() throws Exception {
        assertEquals("barber_migrator", scalar("SELECT tableowner FROM pg_tables " +
                "WHERE schemaname='public' AND tablename='servicos'"));
        assertEquals("barber_migrator", scalar("SELECT tableowner FROM pg_tables " +
                "WHERE schemaname='public' AND tablename='flyway_schema_history'"));
        assertFalse(booleanValue("SELECT rolsuper OR rolcreatedb OR rolcreaterole OR rolbypassrls " +
                "FROM pg_roles WHERE rolname='barber_runtime'"));
        assertFalse(booleanValue("SELECT pg_has_role('barber_runtime', 'barber_migrator', 'MEMBER')"));
    }

    @Test
    void allApplicationTablesAndFlywayHistoryHaveForcedRls() throws Exception {
        assertEquals(13, integer("SELECT count(*) FROM pg_class c " +
                "JOIN pg_namespace n ON n.oid=c.relnamespace " +
                "WHERE n.nspname='public' AND c.relkind='r' " +
                "AND c.relname IN ('administrador','barbearia','barbeiros','clientes','servicos'," +
                "'horario_funcionamento','bloqueio','agendamentos','comissoes','notificacoes'," +
                "'recuperacoes_senha','historico_agendamentos','flyway_schema_history') " +
                "AND c.relrowsecurity AND c.relforcerowsecurity"));
    }

    @Test
    void runtimeCanQueryProjectDataThroughItsRlsPolicy() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("SET ROLE barber_runtime");
            try (ResultSet result = statement.executeQuery("SELECT row_security_active('public.servicos'::regclass), " +
                    "(SELECT count(*) FROM public.servicos)")) {
                result.next();
                assertTrue(result.getBoolean(1));
                assertTrue(result.getLong(2) >= 0);
            } finally {
                statement.execute("RESET ROLE");
            }
        }
    }

    @Test
    void runtimeCanLockRecoveryTokenRowsWithoutChangingThem() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("SET ROLE barber_runtime");
            try (ResultSet result = statement.executeQuery("SELECT id FROM public.recuperacoes_senha FOR UPDATE")) {
                assertFalse(result.next());
            } finally {
                statement.execute("RESET ROLE");
            }
        }
    }

    @Test
    void migratorCanAccessFlywayHistoryWithForcedRls() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("SET ROLE barber_migrator");
            try (ResultSet result = statement.executeQuery("SELECT row_security_active('public.flyway_schema_history'::regclass), " +
                    "(SELECT count(*) FROM public.flyway_schema_history)")) {
                result.next();
                assertTrue(result.getBoolean(1));
                assertTrue(result.getLong(2) >= 7);
            } finally {
                statement.execute("RESET ROLE");
            }
        }
    }

    @Test
    void defaultPrivilegesDenyFutureObjectsUntilExplicitlyGranted() throws Exception {
        for (String role : List.of("anon", "authenticated", "service_role", "barber_runtime")) {
            assertFalse(hasTablePrivilege(role, "default_acl_probe", "SELECT"));
            assertFalse(hasSequencePrivilege(role, "default_acl_probe_sequence", "USAGE"));
            assertFalse(hasFunctionPrivilege(role, "public.default_acl_probe_fn()", "EXECUTE"));
        }
    }

    @Test
    void runtimeHasOnlyUsageOnApplicationIdentitySequences() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT pg_get_serial_sequence(" +
                     "format('%I.%I','public', tablename), 'id') " +
                     "FROM pg_tables WHERE schemaname='public' AND tablename = ANY (" +
                     "ARRAY['administrador','barbearia','barbeiros','clientes','servicos'," +
                     "'horario_funcionamento','bloqueio','agendamentos','comissoes','notificacoes'," +
                     "'recuperacoes_senha','historico_agendamentos'])")) {
            int count = 0;
            while (result.next()) {
                String sequence = result.getString(1);
                if (sequence == null) continue;
                count++;
                assertTrue(hasSequencePrivilege("barber_runtime", sequence, "USAGE"));
                assertFalse(hasSequencePrivilege("barber_runtime", sequence, "SELECT"));
                assertFalse(hasSequencePrivilege("barber_runtime", sequence, "UPDATE"));
            }
            assertEquals(12, count);
        }
    }

    private boolean hasTablePrivilege(String role, String table, String privilege) throws Exception {
        return booleanValue("SELECT has_table_privilege('" + role + "', 'public." + table + "', '" + privilege + "')");
    }

    private boolean hasSequencePrivilege(String role, String sequence, String privilege) throws Exception {
        return booleanValue("SELECT has_sequence_privilege('" + role + "', '" + sequence + "', '" + privilege + "')");
    }

    private boolean hasFunctionPrivilege(String role, String function, String privilege) throws Exception {
        return booleanValue("SELECT has_function_privilege('" + role + "', '" + function + "', '" + privilege + "')");
    }

    private String scalar(String sql) throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getString(1);
        }
    }

    private int integer(String sql) throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }

    private boolean booleanValue(String sql) throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getBoolean(1);
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private static void execute(String sql) throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void executeAsPostgres(String sql) throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("SET SESSION AUTHORIZATION postgres");
            statement.execute(sql);
        }
    }

    private static String transferOwnershipParaPostgres() {
        return "DO $$ DECLARE table_name text; sequence_name text; BEGIN " +
                "FOREACH table_name IN ARRAY ARRAY['administrador','barbearia','barbeiros','clientes','servicos'," +
                "'horario_funcionamento','bloqueio','agendamentos','comissoes','notificacoes','recuperacoes_senha'," +
                "'historico_agendamentos'] LOOP " +
                "EXECUTE format('ALTER TABLE public.%I OWNER TO postgres', table_name); " +
                "sequence_name := pg_get_serial_sequence(format('%I.%I','public',table_name),'id'); " +
                "EXECUTE format('ALTER SEQUENCE %s OWNER TO postgres', sequence_name::regclass); " +
                "END LOOP; ALTER TABLE public.flyway_schema_history OWNER TO postgres; END $$;";
    }

    private static String readScript(String filename) throws IOException {
        return Files.readString(Path.of(SECURITY_DIR + filename));
    }
}
