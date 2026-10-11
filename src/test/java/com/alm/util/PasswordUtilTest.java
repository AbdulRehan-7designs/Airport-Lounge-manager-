package com.alm.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for PasswordUtil — no DB needed, purely unit tests.
 */
public class PasswordUtilTest {

    @Test
    public void testHashIsNotPlaintext() {
        String hash = PasswordUtil.hashPassword("mySecret");
        assertNotNull(hash);
        assertNotEquals("Hash must not equal plain-text input", "mySecret", hash);
    }

    @Test
    public void testHashIsDeterministic() {
        String hash1 = PasswordUtil.hashPassword("hello");
        String hash2 = PasswordUtil.hashPassword("hello");
        assertEquals("Same input must produce same hash", hash1, hash2);
    }

    @Test
    public void testDifferentPasswordsProduceDifferentHashes() {
        String hash1 = PasswordUtil.hashPassword("password1");
        String hash2 = PasswordUtil.hashPassword("password2");
        assertNotEquals("Different inputs must produce different hashes", hash1, hash2);
    }

    @Test
    public void testVerifyMatchesHash() {
        String plain = "SuperSecure123!";
        String hash = PasswordUtil.hashPassword(plain);
        assertTrue("verifyPassword must return true for matching plain+hash",
                PasswordUtil.verifyPassword(plain, hash));
    }

    @Test
    public void testVerifyFailsForWrongPassword() {
        String hash = PasswordUtil.hashPassword("correct");
        assertFalse("verifyPassword must return false for wrong plain-text",
                PasswordUtil.verifyPassword("wrong", hash));
    }

    @Test
    public void testLegacyPlainTextFallback() {
        // PasswordUtil supports a legacy fallback: if stored value equals the
        // plain-text
        // (i.e. old un-hashed password), verifyPassword should still return true.
        String plainAsStored = "oldPassword";
        assertTrue("Legacy plain-text fallback must accept plain == stored value",
                PasswordUtil.verifyPassword("oldPassword", plainAsStored));
    }

    @Test
    public void testEmptyPasswordHash() {
        // Empty strings are valid inputs — should not throw
        String hash = PasswordUtil.hashPassword("");
        assertNotNull("Hash of empty string must not be null", hash);
        assertTrue("Empty password must verify against its own hash",
                PasswordUtil.verifyPassword("", hash));
    }
}
