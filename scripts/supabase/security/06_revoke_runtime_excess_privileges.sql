-- Remove only the two privileges not required by the API runtime role.
-- Execute as barber_migrator (the table owner) or an authorized project administrator.
-- Review the read-only inventory first; this script does not alter ownership or other grants.
BEGIN;

REVOKE INSERT ON TABLE public.administrador FROM barber_runtime;
REVOKE UPDATE ON TABLE public.historico_agendamentos FROM barber_runtime;

COMMIT;
