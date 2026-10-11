-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 20, 2026 at 03:20 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `alm`
--

-- --------------------------------------------------------

--
-- Table structure for table `access_providers`
--

CREATE TABLE `access_providers` (
  `provider_id` int(11) NOT NULL,
  `provider_name` varchar(150) NOT NULL,
  `provider_type` enum('AIRLINE','CREDIT_CARD','AGGREGATOR') NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `access_requests`
--

CREATE TABLE `access_requests` (
  `request_id` int(11) NOT NULL,
  `passenger_id` int(11) NOT NULL,
  `lounge_id` int(11) NOT NULL,
  `provider_id` int(11) DEFAULT NULL,
  `request_time` datetime DEFAULT current_timestamp(),
  `decision_time` datetime DEFAULT NULL,
  `status` enum('PENDING','APPROVED','DENIED') NOT NULL,
  `decision_reason` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `airports`
--

CREATE TABLE `airports` (
  `airport_id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  `iata_code` char(3) NOT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `airports`
--

INSERT INTO `airports` (`airport_id`, `name`, `iata_code`, `city`, `country`, `created_at`, `is_active`, `updated_at`) VALUES
(1, 'Indira Gandhi International Airport', 'DEL', 'Delhi', 'India', '2026-02-15 16:15:27', 1, '2026-02-15 16:20:33');

-- --------------------------------------------------------

--
-- Table structure for table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `audit_id` int(11) NOT NULL,
  `staff_id` int(11) NOT NULL,
  `action_type` varchar(100) DEFAULT NULL,
  `entity_name` varchar(100) DEFAULT NULL,
  `entity_id` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `lounges`
--

CREATE TABLE `lounges` (
  `lounge_id` int(11) NOT NULL,
  `airport_id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  `terminal` varchar(50) DEFAULT NULL,
  `capacity` int(11) NOT NULL,
  `status` enum('ACTIVE','CLOSED','MAINTENANCE') DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `lounges`
--

INSERT INTO `lounges` (`lounge_id`, `airport_id`, `name`, `terminal`, `capacity`, `status`, `created_at`, `is_active`, `updated_at`) VALUES
(2, 1, 'Premium Executive Lounge', 'T3', 120, 'ACTIVE', '2026-02-15 16:16:06', 1, '2026-02-15 16:20:40');

-- --------------------------------------------------------

--
-- Table structure for table `occupancy_logs`
--

CREATE TABLE `occupancy_logs` (
  `log_id` int(11) NOT NULL,
  `lounge_id` int(11) NOT NULL,
  `current_occupancy` int(11) NOT NULL,
  `recorded_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `passengers`
--

CREATE TABLE `passengers` (
  `passenger_id` int(11) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `passport_number` varchar(50) NOT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `passenger_memberships`
--

CREATE TABLE `passenger_memberships` (
  `membership_id` int(11) NOT NULL,
  `passenger_id` int(11) NOT NULL,
  `provider_id` int(11) NOT NULL,
  `tier` varchar(100) DEFAULT NULL,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime NOT NULL,
  `remaining_visits` int(11) DEFAULT NULL,
  `status` enum('ACTIVE','EXPIRED','SUSPENDED') DEFAULT 'ACTIVE',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `staff`
--

CREATE TABLE `staff` (
  `staff_id` int(11) NOT NULL,
  `airport_id` int(11) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `role` enum('ADMIN','OPERATIONS_MANAGER','RECEPTION','SUPERVISOR') NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` int(11) NOT NULL,
  `request_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `currency` char(3) DEFAULT 'USD',
  `payment_method` enum('CARD','UPI','CASH') NOT NULL,
  `payment_status` enum('INITIATED','SUCCESS','FAILED','REFUNDED') NOT NULL,
  `processed_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  `active` tinyint(4) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_active_airports`
-- (See below for the actual view)
--
CREATE TABLE `v_active_airports` (
`airport_id` int(11)
,`name` varchar(150)
,`iata_code` char(3)
,`city` varchar(100)
,`country` varchar(100)
,`created_at` timestamp
,`is_active` tinyint(1)
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_active_lounges`
-- (See below for the actual view)
--
CREATE TABLE `v_active_lounges` (
`lounge_id` int(11)
,`airport_id` int(11)
,`name` varchar(150)
,`terminal` varchar(50)
,`capacity` int(11)
,`status` enum('ACTIVE','CLOSED','MAINTENANCE')
,`created_at` timestamp
,`is_active` tinyint(1)
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_active_passengers`
-- (See below for the actual view)
--
CREATE TABLE `v_active_passengers` (
`passenger_id` int(11)
,`full_name` varchar(150)
,`passport_number` varchar(50)
,`nationality` varchar(100)
,`phone` varchar(30)
,`email` varchar(150)
,`created_at` timestamp
,`is_active` tinyint(1)
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_active_providers`
-- (See below for the actual view)
--
CREATE TABLE `v_active_providers` (
`provider_id` int(11)
,`provider_name` varchar(150)
,`provider_type` enum('AIRLINE','CREDIT_CARD','AGGREGATOR')
,`is_active` tinyint(1)
,`created_at` timestamp
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_active_staff`
-- (See below for the actual view)
--
CREATE TABLE `v_active_staff` (
`staff_id` int(11)
,`airport_id` int(11)
,`full_name` varchar(150)
,`email` varchar(150)
,`role` enum('ADMIN','OPERATIONS_MANAGER','RECEPTION','SUPERVISOR')
,`password_hash` varchar(255)
,`is_active` tinyint(1)
,`created_at` timestamp
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Structure for view `v_active_airports`
--
DROP TABLE IF EXISTS `v_active_airports`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_active_airports`  AS SELECT `airports`.`airport_id` AS `airport_id`, `airports`.`name` AS `name`, `airports`.`iata_code` AS `iata_code`, `airports`.`city` AS `city`, `airports`.`country` AS `country`, `airports`.`created_at` AS `created_at`, `airports`.`is_active` AS `is_active`, `airports`.`updated_at` AS `updated_at` FROM `airports` WHERE `airports`.`is_active` = 1 ;

-- --------------------------------------------------------

--
-- Structure for view `v_active_lounges`
--
DROP TABLE IF EXISTS `v_active_lounges`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_active_lounges`  AS SELECT `lounges`.`lounge_id` AS `lounge_id`, `lounges`.`airport_id` AS `airport_id`, `lounges`.`name` AS `name`, `lounges`.`terminal` AS `terminal`, `lounges`.`capacity` AS `capacity`, `lounges`.`status` AS `status`, `lounges`.`created_at` AS `created_at`, `lounges`.`is_active` AS `is_active`, `lounges`.`updated_at` AS `updated_at` FROM `lounges` WHERE `lounges`.`is_active` = 1 ;

-- --------------------------------------------------------

--
-- Structure for view `v_active_passengers`
--
DROP TABLE IF EXISTS `v_active_passengers`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_active_passengers`  AS SELECT `passengers`.`passenger_id` AS `passenger_id`, `passengers`.`full_name` AS `full_name`, `passengers`.`passport_number` AS `passport_number`, `passengers`.`nationality` AS `nationality`, `passengers`.`phone` AS `phone`, `passengers`.`email` AS `email`, `passengers`.`created_at` AS `created_at`, `passengers`.`is_active` AS `is_active`, `passengers`.`updated_at` AS `updated_at` FROM `passengers` WHERE `passengers`.`is_active` = 1 ;

-- --------------------------------------------------------

--
-- Structure for view `v_active_providers`
--
DROP TABLE IF EXISTS `v_active_providers`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_active_providers`  AS SELECT `access_providers`.`provider_id` AS `provider_id`, `access_providers`.`provider_name` AS `provider_name`, `access_providers`.`provider_type` AS `provider_type`, `access_providers`.`is_active` AS `is_active`, `access_providers`.`created_at` AS `created_at`, `access_providers`.`updated_at` AS `updated_at` FROM `access_providers` WHERE `access_providers`.`is_active` = 1 ;

-- --------------------------------------------------------

--
-- Structure for view `v_active_staff`
--
DROP TABLE IF EXISTS `v_active_staff`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_active_staff`  AS SELECT `staff`.`staff_id` AS `staff_id`, `staff`.`airport_id` AS `airport_id`, `staff`.`full_name` AS `full_name`, `staff`.`email` AS `email`, `staff`.`role` AS `role`, `staff`.`password_hash` AS `password_hash`, `staff`.`is_active` AS `is_active`, `staff`.`created_at` AS `created_at`, `staff`.`updated_at` AS `updated_at` FROM `staff` WHERE `staff`.`is_active` = 1 ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `access_providers`
--
ALTER TABLE `access_providers`
  ADD PRIMARY KEY (`provider_id`);

--
-- Indexes for table `access_requests`
--
ALTER TABLE `access_requests`
  ADD PRIMARY KEY (`request_id`),
  ADD KEY `fk_request_provider` (`provider_id`),
  ADD KEY `idx_request_passenger` (`passenger_id`),
  ADD KEY `idx_request_lounge` (`lounge_id`);

--
-- Indexes for table `airports`
--
ALTER TABLE `airports`
  ADD PRIMARY KEY (`airport_id`),
  ADD UNIQUE KEY `iata_code` (`iata_code`);

--
-- Indexes for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`audit_id`),
  ADD KEY `fk_audit_staff` (`staff_id`);

--
-- Indexes for table `lounges`
--
ALTER TABLE `lounges`
  ADD PRIMARY KEY (`lounge_id`),
  ADD KEY `idx_lounge_airport` (`airport_id`);

--
-- Indexes for table `occupancy_logs`
--
ALTER TABLE `occupancy_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `idx_occupancy_lounge` (`lounge_id`);

--
-- Indexes for table `passengers`
--
ALTER TABLE `passengers`
  ADD PRIMARY KEY (`passenger_id`),
  ADD UNIQUE KEY `passport_number` (`passport_number`);

--
-- Indexes for table `passenger_memberships`
--
ALTER TABLE `passenger_memberships`
  ADD PRIMARY KEY (`membership_id`),
  ADD KEY `idx_membership_passenger` (`passenger_id`),
  ADD KEY `idx_membership_provider` (`provider_id`);

--
-- Indexes for table `staff`
--
ALTER TABLE `staff`
  ADD PRIMARY KEY (`staff_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_staff_airport` (`airport_id`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `fk_transaction_request` (`request_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `access_providers`
--
ALTER TABLE `access_providers`
  MODIFY `provider_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `access_requests`
--
ALTER TABLE `access_requests`
  MODIFY `request_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `airports`
--
ALTER TABLE `airports`
  MODIFY `airport_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `audit_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `lounges`
--
ALTER TABLE `lounges`
  MODIFY `lounge_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `occupancy_logs`
--
ALTER TABLE `occupancy_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `passengers`
--
ALTER TABLE `passengers`
  MODIFY `passenger_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `passenger_memberships`
--
ALTER TABLE `passenger_memberships`
  MODIFY `membership_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `staff`
--
ALTER TABLE `staff`
  MODIFY `staff_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `access_requests`
--
ALTER TABLE `access_requests`
  ADD CONSTRAINT `fk_request_lounge` FOREIGN KEY (`lounge_id`) REFERENCES `lounges` (`lounge_id`),
  ADD CONSTRAINT `fk_request_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`passenger_id`),
  ADD CONSTRAINT `fk_request_provider` FOREIGN KEY (`provider_id`) REFERENCES `access_providers` (`provider_id`);

--
-- Constraints for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD CONSTRAINT `fk_audit_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`);

--
-- Constraints for table `lounges`
--
ALTER TABLE `lounges`
  ADD CONSTRAINT `fk_lounge_airport` FOREIGN KEY (`airport_id`) REFERENCES `airports` (`airport_id`) ON UPDATE CASCADE;

--
-- Constraints for table `occupancy_logs`
--
ALTER TABLE `occupancy_logs`
  ADD CONSTRAINT `fk_occupancy_lounge` FOREIGN KEY (`lounge_id`) REFERENCES `lounges` (`lounge_id`) ON DELETE CASCADE;

--
-- Constraints for table `passenger_memberships`
--
ALTER TABLE `passenger_memberships`
  ADD CONSTRAINT `fk_membership_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `passengers` (`passenger_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_membership_provider` FOREIGN KEY (`provider_id`) REFERENCES `access_providers` (`provider_id`);

--
-- Constraints for table `staff`
--
ALTER TABLE `staff`
  ADD CONSTRAINT `fk_staff_airport` FOREIGN KEY (`airport_id`) REFERENCES `airports` (`airport_id`);

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `fk_transaction_request` FOREIGN KEY (`request_id`) REFERENCES `access_requests` (`request_id`) ON DELETE CASCADE;


--
-- Metadata
--
USE `phpmyadmin`;

--
-- Metadata for table access_providers
--

--
-- Metadata for table access_requests
--

--
-- Metadata for table airports
--

--
-- Metadata for table audit_logs
--

--
-- Metadata for table lounges
--

--
-- Metadata for table occupancy_logs
--

--
-- Metadata for table passengers
--

--
-- Metadata for table passenger_memberships
--

--
-- Metadata for table staff
--

--
-- Metadata for table transactions
--

--
-- Metadata for table users
--

--
-- Metadata for table v_active_airports
--

--
-- Metadata for table v_active_lounges
--

--
-- Metadata for table v_active_passengers
--

--
-- Metadata for table v_active_providers
--

--
-- Metadata for table v_active_staff
--

--
-- Metadata for database alm
--
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
