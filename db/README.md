# `/db` — Database Scripts

This folder contains reference SQL scripts for the `alm` database.
These are **not run automatically** by the application — they are for manual use.

---

## Files

| File | Purpose | When to run |
|------|---------|-------------|
| `schema.sql` | Full phpMyAdmin dump of the `alm` database — includes all tables, views (`v_active_*`), indexes, foreign keys, and seed data (sample airport + lounge) | Run **once** on a fresh MySQL instance to set up the complete DB from scratch |
| `fix-admin-password.sql` | Sets the admin user password to `admin` (SHA-256 + Base64 encoded) | Run after initial setup if the admin login does not work |

---

## What the app uses automatically

`src/main/resources/schema-init.sql` is the file run by `SchemaInitializer` on every app startup. It uses `CREATE TABLE IF NOT EXISTS` so it is safe to run multiple times. It does **not** include seed data or views.

---

## Quick setup for a new environment

```sql
-- 1. Create the database (once only)
CREATE DATABASE IF NOT EXISTS alm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alm;

-- 2. Import the full schema + seed data
-- Run: db/schema.sql  (via phpMyAdmin or mysql CLI)

-- 3. Fix admin password if needed
-- Run: db/fix-admin-password.sql
```

Then configure your connection in `src/main/resources/db.properties`
(copy from `db.properties.example`).
