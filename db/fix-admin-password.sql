-- Fix admin login: set password to the exact hash of "admin" (SHA-256 + Base64).
-- Your DB had a typo: @ instead of 'a', and possibly missing trailing =.
-- Run this in phpMyAdmin (database: alm).

UPDATE users
SET password = 'jZae727K08KaOhKSkV3QeCQADsxZqgX1uFQw/6+0z4g='
WHERE username = 'admin';
