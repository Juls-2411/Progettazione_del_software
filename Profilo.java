package com.example.gestioneafam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Dati anagrafici e curriculum autodichiarato dello Studente AFAM
 * (RAD, macro caso d'uso Gestione Profilo).
 */
@Entity
@Table(name = "profilo")
public class Profilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProfilo;

    private String nome;
    private String cognome;
    private String codiceFiscale;
    private String email;
    private String indirizzo;
    private String recapito;

    /** Percorso locale del file PDF del curriculum autodichiarato (DSAN). */
    private String curriculumPath;

    /** Percorso locale dell'immagine profilo. */
    private String immagineProfiloPath;

    public Profilo() {
    }

    public Long getIdProfilo() {
        return idProfilo;
    }

    public void setIdProfilo(Long idProfilo) {
        this.idProfilo = idProfilo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getRecapito() {
        return recapito;
    }

    public void setRecapito(String recapito) {
        this.recapito = recapito;
    }

    public String getCurriculumPath() {
        return curriculumPath;
    }

    public void setCurriculumPath(String curriculumPath) {
        this.curriculumPath = curriculumPath;
    }

    public String getImmagineProfiloPath() {
        return immagineProfiloPath;
    }

    public void setImmagineProfiloPath(String immagineProfiloPath) {
        this.immagineProfiloPath = immagineProfiloPath;
    }
}
