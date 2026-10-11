-- ============================================================================
-- Airport Lounge Manager — Database Alignment Verification Script
--
-- HOW TO RUN IN phpMyAdmin:
--   1. Select the 'alm' database in the left panel
--   2. Go to the SQL tab
--   3. Paste this entire script and click Go
--
-- DO NOT add "USE alm;" — phpMyAdmin already has the database selected.
-- A healthy database returns zero rows for checks 1–4 and 7.
-- ============================================================================


-- ── 1. TABLE EXISTENCE CHECK ─────────────────────────────────────────────────
-- Returns the names of any tables the app needs that are missing from your DB.
SELECT 'MISSING TABLE' AS check_type, required_table AS detail
FROM (
    SELECT 'users'               AS required_table UNION ALL
    SELECT 'passengers'                            UNION ALL
    SELECT 'lounges'                               UNION ALL
    SELECT 'lounge_amenities'                      UNION ALL
    SELECT 'airports'                              UNION ALL
    SELECT 'access_providers'                      UNION ALL
    SELECT 'access_requests'                       UNION ALL
    SELECT 'transactions'                          UNION ALL
    SELECT 'staff'                                 UNION ALL
    SELECT 'occupancy_logs'                        UNION ALL
    SELECT 'passenger_memberships'                 UNION ALL
    SELECT 'audit_logs'
) required
WHERE required_table NOT IN (
    SELECT TABLE_NAME
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
);

-- ── 2. COLUMN EXISTENCE CHECKS ───────────────────────────────────────────────
-- Returns any column the app queries that is absent from your DB.

SELECT 'MISSING COLUMN' AS check_type,
       CONCAT(required_table, '.', required_column) AS detail
FROM (
    -- users (read by UserDAO)
    SELECT 'users' AS required_table, 'id'         AS required_column UNION ALL
    SELECT 'users', 'username'                                         UNION ALL
    SELECT 'users', 'email'                                            UNION ALL
    SELECT 'users', 'password'                                         UNION ALL
    SELECT 'users', 'role'                                             UNION ALL
    SELECT 'users', 'active'                                           UNION ALL

    -- passengers (read by PassengerDAO)
    SELECT 'passengers', 'passenger_id'                                UNION ALL
    SELECT 'passengers', 'full_name'                                   UNION ALL
    SELECT 'passengers', 'passport_number'                             UNION ALL
    SELECT 'passengers', 'nationality'                                 UNION ALL
    SELECT 'passengers', 'phone'                                       UNION ALL
    SELECT 'passengers', 'email'                                       UNION ALL
    SELECT 'passengers', 'is_active'                                   UNION ALL

    -- lounges (read by LoungeDAO)
    SELECT 'lounges', 'lounge_id'                                      UNION ALL
    SELECT 'lounges', 'airport_id'                                     UNION ALL
    SELECT 'lounges', 'name'                                           UNION ALL
    SELECT 'lounges', 'terminal'                                       UNION ALL
    SELECT 'lounges', 'capacity'                                       UNION ALL
    SELECT 'lounges', 'status'                                         UNION ALL
    SELECT 'lounges', 'is_active'                                      UNION ALL

    -- lounge_amenities (read by LoungeAmenityDAO)
    SELECT 'lounge_amenities', 'amenity_id'                            UNION ALL
    SELECT 'lounge_amenities', 'lounge_id'                             UNION ALL
    SELECT 'lounge_amenities', 'item_name'                             UNION ALL
    SELECT 'lounge_amenities', 'total_count'                           UNION ALL
    SELECT 'lounge_amenities', 'available_count'                       UNION ALL

    -- access_providers (read by AccessProviderDAO)
    SELECT 'access_providers', 'provider_id'                           UNION ALL
    SELECT 'access_providers', 'provider_name'                         UNION ALL
    SELECT 'access_providers', 'provider_type'                         UNION ALL
    SELECT 'access_providers', 'is_active'                             UNION ALL

    -- access_requests (read by AccessRequestDAO — including JOIN aliases)
    SELECT 'access_requests', 'request_id'                             UNION ALL
    SELECT 'access_requests', 'passenger_id'                           UNION ALL
    SELECT 'access_requests', 'lounge_id'                              UNION ALL
    SELECT 'access_requests', 'provider_id'                            UNION ALL
    SELECT 'access_requests', 'status'                                 UNION ALL
    SELECT 'access_requests', 'request_time'                           UNION ALL
    SELECT 'access_requests', 'decision_time'                          UNION ALL
    SELECT 'access_requests', 'decision_reason'                        UNION ALL

    -- transactions (read by TransactionDAO)
    SELECT 'transactions', 'transaction_id'                            UNION ALL
    SELECT 'transactions', 'request_id'                                UNION ALL
    SELECT 'transactions', 'amount'                                    UNION ALL
    SELECT 'transactions', 'currency'                                  UNION ALL
    SELECT 'transactions', 'payment_method'                            UNION ALL
    SELECT 'transactions', 'payment_status'                            UNION ALL
    SELECT 'transactions', 'processed_at'
) required
WHERE NOT EXISTS (
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS c
    WHERE c.TABLE_SCHEMA = DATABASE()
      AND c.TABLE_NAME   = required.required_table
      AND c.COLUMN_NAME  = required.required_column
);

