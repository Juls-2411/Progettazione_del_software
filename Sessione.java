package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Sessione di lavoro dello Studente AFAM autenticato.
 * In una web app la sessione viveva nell'HttpSession del server; in
 * un'applicazione desktop persiste comunque a fini di audit/inattivita'
 * (RAD, caso d'uso 1.5 Logout), ma lo stato "vivo" della sessione corrente
 * e' gestito lato client da control.SessioneCorrente.
 */
@Entity
@Table(name = "sessione")
public class Sessione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSessione;

    private LocalDateTime dataOraInizio;
    private LocalDateTime dataOraScadenza;

    /** Valori tipici: ATTIVA, CHIUSA, SCADUTA_PER_INATTIVITA. */
    private String stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public Sessione() {
    }

    public Sessione(LocalDateTime dataOraInizio, LocalDateTime dataOraScadenza) {
        this.dataOraInizio = dataOraInizio;
        this.dataOraScadenza = dataOraScadenza;
        this.stato = "ATTIVA";
    }

    public Long getIdSessione() {
        return idSessione;
    }

    public LocalDateTime getDataOraInizio() {
        return dataOraInizio;
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
}
