package com.example.gestioneafam.repository;

/**
 * Eccezione corrispondente al macro caso d'uso 7 "Comportamento Eccezionale"
 * / caso d'uso 7.1 "Errore Comunicazione DBMS" del RAD.
 *
 * Non e' legata a un singolo macro caso d'uso applicativo (Autenticazione,
 * Profilo, Contenuti, ...): rappresenta la relazione «extend» che, nel
 * modello dei casi d'uso (§3.4.2), collega "Errore Comunicazione DBMS" a
 * *tutti* i casi d'uso che comportano una lettura o scrittura persistente.
 * Per questo e' sollevata una sola volta, dentro {@link AbstractRepository},
 * e non duplicata nei singoli Control (coerentemente con la nota
 * metodologica del RAD: "evitando di duplicare la logica di gestione
 * dell'errore nei singoli casi d'uso").
 *
 * I Boundary che la intercettano devono, coerentemente con la
 * postcondizione del caso d'uso, mostrare il messaggio critico, invalidare
 * la sessione corrente e reindirizzare l'utente al login
 * (vedi MainApp.gestisciErroreCritico).
 */
public class DbmsException extends RuntimeException {

    public DbmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DbmsException(String message) {
        super(message);
    }
}
