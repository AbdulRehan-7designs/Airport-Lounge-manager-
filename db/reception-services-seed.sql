-- ─────────────────────────────────────────────────────────────────────────────
-- RECEPTION SERVICES SEED DATA
-- ─────────────────────────────────────────────────────────────────────────────

USE alm;

-- 1. Seed Lounge Services (Lounge 1: Plaza Premium)
INSERT INTO lounge_services (lounge_id, service_name, category, price, description, max_slots) VALUES
(1, 'Premium Shower', 'Wellness', 15.00, '30-minute hot shower with fresh towels and toiletries', 4),
(1, 'Nap Pod (1hr)', 'Rest', 25.00, 'Private soundproof sleeping pod', 6),
(1, 'Meeting Room (Small)', 'Business', 40.00, '4-person meeting room with video conferencing', 2),
(1, 'Express Buffet Upgrade', 'Dining', 10.00, 'Access to premium chef selection and alcohol', NULL),
(1, 'Spa Massage (15min)', 'Wellness', 20.00, 'Quick neck and shoulder stress relief massage', 2);

-- Seed Lounge Services (Lounge 2: ITC Green)
INSERT INTO lounge_services (lounge_id, service_name, category, price, description, max_slots) VALUES
(2, 'Luxury Shower', 'Wellness', 18.00, 'Eco-friendly luxury shower suite', 2),
(2, 'Private Suite (2hr)', 'Rest', 50.00, 'Executive suite with workstation and daybed', 3),
(2, 'Aura Scan', 'Wellness', 30.00, 'Full body relaxation and bio-feedback scan', 1);

-- 2. Create Active Visits for some passengers
-- We need these to link services. (visis.visit_id will be 1, 2, 3...)
-- Clear existing visits first to ensure IDs match our plan
DELETE FROM visit_services;
DELETE FROM visits;
ALTER TABLE visits AUTO_INCREMENT = 1;

INSERT INTO visits (passenger_id, lounge_id, access_type, check_in_time, payment_status) VALUES
(1, 1, 'COMPLIMENTARY_AIRLINE',    DATE_SUB(NOW(), INTERVAL 1 HOUR), 'NOT_REQUIRED'),
(2, 1, 'COMPLIMENTARY_MEMBERSHIP', DATE_SUB(NOW(), INTERVAL 45 MINUTE), 'NOT_REQUIRED'),
(3, 1, 'PAID',                    DATE_SUB(NOW(), INTERVAL 30 MINUTE), 'PAID'),
(6, 2, 'COMPLIMENTARY_MEMBERSHIP', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'NOT_REQUIRED'),
(7, 2, 'PAID',                    DATE_SUB(NOW(), INTERVAL 1 HOUR), 'PENDING');

-- 3. Seed Visit Services (The Queue Tasks)
-- visit_id 1 (Passenger 1) -> Shower (Pending)
INSERT INTO visit_services (visit_id, service_id, passenger_id, quantity, total_price, status, requested_at) VALUES
(1, 1, 1, 1, 15.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
-- visit_id 2 (Passenger 2) -> Nap Pod (In Progress)
(2, 2, 2, 1, 25.00, 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
-- visit_id 3 (Passenger 3) -> Meeting Room (Pending)
(3, 3, 3, 1, 40.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
-- visit_id 4 (Passenger 6) -> Luxury Shower (In Progress)
(4, 6, 6, 1, 18.00, 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
-- visit_id 5 (Passenger 7) -> Private Suite (Pending)
(5, 7, 7, 1, 50.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 4. Seed Complaints
INSERT INTO complaints (passenger_id, lounge_id, visit_id, category, description, severity, status, reported_by) VALUES
(5, 1, NULL, 'Facility', 'Heating in Section B is not working properly.', 'MEDIUM', 'OPEN', 'Reception Staff'),
(8, 1, NULL, 'Service', 'Long wait time for shower room today.', 'LOW', 'IN_PROGRESS', 'Passenger 8'),
(11, 2, NULL, 'Food', 'Buffet items were cold at 10:30 AM.', 'HIGH', 'OPEN', 'Syed Imran');

-- 5. Seed Waitlist
INSERT INTO waitlist (passenger_id, lounge_id, priority, status, requested_at) VALUES
(10, 1, 'NORMAL', 'WAITING', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(12, 1, 'VIP',    'WAITING', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(14, 2, 'HIGH',   'WAITING', DATE_SUB(NOW(), INTERVAL 5 MINUTE));
