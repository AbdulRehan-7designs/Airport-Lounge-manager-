-- ============================================================================
-- Airport Lounge Manager — DB Alignment Fix Script
-- Generated: 2026-02-22
--
-- Run this in phpMyAdmin (alm database selected) to fix the 2 gaps found
-- by verify-schema.sql:
--   Fix 1: Create missing lounge_amenities table
--   Fix 2: Add AUDITOR to staff.role enum
-- ============================================================================

-- ── Fix 1: Create lounge_amenities ───────────────────────────────────────────
-- This table was missing from your DB. The app's LoungeAmenityDAO and
-- LoungesServlet (add-amenity action) require it.

CREATE TABLE IF NOT EXISTS alm.lounge_amenities (
  amenity_id      int(11)      NOT NULL AUTO_INCREMENT,
  lounge_id       int(11)      NOT NULL,
  item_name       varchar(100) NOT NULL,
  total_count     int(11)      NOT NULL DEFAULT 0,
  available_count int(11)      NOT NULL DEFAULT 0,
  updated_at      timestamp    NOT NULL DEFAULT current_timestamp()
                               ON UPDATE current_timestamp(),
  PRIMARY KEY (amenity_id),
  KEY idx_amenity_lounge (lounge_id),
  CONSTRAINT fk_amenity_lounge
    FOREIGN KEY (lounge_id) REFERENCES alm.lounges (lounge_id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Fix 2: Add AUDITOR to staff.role enum ────────────────────────────────────
-- The app's new RBAC model adds a Data & Financial Auditor role.
-- This ALTER adds AUDITOR without changing existing rows.

ALTER TABLE alm.staff
  MODIFY COLUMN role
    enum('ADMIN','OPERATIONS_MANAGER','RECEPTION','SUPERVISOR','AUDITOR')
    NOT NULL;

-- ── Verify ───────────────────────────────────────────────────────────────────
-- Run these two SELECTs to confirm. Both should return OK rows.

SELECT 'lounge_amenities' AS tbl,
       CASE WHEN COUNT(*) > 0 THEN 'OK — table exists'
            ELSE 'STILL MISSING — check for errors above'
       END AS status
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'alm' AND TABLE_NAME = 'lounge_amenities';

SELECT 'staff.role' AS col,
       CASE WHEN COLUMN_TYPE LIKE "%'AUDITOR'%"
            THEN 'OK — AUDITOR present in enum'
            ELSE 'STILL MISSING — check for errors above'
       END AS status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'alm'
  AND TABLE_NAME   = 'staff'
  AND COLUMN_NAME  = 'role';
