-- ============================================================================
-- SEED DATA — Hyderabad / Telangana (Real data)
-- Airport: Rajiv Gandhi International Airport (HYD), Shamshabad
--
-- IMPORTANT:
--   - Does NOT touch the 'users' table (your logins are preserved)
--   - system_settings is auto-created by SettingDAO (no need to seed)
--   - notifications references users.id — seeded with safe user_id = 1
--
-- Run: Import this file in phpMyAdmin → SQL tab → Go
-- ============================================================================

SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';
SET NAMES utf8mb4;

-- Disable FK checks for the entire script
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Auto-patch passengers schema for Live Status Updates
ALTER TABLE passengers 
  ADD COLUMN IF NOT EXISTS membership_type varchar(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS ticket_class varchar(100) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS flight_time datetime DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS access_status enum('ELIGIBLE', 'NOT_ELIGIBLE', 'CHECKED_IN') DEFAULT 'NOT_ELIGIBLE',
  ADD COLUMN IF NOT EXISTS checked_in_lounge_id int(11) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS assigned_seat varchar(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS check_in_time datetime DEFAULT NULL;

-- 2. CREATE NEW TABLES (If not exist)

CREATE TABLE IF NOT EXISTS lounge_policies (
  policy_id int(11) NOT NULL AUTO_INCREMENT,
  lounge_id int(11) NOT NULL,
  entry_fee decimal(10,2) NOT NULL DEFAULT 40.00,
  guest_fee decimal(10,2) NOT NULL DEFAULT 20.00,
  free_visit_limit int(11) NOT NULL DEFAULT 4,
  overstay_rate decimal(10,2) NOT NULL DEFAULT 15.00,
  PRIMARY KEY (policy_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visits (
  visit_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) NOT NULL,
  lounge_id int(11) NOT NULL,
  access_type ENUM('COMPLIMENTARY_AIRLINE', 'COMPLIMENTARY_MEMBERSHIP', 'PAID', 'DENIED') NOT NULL,
  check_in_time datetime DEFAULT CURRENT_TIMESTAMP,
  check_out_time datetime DEFAULT NULL,
  payment_status ENUM('PENDING', 'PAID', 'NOT_REQUIRED') DEFAULT 'NOT_REQUIRED',
  PRIMARY KEY (visit_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS billing (
  bill_id int(11) NOT NULL AUTO_INCREMENT,
  visit_id int(11) NOT NULL,
  charge_type varchar(50) NOT NULL,
  amount decimal(10,2) NOT NULL,
  PRIMARY KEY (bill_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lounge_services (
  service_id int(11) NOT NULL AUTO_INCREMENT,
  lounge_id int(11) NOT NULL,
  service_name varchar(100) NOT NULL,
  category varchar(50) DEFAULT 'General',
  price decimal(10,2) NOT NULL DEFAULT 0.00,
  description text DEFAULT NULL,
  max_slots int(11) DEFAULT NULL,
  is_available tinyint(1) DEFAULT 1,
  PRIMARY KEY (service_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visit_services (
  vs_id int(11) NOT NULL AUTO_INCREMENT,
  visit_id int(11) NOT NULL,
  service_id int(11) NOT NULL,
  passenger_id int(11) NOT NULL,
  quantity int(11) DEFAULT 1,
  total_price decimal(10,2) DEFAULT 0.00,
  status enum('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
  requested_at timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (vs_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id) ON DELETE CASCADE,
  FOREIGN KEY (service_id) REFERENCES lounge_services(service_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS complaints (
  complaint_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) DEFAULT NULL,
  lounge_id int(11) NOT NULL,
  visit_id int(11) DEFAULT NULL,
  category varchar(50) NOT NULL,
  description text NOT NULL,
  severity enum('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
  status enum('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
  reported_by varchar(100) DEFAULT 'Staff',
  reported_at timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (complaint_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS waitlist (
  waitlist_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) NOT NULL,
  lounge_id int(11) NOT NULL,
  priority enum('NORMAL', 'HIGH', 'VIP') DEFAULT 'NORMAL',
  status enum('WAITING', 'NOTIFIED', 'ADMITTED', 'EXPIRED', 'CANCELLED') DEFAULT 'WAITING',
  requested_at timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (waitlist_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visit_guests (
  guest_id int(11) NOT NULL AUTO_INCREMENT,
  visit_id int(11) NOT NULL,
  guest_name varchar(255) NOT NULL,
  guest_type ENUM('ADULT', 'CHILD', 'INFANT') DEFAULT 'ADULT',
  PRIMARY KEY (guest_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session_tokens (
  token_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) NOT NULL,
  token_string varchar(64) NOT NULL,
  generated_at datetime DEFAULT current_timestamp(),
  expiry_time datetime NOT NULL,
  is_used tinyint(1) DEFAULT 0,
  PRIMARY KEY (token_id),
  UNIQUE KEY token_string (token_string)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. CLEAN DATA
DELETE FROM session_tokens;
DELETE FROM notifications;
DELETE FROM audit_logs;
DELETE FROM occupancy_logs;
DELETE FROM transactions;
DELETE FROM access_requests;
DELETE FROM passenger_memberships;
DELETE FROM lounge_amenities;
DELETE FROM access_providers;
DELETE FROM staff;
DELETE FROM passengers;
DELETE FROM lounges;
DELETE FROM airports;
DELETE FROM visit_services;
DELETE FROM visits;
DELETE FROM lounge_services;
DELETE FROM complaints;
DELETE FROM waitlist;

ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE audit_logs AUTO_INCREMENT = 1;
ALTER TABLE occupancy_logs AUTO_INCREMENT = 1;
ALTER TABLE transactions AUTO_INCREMENT = 1;
ALTER TABLE access_requests AUTO_INCREMENT = 1;
ALTER TABLE passenger_memberships AUTO_INCREMENT = 1;
ALTER TABLE lounge_amenities AUTO_INCREMENT = 1;
ALTER TABLE access_providers AUTO_INCREMENT = 1;
ALTER TABLE staff AUTO_INCREMENT = 1;
ALTER TABLE passengers AUTO_INCREMENT = 1;
ALTER TABLE lounges AUTO_INCREMENT = 1;
ALTER TABLE airports AUTO_INCREMENT = 1;
ALTER TABLE visits AUTO_INCREMENT = 1;
ALTER TABLE lounge_services AUTO_INCREMENT = 1;
ALTER TABLE visit_services AUTO_INCREMENT = 1;

-- 4. INSERT DATA

-- Airports
INSERT INTO airports (airport_id, name, iata_code, city, country) VALUES
  (1, 'Rajiv Gandhi International Airport',                 'HYD', 'Hyderabad',  'India'),
  (2, 'Indira Gandhi International Airport',                'DEL', 'New Delhi',  'India'),
  (3, 'Chhatrapati Shivaji Maharaj International Airport',  'BOM', 'Mumbai',     'India');

-- Lounges
INSERT INTO lounges (lounge_id, airport_id, name, terminal, capacity, status) VALUES
  (1, 1, 'Plaza Premium Lounge',       'Domestic',         60, 'ACTIVE'),
  (2, 1, 'ITC Green Lounge',           'Domestic',         40, 'ACTIVE'),
  (3, 1, 'Above Ground Level Lounge',  'International',    50, 'ACTIVE'),
  (4, 1, 'Encalm Prive Lounge',        'International',    35, 'ACTIVE');

-- Staff
INSERT INTO staff (staff_id, airport_id, full_name, email, role, password_hash) VALUES
  (1, 1, 'Mohammed Irfan',  'admin@rgialounge.in',      'ADMIN',              '75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8='),
  (2, 1, 'Sravani Devi',    'ops@rgialounge.in',        'OPERATIONS_MANAGER', '75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8='),
  (3, 1, 'Venkat Reddy',    'reception1@rgialounge.in', 'RECEPTION',          '75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8='),
  (6, 1, 'Lakshmi Prasad',  'auditor@rgialounge.in',    'AUDITOR',            '75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=');

-- Passengers
INSERT INTO passengers (passenger_id, full_name, passport_number, nationality, phone, email, ticket_class, membership_type, access_status, assigned_seat) VALUES
  (1,  'Viva_FirstClass_Flyer',  'T1234567', 'Indian', '+91-9876543210', 'test1@gmail.com', 'First Class', NULL, 'ELIGIBLE', 'A01'),
  (2,  'Viva_Premium_Member',    'T2345678', 'Indian', '+91-9988776655', 'test2@gmail.com', 'Economy', 'Amex Platinum', 'ELIGIBLE', 'B12'),
  (3,  'Ananya Sharma',          'T6789012', 'Indian', '+91-8811223344', 'ananya.s@gmail.com', 'Economy', NULL, 'ELIGIBLE', 'C05');

-- Lounge Services
INSERT INTO lounge_services (lounge_id, service_name, category, price) VALUES
(1, 'Premium Shower', 'Wellness', 15.00),
(1, 'Nap Pod (1hr)', 'Rest', 25.00),
(1, 'Meeting Room', 'Business', 40.00),
(2, 'Luxury Shower', 'Wellness', 18.00);

-- Active Visits
INSERT INTO visits (visit_id, passenger_id, lounge_id, access_type, check_in_time) VALUES
(1, 1, 1, 'COMPLIMENTARY_AIRLINE',    DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 2, 1, 'COMPLIMENTARY_MEMBERSHIP', DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- Service Queue Tasks
INSERT INTO visit_services (visit_id, service_id, passenger_id, status, requested_at) VALUES
(1, 1, 1, 'PENDING',     DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(2, 2, 2, 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 20 MINUTE));

-- Complaints
INSERT INTO complaints (lounge_id, category, description, severity) VALUES
(1, 'Facility', 'HVAC temperature too high in Section A', 'MEDIUM'),
(1, 'Service', 'Long queue for buffet upgrade', 'LOW');

-- System Settings
INSERT IGNORE INTO system_settings (setting_key, setting_value, description) VALUES
  ('BUSY_THRESHOLD_PCT',  '90',    'Capacity percentage after which a lounge is marked as BUSY.'),
  ('AUTO_DENY_EXPIRED',   'true',  'Automatically deny requests if membership is expired.');

-- Re-enable FK checks
SET FOREIGN_KEY_CHECKS = 1;
