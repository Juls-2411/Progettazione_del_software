package com.example.gestioneafam.util;

/**
 * Rappresenta l'attore secondario "MailServer" del RAD: il sistema gli
 * affida l'invio dei codici OTP e di recupero password. In questa fase
 * di prototipazione desktop non e' collegato un vero server SMTP (cosi'
 * come, nella bozza web originale, l'invio era solo stampato in console);
 * l'interfaccia e' pero' gia' pronta per una implementazione reale
 * (es. con Jakarta Mail) senza toccare i Control che la usano.
 */
public interface MailService {

    void inviaCodiceOtp(String destinatario, String codice);

    void inviaCodiceRecuperoPassword(String destinatario, String codice);
}
