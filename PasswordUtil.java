package com.example.gestioneafam.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Nella bozza web le password non venivano mai hashate (erano confrontate
 * in chiaro con un valore hardcoded). Qui si applica un hashing con salt
 * per rispettare il requisito di tutela dei dati personali (RAD §3.3.8).
 *
 * Nota didattica: per un progetto reale si raccomanda una libreria dedicata
 * (es. BCrypt/Argon2). SHA-256 con salt e' qui usato per restare a librerie
 * di sola standard library, evitando dipendenze aggiuntive nel prototipo.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = digest(plainPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = digest(plainPassword, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private static byte[] digest(String plainPassword, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(plainPassword.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo di hashing non disponibile", e);
        }
    }
}
