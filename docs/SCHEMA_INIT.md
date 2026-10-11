# Schema initialization (directly in DB)

The application **applies the SQL schema to the database automatically** when it starts, so the schema is created or updated in the DB whenever needed.

## How it works

1. **On startup** – A `ServletContextListener` (`com.alm.init.SchemaInitializer`) runs when the app deploys.
2. **Database** – If the connection fails with "Unknown database", the initializer tries to create the database `alm` (using the same DB URL/user/password config, connecting without a database name).
3. **Tables** – It runs the DDL from the classpath resource `schema-init.sql`, which uses **CREATE TABLE IF NOT EXISTS** for all tables. This is **idempotent**: safe to run on every startup; existing tables are left unchanged.

## Files

| File | Purpose |
|------|--------|
| `src/main/resources/schema-init.sql` | DDL for all tables (CREATE TABLE IF NOT EXISTS). No views, no foreign-key ALTERs. |
| `src/main/java/com/alm/init/SchemaInitializer.java` | Listener that runs the DDL on `contextInitialized`. |

## Requirements

- **Database server** must be running and reachable (e.g. MySQL/MariaDB).
- **Database `alm`** – Either create it manually once (`CREATE DATABASE IF NOT EXISTS alm;`) or let the initializer create it when the configured URL points to `alm` and the first connection fails with "Unknown database".
- **Config** – Same as rest of app: `db.properties` or env/system properties (`DB_URL`, `DB_USER`, `DB_PASSWORD`).

## Manual schema (optional)

For a full schema including foreign keys, views, and seed data, run the static file **`schema.sql`** once (e.g. in phpMyAdmin or MySQL client). The startup initializer only ensures **tables exist**; it does not replace a one-time full schema run.

## Disabling

- To skip schema init (e.g. in tests): do not load `schema-init.sql` on the classpath, or the listener will no-op when the resource is missing.
- The listener is registered via `@WebListener` and in `web.xml`; remove one or both to disable.
