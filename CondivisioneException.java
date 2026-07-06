package com.example.gestioneafam.control;

/**
 * Eccezione applicativa sollevata da {@link GestoreCondivisioneControl}
 * nelle sequenze alternative previste dal RAD §4 (selezione o opzioni non
 * valide in creazione/modifica di un link di condivisione).
 */
public class CondivisioneException extends RuntimeException {

    public CondivisioneException(String message) {
        super(message);
    }
}