-- ── 3. ENUM VALUE CHECKS ─────────────────────────────────────────────────────
-- Returns any enum column whose set of allowed values doesn't match what the app uses.

SELECT 'WRONG ENUM VALUES' AS check_type,
       CONCAT(TABLE_NAME, '.', COLUMN_NAME) AS detail,
       COLUMN_TYPE AS current_values,
       expected AS expected_values
FROM (
    SELECT 'access_requests' AS tbl, 'status' AS col,
           "enum('PENDING','APPROVED','DENIED')" AS expected
    UNION ALL
    SELECT 'transactions', 'payment_method',
           "enum('CARD','UPI','CASH')"
    UNION ALL
    SELECT 'transactions', 'payment_status',
           "enum('INITIATED','SUCCESS','FAILED','REFUNDED')"
    UNION ALL
    SELECT 'access_providers', 'provider_type',
           "enum('AIRLINE','CREDIT_CARD','AGGREGATOR')"
    UNION ALL
    SELECT 'lounges', 'status',
           "enum('ACTIVE','CLOSED','MAINTENANCE')"
    UNION ALL
    SELECT 'staff', 'role',
           "enum('ADMIN','OPERATIONS_MANAGER','RECEPTION','SUPERVISOR','AUDITOR')"
    UNION ALL
    SELECT 'passenger_memberships', 'status',
           "enum('ACTIVE','EXPIRED','SUSPENDED')"
) expected_enums
JOIN INFORMATION_SCHEMA.COLUMNS c
  ON c.TABLE_SCHEMA = DATABASE()
 AND c.TABLE_NAME   = expected_enums.tbl
 AND c.COLUMN_NAME  = expected_enums.col
WHERE c.COLUMN_TYPE != expected_enums.expected;

-- ── 4. FOREIGN KEY CHECK ─────────────────────────────────────────────────────
-- Returns missing foreign key constraints that the app's JOIN queries depend on.

SELECT 'MISSING FK' AS check_type,
       CONCAT(required_table, ' → ', referenced_table, ' (', constraint_name, ')') AS detail
FROM (
    SELECT 'fk_lounge_airport'    AS constraint_name,
           'lounges'              AS required_table,
           'airports'             AS referenced_table  UNION ALL
    SELECT 'fk_request_passenger', 'access_requests', 'passengers'      UNION ALL
    SELECT 'fk_request_lounge',    'access_requests', 'lounges'         UNION ALL
    SELECT 'fk_request_provider',  'access_requests', 'access_providers' UNION ALL
    SELECT 'fk_transaction_request','transactions',   'access_requests'
) required_fks
WHERE constraint_name NOT IN (
    SELECT CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA     = DATABASE()
      AND CONSTRAINT_TYPE  = 'FOREIGN KEY'
);

-- ── 5. STAFF ROLE ENUM — AUDITOR CHECK ───────────────────────────────────────
-- Specific check: confirms AUDITOR was added to staff.role enum.

SELECT
    CASE
        WHEN COLUMN_TYPE LIKE "%'AUDITOR'%"
        THEN 'OK — AUDITOR present in staff.role enum'
        ELSE 'ACTION REQUIRED — Run: ALTER TABLE staff MODIFY COLUMN role enum(''ADMIN'',''OPERATIONS_MANAGER'',''RECEPTION'',''SUPERVISOR'',''AUDITOR'') NOT NULL'
    END AS auditor_check
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME   = 'staff'
  AND COLUMN_NAME  = 'role';

-- ── 6. USERS TABLE — DEFAULT ADMIN ACCOUNT ───────────────────────────────────
-- Confirms an ADMIN user exists. Table is fully qualified (alm.users)
-- so this works even when phpMyAdmin's batch context shifts after INFORMATION_SCHEMA queries.

SELECT
    CASE
        WHEN COUNT(*) > 0 THEN CONCAT('OK — ', COUNT(*), ' ADMIN user(s) found')
        ELSE 'WARNING — No ADMIN user exists. The app will have no admin account.'
    END AS admin_user_check
FROM alm.users
WHERE UPPER(role) = 'ADMIN' AND active = 1;


-- ── 7. REFERENTIAL INTEGRITY — ORPHAN CHECK ───────────────────────────────────
-- Returns any rows that would cause JOIN queries to produce NULL or fail.

SELECT 'ORPHAN: access_requests.passenger_id not in passengers' AS check_type, COUNT(*) AS orphan_count
FROM alm.access_requests ar
LEFT JOIN alm.passengers p ON ar.passenger_id = p.passenger_id
WHERE p.passenger_id IS NULL
UNION ALL
SELECT 'ORPHAN: access_requests.lounge_id not in lounges', COUNT(*)
FROM alm.access_requests ar
LEFT JOIN alm.lounges l ON ar.lounge_id = l.lounge_id
WHERE l.lounge_id IS NULL
UNION ALL
SELECT 'ORPHAN: transactions.request_id not in access_requests', COUNT(*)
FROM alm.transactions t
LEFT JOIN alm.access_requests ar ON t.request_id = ar.request_id
WHERE ar.request_id IS NULL;

-- ── 8. SUMMARY ────────────────────────────────────────────────────────────────
SELECT '✔  Verification complete. Review any rows returned above.' AS summary;
