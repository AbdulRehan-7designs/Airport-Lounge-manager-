-- ─────────────────────────────────────────────────────────────────────────────
-- RECEPTION SERVICES SCHEMA
-- Adds tables for Premium Services, Waitlist, and Complaints
-- ─────────────────────────────────────────────────────────────────────────────

USE alm;

-- 1. Available services in each lounge
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

-- 2. Services linked to specific passenger visits
CREATE TABLE IF NOT EXISTS visit_services (
  vs_id int(11) NOT NULL AUTO_INCREMENT,
  visit_id int(11) NOT NULL,
  service_id int(11) NOT NULL,
  passenger_id int(11) NOT NULL,
  quantity int(11) DEFAULT 1,
  total_price decimal(10,2) DEFAULT 0.00,
  status enum('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
  requested_at timestamp NOT NULL DEFAULT current_timestamp(),
  completed_at timestamp NULL DEFAULT NULL,
  PRIMARY KEY (vs_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id) ON DELETE CASCADE,
  FOREIGN KEY (service_id) REFERENCES lounge_services(service_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Complaints and feedback
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
  resolved_at timestamp NULL DEFAULT NULL,
  PRIMARY KEY (complaint_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Entry Waitlist
CREATE TABLE IF NOT EXISTS waitlist (
  waitlist_id int(11) NOT NULL AUTO_INCREMENT,
  passenger_id int(11) NOT NULL,
  lounge_id int(11) NOT NULL,
  priority enum('NORMAL', 'HIGH', 'VIP') DEFAULT 'NORMAL',
  status enum('WAITING', 'NOTIFIED', 'ADMITTED', 'EXPIRED', 'CANCELLED') DEFAULT 'WAITING',
  requested_at timestamp NOT NULL DEFAULT current_timestamp(),
  notified_at timestamp NULL DEFAULT NULL,
  PRIMARY KEY (waitlist_id),
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id),
  FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Visit Guests (referenced by ReceptionServiceDAO.addGuestToVisit)
CREATE TABLE IF NOT EXISTS visit_guests (
  guest_id int(11) NOT NULL AUTO_INCREMENT,
  visit_id int(11) NOT NULL,
  passenger_id int(11) NOT NULL,
  guest_name varchar(150) NOT NULL,
  guest_document varchar(50) DEFAULT NULL,
  fee decimal(10,2) DEFAULT 0.00,
  added_at timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (guest_id),
  FOREIGN KEY (visit_id) REFERENCES visits(visit_id) ON DELETE CASCADE,
  FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
