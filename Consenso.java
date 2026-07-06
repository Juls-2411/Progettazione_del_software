package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Consenso dello studente alla condivisione della propria identita digitale
 * verso altri nodi AFAM (RAD, caso d'uso 6.2 Gestisci Consenso Condivisione
 * Esterna). Multiplicita' Account 1---1 Consenso: ogni studente ha un'unica
 * impostazione di consenso, aggiornabile nel tempo (concesso/negato).
 */
@Entity
@Table(name = "consenso")
public class Consenso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsenso;

    /** Valori ammessi: CONCESSO, NEGATO. Default prudenziale: NEGATO. */
    private String statoConsenso = "NEGATO";

    /** Elenco (serializzato, es. CSV) dei dati resi consultabili dalla rete AFAM. */
    private String datiCondivisibili;

    private LocalDateTime dataAggiornamento;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    public Consenso() {
    }

    public Long getIdConsenso() {
        return idConsenso;
    }

    public String getStatoConsenso() {
        return statoConsenso;
    }

    public void setStatoConsenso(String statoConsenso) {
        this.statoConsenso = statoConsenso;
    }

    public String getDatiCondivisibili() {
        return datiCondivisibili;
    }

    public void setDatiCondivisibili(String datiCondivisibili) {
        this.datiCondivisibili = datiCondivisibili;
    }

    public LocalDateTime getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(LocalDateTime dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
