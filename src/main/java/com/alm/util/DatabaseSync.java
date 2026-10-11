package com.alm.util;

import com.alm.util.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSync {
        public static void main(String[] args) {
                try (Connection conn = DBConnection.getConnection();
                                Statement stmt = conn.createStatement()) {

                        System.out.println("Applying schema changes for Unified Decision Engine...");

                        // Try to add the new UDE columns to passengers
                        try {
                                stmt.execute("ALTER TABLE passengers " +
                                                "ADD COLUMN booking_reference varchar(20) DEFAULT NULL, " +
                                                "ADD COLUMN flight_id int(11) DEFAULT NULL, " +
                                                "ADD COLUMN membership_id int(11) DEFAULT NULL, " +
                                                "ADD COLUMN membership_valid tinyint(1) DEFAULT 1, " +
                                                "ADD COLUMN visit_count_this_month int(11) DEFAULT 0");
                                System.out.println("Passengers table passenger unified columns added!");
                        } catch (Exception e) {
                                System.out.println("Passenger table columns might exist. " + e.getMessage());
                        }

                        // Create lounge_policies
                        stmt.execute("CREATE TABLE IF NOT EXISTS lounge_policies (" +
                                        "policy_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "lounge_id int(11) NOT NULL, " +
                                        "entry_fee decimal(10,2) NOT NULL DEFAULT 40.00, " +
                                        "guest_fee decimal(10,2) NOT NULL DEFAULT 20.00, " +
                                        "free_visit_limit int(11) NOT NULL DEFAULT 4, " +
                                        "overstay_rate decimal(10,2) NOT NULL DEFAULT 15.00, " +
                                        "PRIMARY KEY (policy_id), " +
                                        "FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("lounge_policies created");

                        // Create visits
                        stmt.execute("CREATE TABLE IF NOT EXISTS visits (" +
                                        "visit_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "passenger_id int(11) NOT NULL, " +
                                        "lounge_id int(11) NOT NULL, " +
                                        "access_type ENUM('COMPLIMENTARY_AIRLINE', 'COMPLIMENTARY_MEMBERSHIP', 'PAID', 'DENIED') NOT NULL, "
                                        +
                                        "check_in_time datetime DEFAULT CURRENT_TIMESTAMP, " +
                                        "check_out_time datetime DEFAULT NULL, " +
                                        "payment_status ENUM('PENDING', 'PAID', 'NOT_REQUIRED') DEFAULT 'NOT_REQUIRED', "
                                        +
                                        "PRIMARY KEY (visit_id), " +
                                        "FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id), " +
                                        "FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("visits created");

                        // Create billing
                        stmt.execute("CREATE TABLE IF NOT EXISTS billing (" +
                                        "bill_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "visit_id int(11) NOT NULL, " +
                                        "charge_type varchar(50) NOT NULL, " +
                                        "amount decimal(10,2) NOT NULL, " +
                                        "status ENUM('UNPAID', 'PAID') DEFAULT 'UNPAID', " +
                                        "created_at datetime DEFAULT CURRENT_TIMESTAMP, " +
                                        "PRIMARY KEY (bill_id), " +
                                        "FOREIGN KEY (visit_id) REFERENCES visits(visit_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("billing created");

                        // --- RECEPTION SERVICES TABLES ---

                        // lounge_services
                        stmt.execute("CREATE TABLE IF NOT EXISTS lounge_services (" +
                                        "service_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "lounge_id int(11) NOT NULL, " +
                                        "service_name varchar(100) NOT NULL, " +
                                        "category varchar(50) DEFAULT 'General', " +
                                        "price decimal(10,2) NOT NULL DEFAULT 0.00, " +
                                        "description text DEFAULT NULL, " +
                                        "max_slots int(11) DEFAULT NULL, " +
                                        "is_available tinyint(1) DEFAULT 1, " +
                                        "PRIMARY KEY (service_id), " +
                                        "FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("lounge_services created");

                        // visit_services
                        stmt.execute("CREATE TABLE IF NOT EXISTS visit_services (" +
                                        "vs_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "visit_id int(11) NOT NULL, " +
                                        "service_id int(11) NOT NULL, " +
                                        "passenger_id int(11) NOT NULL, " +
                                        "quantity int(11) DEFAULT 1, " +
                                        "total_price decimal(10,2) DEFAULT 0.00, " +
                                        "status enum('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING', "
                                        +
                                        "requested_at timestamp NOT NULL DEFAULT current_timestamp(), " +
                                        "PRIMARY KEY (vs_id), " +
                                        "FOREIGN KEY (visit_id) REFERENCES visits(visit_id), " +
                                        "FOREIGN KEY (service_id) REFERENCES lounge_services(service_id), " +
                                        "FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("visit_services created");

                        // complaints
                        stmt.execute("CREATE TABLE IF NOT EXISTS complaints (" +
                                        "complaint_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "passenger_id int(11) DEFAULT NULL, " +
                                        "lounge_id int(11) NOT NULL, " +
                                        "visit_id int(11) DEFAULT NULL, " +
                                        "category varchar(50) NOT NULL, " +
                                        "description text NOT NULL, " +
                                        "severity enum('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM', " +
                                        "status enum('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN', " +
                                        "reported_by varchar(100) DEFAULT 'Staff', " +
                                        "reported_at timestamp NOT NULL DEFAULT current_timestamp(), " +
                                        "PRIMARY KEY (complaint_id), " +
                                        "FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id), " +
                                        "FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id), " +
                                        "FOREIGN KEY (visit_id) REFERENCES visits(visit_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("complaints created");

                        // waitlist
                        stmt.execute("CREATE TABLE IF NOT EXISTS waitlist (" +
                                        "waitlist_id int(11) NOT NULL AUTO_INCREMENT, " +
                                        "passenger_id int(11) NOT NULL, " +
                                        "lounge_id int(11) NOT NULL, " +
                                        "priority enum('NORMAL', 'HIGH', 'VIP') DEFAULT 'NORMAL', " +
                                        "status enum('WAITING', 'NOTIFIED', 'ADMITTED', 'EXPIRED', 'CANCELLED') DEFAULT 'WAITING', "
                                        +
                                        "requested_at timestamp NOT NULL DEFAULT current_timestamp(), " +
                                        "PRIMARY KEY (waitlist_id), " +
                                        "FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id), " +
                                        "FOREIGN KEY (lounge_id) REFERENCES lounges(lounge_id)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                        System.out.println("waitlist created");

                        // --- SEEDING DATA FOR SERVICE QUEUES ---

                        // Only seed if empty
                        java.sql.ResultSet rsServices = stmt.executeQuery("SELECT COUNT(*) FROM lounge_services");
                        if (rsServices.next() && rsServices.getInt(1) == 0) {
                                stmt.execute("INSERT INTO lounge_services (lounge_id, service_name, category, price) VALUES "
                                                +
                                                "(1, 'Premium Shower', 'Wellness', 15.00), " +
                                                "(1, 'Nap Pod (1hr)', 'Rest', 25.00), " +
                                                "(1, 'Meeting Room', 'Business', 40.00), " +
                                                "(2, 'Luxury Shower', 'Wellness', 18.00)");
                                System.out.println("Seeded lounge_services");
                        }

                        // Create active visits for our viva passengers if they don't have one
                        // Passenger 1 in Lounge 1
                        stmt.execute("INSERT IGNORE INTO visits (visit_id, passenger_id, lounge_id, access_type, check_in_time) "
                                        +
                                        "VALUES (101, 1, 1, 'COMPLIMENTARY_AIRLINE', NOW())");
                        // Passenger 2 in Lounge 1
                        stmt.execute("INSERT IGNORE INTO visits (visit_id, passenger_id, lounge_id, access_type, check_in_time) "
                                        +
                                        "VALUES (102, 2, 1, 'COMPLIMENTARY_MEMBERSHIP', NOW())");

                        // Seed service queue tasks
                        java.sql.ResultSet rsVS = stmt.executeQuery("SELECT COUNT(*) FROM visit_services");
                        if (rsVS.next() && rsVS.getInt(1) == 0) {
                                stmt.execute("INSERT INTO visit_services (visit_id, service_id, passenger_id, status) VALUES "
                                                +
                                                "(101, 1, 1, 'PENDING'), " +
                                                "(102, 2, 2, 'IN_PROGRESS')");
                                System.out.println("Seeded active service queue!");
                        }

                        // Seed complaints
                        java.sql.ResultSet rsComp = stmt.executeQuery("SELECT COUNT(*) FROM complaints");
                        if (rsComp.next() && rsComp.getInt(1) == 0) {
                                stmt.execute("INSERT INTO complaints (lounge_id, category, description, severity) VALUES "
                                                +
                                                "(1, 'Facility', 'HVAC temperature too high in Section A', 'MEDIUM'), "
                                                +
                                                "(1, 'Service', 'Long queue for buffet upgrade', 'LOW')");
                                System.out.println("Seeded complaints!");
                        }

                        // Fix and update seed data
                        stmt.execute("UPDATE passengers SET full_name = 'Viva_FirstClass_Flyer', ticket_class='First Class', "
                                        +
                                        "membership_valid=1, visit_count_this_month=0, access_status='ELIGIBLE' WHERE passenger_id=1");
                        stmt.execute(
                                        "UPDATE passengers SET full_name = 'Viva_Premium_Member', ticket_class='Economy', membership_type='Amex Platinum', "
                                                        +
                                                        "membership_valid=1, visit_count_this_month=2, access_status='ELIGIBLE' WHERE passenger_id=2");
                        stmt.execute(
                                        "UPDATE passengers SET full_name = 'Viva_Exhausted_Member', ticket_class='Economy', membership_type='Amex Platinum', "
                                                        +
                                                        "membership_valid=1, visit_count_this_month=4, access_status='ELIGIBLE' WHERE passenger_id=3");
                        stmt.execute(
                                        "UPDATE passengers SET full_name = 'Viva_Denied_Access', ticket_class='Economy', membership_type=NULL, "
                                                        +
                                                        "membership_valid=1, visit_count_this_month=0, access_status='NOT_ELIGIBLE' WHERE passenger_id=4");
                        stmt.execute(
                                        "UPDATE passengers SET full_name = 'Viva_Regular_PayPerUse', ticket_class='Economy', membership_type=NULL, "
                                                        +
                                                        "membership_valid=1, visit_count_this_month=0, access_status='ELIGIBLE' WHERE passenger_id=5");
                        System.out.println("Viva Targets set up in passengers!");

                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        System.exit(0);
                }
        }
}
