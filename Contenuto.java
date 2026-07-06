package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Elemento della produzione artistica dello studente: un file caricato
 * oppure un collegamento a piattaforma esterna (RAD, macro caso d'uso
 * Gestione Contenuti).
 */
@Entity
@Table(name = "contenuto")
public class Contenuto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContenuto;

    private String titolo;
    private String didascalia;
    private String testoAlternativo;

    /** Valori tipici: FILE_AUDIO, FILE_VIDEO, FILE_SPARTITO, LINK_ESTERNO. */
    private String tipo;

    /** Valori ammessi: PRIVATO, PUBBLICO, CONDIVISO_VIA_LINK (RAD §3.1). */
    private String visibilita = "PRIVATO";

    /** Valori tipici: ATTIVO, ARCHIVIATO. */
    private String statoArchiviazione = "ATTIVO";

    /** Valorizzato solo se il contenuto e' un collegamento esterno. */
    private String urlEsterno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartella_id")
    private Cartella cartella;

    // Contenuto 1---1 File (solo se e' un file caricato, non un link esterno)
    @OneToOne(mappedBy = "contenuto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private File file;

    // Contenuto *---* Link: un contenuto puo' comparire in piu' link di condivisione
    @ManyToMany(mappedBy = "contenutiSelezionati")
    private List<Link> linkDiCondivisione = new ArrayList<>();

    public Contenuto() {
    }

    public Long getIdContenuto() {
        return idContenuto;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDidascalia() {
        return didascalia;
    }

    public void setDidascalia(String didascalia) {
        this.didascalia = didascalia;
    }

    public String getTestoAlternativo() {
        return testoAlternativo;
    }

    public void setTestoAlternativo(String testoAlternativo) {
        this.testoAlternativo = testoAlternativo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getVisibilita() {
        return visibilita;
    }

    public void setVisibilita(String visibilita) {
        this.visibilita = visibilita;
    }

    public String getStatoArchiviazione() {
        return statoArchiviazione;
    }

    public void setStatoArchiviazione(String statoArchiviazione) {
        this.statoArchiviazione = statoArchiviazione;
    }

    public String getUrlEsterno() {
        return urlEsterno;
    }

    public void setUrlEsterno(String urlEsterno) {
        this.urlEsterno = urlEsterno;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Cartella getCartella() {
        return cartella;
    }

    public void setCartella(Cartella cartella) {
        this.cartella = cartella;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        if (file != null) {
            file.setContenuto(this);
        }
    }

    public List<Link> getLinkDiCondivisione() {
        return linkDiCondivisione;
    }
}
