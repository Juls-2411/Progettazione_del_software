package com.example.gestioneafam.util;

import java.security.SecureRandom;

/**
 * Generatore di codici numerici casuali (sostituisce il codice statico
 * "123456" usato nella bozza web solo a scopo di test).
 */
public final class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpGenerator() {
    }

    /** Genera un codice numerico a {@code lunghezza} cifre. */
    public static String generaCodice(int lunghezza) {
        StringBuilder sb = new StringBuilder(lunghezza);
        for (int i = 0; i < lunghezza; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
