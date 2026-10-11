-- ─────────────────────────────────────────────────────────────────────────────
-- Migration: Add Aviationstack API fields to flights table
-- Safe to re-run — uses IF NOT EXISTS pattern via checking column existence.
-- ─────────────────────────────────────────────────────────────────────────────

-- Add terminal column (departure terminal, e.g., "2", "I")
ALTER TABLE flights ADD COLUMN IF NOT EXISTS terminal VARCHAR(20) DEFAULT '' AFTER eligibility_percentage;

-- Add gate column (departure gate, e.g., "D11", "A1")
ALTER TABLE flights ADD COLUMN IF NOT EXISTS gate VARCHAR(20) DEFAULT '' AFTER terminal;

-- Add delay_minutes column (departure delay in minutes, 0 = on time)
ALTER TABLE flights ADD COLUMN IF NOT EXISTS delay_minutes INT DEFAULT 0 AFTER gate;

-- Update the status enum to include ARRIVED and DIVERTED from Aviationstack
ALTER TABLE flights MODIFY COLUMN status ENUM('SCHEDULED','BOARDING','DEPARTED','DELAYED','CANCELLED','ARRIVED','DIVERTED') DEFAULT 'SCHEDULED';
