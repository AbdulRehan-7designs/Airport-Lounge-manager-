package com.alm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies the given plain password against the stored value.
     * Accepts both: (1) stored hash (SHA-256 Base64), (2) plain text (legacy).
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String stored = storedHash.trim();
        // Normal check: stored value is the hash of the password
        if (stored.equals(hashPassword(plainPassword))) return true;
        // Legacy: stored value is plain text (e.g. "admin" in DB)
        return stored.equals(plainPassword);
    }
}
