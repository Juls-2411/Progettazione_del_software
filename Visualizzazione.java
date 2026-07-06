package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Riscontro di avvenuta apertura di un link condiviso da un Utente Esterno
 * (RAD, caso d'uso 5.1 Visualizza Contenuti Condivisi). Mantenuto a livello
 * generale, senza tracciamento di dati tecnici del visitatore, per scelta
 * di privacy coerente con il RAD.
 */
@Entity
@Table(name = "visualizzazione")
public class Visualizzazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVisualizzazione;

    private LocalDateTime dataOra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id")
    private Link link;

    public Visualizzazione() {
    }

    public Visualizzazione(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    public Long getIdVisualizzazione() {
        return idVisualizzazione;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}
