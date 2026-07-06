package com.example.gestioneafam.control;

/**
 * Eccezione applicativa sollevata dai Control quando si verifica una delle
 * sequenze alternative previste dal RAD (dati non validi, credenziali errate,
 * codice scaduto, ecc.). I Boundary la intercettano e mostrano il messaggio
 * corrispondente nell'interfaccia, cosi' come nel flusso web i vari
 * "redirect:/login?error=..." mostravano un messaggio nella pagina.
 */
public class AutenticazioneException extends RuntimeException {

    public AutenticazioneException(String message) {
        super(message);
    }
}
