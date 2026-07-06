package com.example.gestioneafam.util;

/**
 * Implementazione "di sviluppo" di MailService: stampa in console invece di
 * inviare una vera email, esattamente come faceva il System.out.println
 * nella bozza web (AutenticazioneController). Da sostituire con un
 * MailService reale in fase di distribuzione.
 */
public class ConsoleMailService implements MailService {

    @Override
    public void inviaCodiceOtp(String destinatario, String codice) {
        System.out.println("[SIMULAZIONE EMAIL] A: " + destinatario + " - Il tuo codice OTP e': " + codice);
    }

    @Override
    public void inviaCodiceRecuperoPassword(String destinatario, String codice) {
        System.out.println("[SIMULAZIONE EMAIL] A: " + destinatario + " - Codice di recupero password: " + codice);
    }
}
