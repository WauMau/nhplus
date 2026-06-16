package de.hitec.nhplus.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Stateless utility for password security.
 *
 * <p>Bundles everything the login system needs to handle passwords safely:
 * generating a random salt, hashing a password with SHA-256 over that salt,
 * verifying a candidate password against a stored hash, generating a secure
 * one-time password and validating a password against the policy. Clear-text
 * passwords are never stored &mdash; only salt and hash.</p>
 *
 * <p>Single responsibility: password cryptography and policy only. The class is
 * {@code final} and has a private constructor, so it cannot be instantiated or
 * extended; all methods are static.</p>
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * Private constructor &mdash; this is a utility class and must not be instantiated.
     */
    private PasswordUtil() {
        // Utility-Klasse — keine Instanziierung
    }

    /**
     * Generates a new random 16-byte salt, encoded as a lower-case hex string.
     *
     * @return the freshly generated salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        return toHex(salt);
    }

    /**
     * Hashes a password with SHA-256 over the given salt.
     *
     * @param password the clear-text password to hash
     * @param salt     the salt to mix in (see {@link #generateSalt()})
     * @return the resulting hash as a lower-case hex string
     * @throws IllegalStateException if the SHA-256 algorithm is not available in the JVM
     */
    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return toHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 ist in dieser JVM nicht verfügbar.", e);
        }
    }

    /**
     * Verifies a candidate password against a stored hash by re-hashing it with
     * the stored salt and comparing the results.
     *
     * @param password     the clear-text password to check
     * @param salt         the salt that was used for the stored hash
     * @param expectedHash the stored hash to compare against
     * @return {@code true} if the password matches, {@code false} otherwise
     */
    public static boolean verify(String password, String salt, String expectedHash) {
        return hash(password, salt).equalsIgnoreCase(expectedHash);
    }

    /**
     * Converts a byte array into its lower-case hexadecimal string representation.
     *
     * @param bytes the bytes to encode
     * @return the hex string
     */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Generates a random 8-character one-time password.
     *
     * <p>Drawn from a character set that mixes upper-case letters, lower-case
     * letters, digits and special characters while leaving out easily confused
     * characters (such as {@code I}, {@code l}, {@code O}, {@code 0}, {@code 1}).
     * The result always satisfies {@link #isValidPassword(String)}.</p>
     *
     * @return a newly generated secure password
     */
    public static String generateSecurePassword() {

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ" +
                        "abcdefghijkmnopqrstuvwxyz" +
                        "23456789" +
                        "!$%&?#";

        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    /**
     * Checks whether a password fulfils the password policy: at least 8
     * characters and containing at least one upper-case letter, one lower-case
     * letter and one special character.
     *
     * @param password the password to validate (may be {@code null})
     * @return {@code true} if the password meets all requirements, {@code false} otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {return false;}

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        return hasUpper && hasLower && hasSpecial;
    }
}
