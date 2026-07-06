package com.example.gestioneafam.control;

/**
 * Eccezione applicativa sollevata da {@link GestoreVisualizzazioneControl}
 * nelle sequenze alternative previste dal RAD §5 (link non valido/scaduto/
 * revocato, password errata).
 */
public class VisualizzazioneException extends RuntimeException {

    public VisualizzazioneException(String message) {
        super(message);
    }
}
