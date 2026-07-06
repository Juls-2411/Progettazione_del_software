package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Codice temporaneo per il recupero della password
 * (RAD, caso d'uso 1.3 Recupero Password).
 */
@Entity
@Table(name = "codice_recupero")
public class CodiceRecupero {

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

    public CodiceRecupero() {
    }

    public CodiceRecupero(String codice, LocalDateTime dataOraGenerazione, LocalDateTime dataOraScadenza) {
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

    public LocalDateTime getDataOraScadenza() {
        return dataOraScadenza;
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
