package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;

/**
 * In una web app lo stato "sono autenticato / chi sono" viveva nella
 * HttpSession del server, condivisa tra le richieste HTTP del browser.
 * In un'applicazione desktop non esiste un server: lo stato della sessione
 * corrente vive semplicemente in memoria, per la durata del processo Java,
 * ed e' condiviso tra i vari Boundary tramite questo singleton.
 */
public final class SessioneCorrente {

    private static SessioneCorrente istanza;

    private Account accountAutenticato;
    private String otpAtteso;
    private String emailInSospeso; // usato durante il flusso login->otp

    private String codiceRecuperoAtteso;
    private String emailRecuperoInSospeso; // usato durante il flusso recupero password

    private String messaggioAvviso;
    private boolean messaggioAvvisoSuccesso;

    private SessioneCorrente() {
    }

    public static synchronized SessioneCorrente getIstanza() {
        if (istanza == null) {
            istanza = new SessioneCorrente();
        }
        return istanza;
    }

    public Account getAccountAutenticato() {
        return accountAutenticato;
    }

    public void setAccountAutenticato(Account accountAutenticato) {
        this.accountAutenticato = accountAutenticato;
    }

    public boolean isAutenticato() {
        return accountAutenticato != null;
    }

    public String getOtpAtteso() {
        return otpAtteso;
    }

    public void setOtpAtteso(String otpAtteso) {
        this.otpAtteso = otpAtteso;
    }

    public String getEmailInSospeso() {
        return emailInSospeso;
    }

    public void setEmailInSospeso(String emailInSospeso) {
        this.emailInSospeso = emailInSospeso;
    }

    public String getCodiceRecuperoAtteso() {
        return codiceRecuperoAtteso;
    }

    public void setCodiceRecuperoAtteso(String codiceRecuperoAtteso) {
        this.codiceRecuperoAtteso = codiceRecuperoAtteso;
    }

    public String getEmailRecuperoInSospeso() {
        return emailRecuperoInSospeso;
    }

    public void setEmailRecuperoInSospeso(String emailRecuperoInSospeso) {
        this.emailRecuperoInSospeso = emailRecuperoInSospeso;
    }

    public void setMessaggioAvviso(String messaggioAvviso) {
        setMessaggioAvviso(messaggioAvviso, false);
    }

    public void setMessaggioAvviso(String messaggioAvviso, boolean successo) {
        this.messaggioAvviso = messaggioAvviso;
        this.messaggioAvvisoSuccesso = successo;
    }

    public String getEConsumaMessaggioAvviso() {
        String messaggio = messaggioAvviso;
        messaggioAvviso = null;
        return messaggio;
    }

    /** Da leggere subito dopo getEConsumaMessaggioAvviso, per sapere se il
     *  messaggio appena consumato era un esito positivo o un avviso/errore. */
    public boolean isMessaggioAvvisoSuccesso() {
        return messaggioAvvisoSuccesso;
    }

    /** Invalida completamente lo stato (logout). */
    public void invalida() {
        accountAutenticato = null;
        otpAtteso = null;
        emailInSospeso = null;
        codiceRecuperoAtteso = null;
        emailRecuperoInSospeso = null;
    }
}
