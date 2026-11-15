-- One-time role creation. Set each password separately through the approved secret workflow.
-- Do not put PASSWORD literals in this file or in the API environment.
BEGIN;

CREATE ROLE barber_runtime LOGIN NOINHERIT
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;

CREATE ROLE barber_migrator LOGIN NOINHERIT
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;

COMMENT ON ROLE barber_runtime IS 'Zheiry API runtime: application DML only; no ownership or DDL.';
COMMENT ON ROLE barber_migrator IS 'Zheiry migration job only: owns application objects; never used by the API.';

COMMIT;
