package com.example.gestioneafam.control;

/**
 * Eccezione applicativa sollevata da {@link GestoreProfiloControl} nelle
 * sequenze alternative previste dal RAD per il macro caso d'uso Gestione
 * Profilo (dati non validi, immagine non conforme, password attuale
 * errata, ecc.). Segue lo stesso schema di AutenticazioneException.
 */
public class ProfiloException extends RuntimeException {

    public ProfiloException(String message) {
        super(message);
    }
}
