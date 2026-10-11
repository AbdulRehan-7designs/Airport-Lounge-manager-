CREATE TABLE IF NOT EXISTS `system_settings` (
  `setting_key` varchar(100) NOT NULL,
  `setting_value` text DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('BUSY_THRESHOLD_PCT', '90', 'Capacity percentage after which a lounge is marked as BUSY.'),
('AUTO_DENY_EXPIRED', 'true', 'Whether to automatically deny access requests if membership is expired.'),
('MAINTENANCE_MODE', 'false', 'Global toggle to put the entire management system in read-only mode.');
