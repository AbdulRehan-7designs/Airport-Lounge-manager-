SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';
SET NAMES utf8mb4;

-- Tables (dependency order)
CREATE TABLE IF NOT EXISTS airports (
  airport_id int(11) NOT NULL AUTO_INCREMENT,
  name varchar(150) NOT NULL,
  iata_code char(3) NOT NULL,
  city varchar(100) NOT NULL,
  country varchar(100) NOT NULL,
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  is_active tinyint(1) DEFAULT 1,
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (airport_id),
  UNIQUE KEY iata_code (iata_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lounges (
  lounge_id int(11) NOT NULL AUTO_INCREMENT,
  airport_id int(11) NOT NULL,
  name varchar(150) NOT NULL,
  terminal varchar(50) DEFAULT NULL,
  capacity int(11) NOT NULL,
  status enum('ACTIVE','CLOSED','MAINTENANCE') DEFAULT 'ACTIVE',
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  is_active tinyint(1) DEFAULT 1,
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (lounge_id),
  KEY idx_lounge_airport (airport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lounge_amenities (
  amenity_id int(11) NOT NULL AUTO_INCREMENT,
  lounge_id int(11) NOT NULL,
  item_name varchar(100) NOT NULL,
  total_count int(11) NOT NULL DEFAULT 0,
  available_count int(11) NOT NULL DEFAULT 0,
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (amenity_id),
  KEY idx_amenity_lounge (lounge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS access_providers (
  provider_id int(11) NOT NULL AUTO_INCREMENT,
  provider_name varchar(150) NOT NULL,
  provider_type enum('AIRLINE','CREDIT_CARD','AGGREGATOR') NOT NULL,
  is_active tinyint(1) DEFAULT 1,
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS passengers (
  passenger_id int(11) NOT NULL AUTO_INCREMENT,
  booking_reference varchar(20) DEFAULT NULL,
  full_name varchar(150) NOT NULL,
  passport_number varchar(50) NOT NULL,
  nationality varchar(100) DEFAULT NULL,
  phone varchar(30) DEFAULT NULL,
  email varchar(150) DEFAULT NULL,
  membership_type varchar(100) DEFAULT NULL,
  membership_id int(11) DEFAULT NULL,
  membership_valid tinyint(1) DEFAULT 1,
  visit_count_this_month int(11) DEFAULT 0,
  ticket_class varchar(100) DEFAULT NULL,
  flight_id int(11) DEFAULT NULL,
  flight_time datetime DEFAULT NULL,
  access_status enum('ELIGIBLE', 'NOT_ELIGIBLE', 'CHECKED_IN') DEFAULT 'NOT_ELIGIBLE',
  checked_in_lounge_id int(11) DEFAULT NULL,
  assigned_seat varchar(20) DEFAULT NULL,
  check_in_time datetime DEFAULT NULL,
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  is_active tinyint(1) DEFAULT 1,
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (passenger_id),
  UNIQUE KEY passport_number (passport_number),
  UNIQUE KEY booking_reference (booking_reference),
  FOREIGN KEY (flight_id) REFERENCES flights(flight_id),
  FOREIGN KEY (membership_id) REFERENCES access_providers(provider_id)
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

CREATE TABLE IF NOT EXISTS flights (
  flight_id int(11) NOT NULL AUTO_INCREMENT,
  flight_number varchar(20) NOT NULL,
  airline varchar(100) NOT NULL,
  origin varchar(100) NOT NULL,
  destination varchar(100) NOT NULL,
  scheduled_departure datetime NOT NULL,
  scheduled_arrival datetime NOT NULL,
  status enum('SCHEDULED','BOARDING','DEPARTED','DELAYED','CANCELLED') DEFAULT 'SCHEDULED',
  passenger_capacity int(11) NOT NULL DEFAULT 150,
  eligibility_percentage decimal(3,2) NOT NULL DEFAULT 0.15,
  last_updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (flight_id),
  UNIQUE KEY idx_flight_number_date (flight_number, scheduled_departure)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS access_requests (

  request_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) NOT NULL,
  lounge_id int(11) NOT NULL,
  provider_id int(11) DEFAULT NULL,
  request_time datetime DEFAULT current_timestamp(),
  decision_time datetime DEFAULT NULL,
  status enum('PENDING','APPROVED','DENIED') NOT NULL,
  decision_reason varchar(255) DEFAULT NULL,
  checked_in_at datetime DEFAULT NULL,
  checked_out_at datetime DEFAULT NULL,
  PRIMARY KEY (request_id),
  KEY idx_request_passenger (passenger_id),
  KEY idx_request_lounge (lounge_id),
  KEY fk_request_provider (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS staff (
  staff_id int(11) NOT NULL AUTO_INCREMENT,
  airport_id int(11) NOT NULL,
  full_name varchar(150) NOT NULL,
  email varchar(150) NOT NULL,
  role enum('ADMIN','OPERATIONS_MANAGER','RECEPTION','SUPERVISOR','AUDITOR') NOT NULL,
  password_hash varchar(255) NOT NULL,
  is_active tinyint(1) DEFAULT 1,
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (staff_id),
  UNIQUE KEY email (email),
  KEY idx_staff_airport (airport_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS audit_logs (
  audit_id int(11) NOT NULL AUTO_INCREMENT,
  staff_id int(11) NOT NULL,
  action_type varchar(100) DEFAULT NULL,
  entity_name varchar(100) DEFAULT NULL,
  entity_id int(11) DEFAULT NULL,
  description text DEFAULT NULL,
  created_at datetime DEFAULT current_timestamp(),
  PRIMARY KEY (audit_id),
  KEY fk_audit_staff (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS occupancy_logs (
  log_id int(11) NOT NULL AUTO_INCREMENT,
  lounge_id int(11) NOT NULL,
  current_occupancy int(11) NOT NULL,
  recorded_at datetime DEFAULT current_timestamp(),
  PRIMARY KEY (log_id),
  KEY idx_occupancy_lounge (lounge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS passenger_memberships (
  membership_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) NOT NULL,
  provider_id int(11) NOT NULL,
  tier varchar(100) DEFAULT NULL,
  valid_from datetime NOT NULL,
  valid_to datetime NOT NULL,
  remaining_visits int(11) DEFAULT NULL,
  status enum('ACTIVE','EXPIRED','SUSPENDED') DEFAULT 'ACTIVE',
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (membership_id),
  KEY idx_membership_passenger (passenger_id),
  KEY idx_membership_provider (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id int(11) NOT NULL AUTO_INCREMENT,
  request_id int(11) NOT NULL,
  amount decimal(10,2) NOT NULL,
  currency char(3) DEFAULT 'USD',
  payment_method enum('CARD','UPI','CASH') NOT NULL,
  payment_status enum('INITIATED','SUCCESS','FAILED','REFUNDED') NOT NULL,
  processed_at datetime DEFAULT current_timestamp(),
  PRIMARY KEY (transaction_id),
  KEY fk_transaction_request (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  username varchar(100) NOT NULL,
  email varchar(150) DEFAULT NULL,
  password varchar(255) NOT NULL,
  role varchar(50) NOT NULL,   -- Stores Role enum name: ADMIN | OPERATIONS_MANAGER | SUPERVISOR | RECEPTION | AUDITOR | PASSENGER
  active tinyint(4) NOT NULL DEFAULT 1,
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  updated_at timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (id),
  UNIQUE KEY username (username),
  UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────────────────────
-- Migration: add AUDITOR to existing staff.role enum (safe to re-run on MySQL 8 / MariaDB).
-- This is a no-op if AUDITOR already exists in the enum.
-- On older MySQL versions you may need to run this manually once.
-- ─────────────────────────────────────────────────────────────────────────────
-- ─────────────────────────────────────────────────────────────────────────────
-- Migration: add AUDITOR to existing staff.role enum (safe to re-run on MySQL 8 / MariaDB).
-- This is a no-op if AUDITOR already exists in the enum.
-- On older MySQL versions you may need to run this manually once.
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE staff
  MODIFY COLUMN role enum('ADMIN','OPERATIONS_MANAGER','RECEPTION','SUPERVISOR','AUDITOR') NOT NULL;

-- ─────────────────────────────────────────────────────────────────────────────
-- UNIFIED DECISION ENGINE: Visits, Billing & Policies
-- ─────────────────────────────────────────────────────────────────────────────

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
