package com.example.gestioneafam.control;

/**
 * Eccezione applicativa sollevata da {@link GestoreFederazioneControl}
 * nelle sequenze alternative previste dal RAD §6 (profilo non disponibile
 * o non condiviso, nodo non raggiungibile).
 */
public class FederazioneException extends RuntimeException {

    public FederazioneException(String message) {
        super(message);
    }
}
