package com.example.gestioneafam.control;

/**
 * Eccezione applicativa sollevata da {@link GestoreContenutiControl} nelle
 * sequenze alternative previste dal RAD §3 (contenuto non valido, nome
 * cartella duplicato o vuoto, ecc.).
 */
public class ContenutoException extends RuntimeException {

    public ContenutoException(String message) {
        super(message);
    }
}
