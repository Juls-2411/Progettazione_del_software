package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Link univoco di condivisione verso soggetti esterni
 * (RAD, macro caso d'uso Gestione Condivisione).
 */
@Entity
@Table(name = "link_condivisione")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLink;

    @Column(unique = true, nullable = false)
    private String url;

    private LocalDateTime dataCreazione;
    private LocalDateTime scadenza;

    /** Password facoltativa a protezione del link (hash, non chiaro). */
    private String passwordHash;

    /** Valori tipici: ATTIVO, REVOCATO, SCADUTO. */
    private String stato = "ATTIVO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // Link *---* Contenuto (owning side)
    @ManyToMany
    @JoinTable(
            name = "link_contenuto",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "contenuto_id")
    )
    private List<Contenuto> contenutiSelezionati = new ArrayList<>();

    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visualizzazione> visualizzazioni = new ArrayList<>();

    public Link() {
    }

    public Long getIdLink() {
        return idLink;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public LocalDateTime getScadenza() {
        return scadenza;
    }

    public void setScadenza(LocalDateTime scadenza) {
        this.scadenza = scadenza;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public List<Contenuto> getContenutiSelezionati() {
        return contenutiSelezionati;
    }

    public List<Visualizzazione> getVisualizzazioni() {
        return visualizzazioni;
    }

    public boolean isValido() {
        return "ATTIVO".equals(stato) && (scadenza == null || LocalDateTime.now().isBefore(scadenza));
    }
}
