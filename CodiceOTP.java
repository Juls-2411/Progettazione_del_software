package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Codice OTP usato come secondo fattore di autenticazione al Login
 * (RAD, caso d'uso 1.2 Login). A differenza della bozza web, non e' la
 * chiave primaria: un account genera piu' codici OTP nel tempo, uno per
 * ogni tentativo di accesso.
 */
@Entity
@Table(name = "codice_otp")
public class CodiceOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codice;

    private LocalDateTime dataOraGenerazione;
    private LocalDateTime dataOraScadenza;

    /** Valori tipici: ATTIVO, USATO, SCADUTO. */
    private String stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public CodiceOTP() {
    }

    public CodiceOTP(String codice, LocalDateTime dataOraGenerazione, LocalDateTime dataOraScadenza) {
        this.codice = codice;
        this.dataOraGenerazione = dataOraGenerazione;
        this.dataOraScadenza = dataOraScadenza;
        this.stato = "ATTIVO";
    }

    public Long getId() {
        return id;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public LocalDateTime getDataOraGenerazione() {
        return dataOraGenerazione;
    }

    public void setDataOraGenerazione(LocalDateTime dataOraGenerazione) {
        this.dataOraGenerazione = dataOraGenerazione;
    }

    public LocalDateTime getDataOraScadenza() {
        return dataOraScadenza;
    }

    public void setDataOraScadenza(LocalDateTime dataOraScadenza) {
        this.dataOraScadenza = dataOraScadenza;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isScaduto() {
        return LocalDateTime.now().isAfter(dataOraScadenza);
    }
}
