package com.example.gestioneafam.entity;

import jakarta.persistence.*;

/**
 * Rappresenta il file fisico associato a un Contenuto caricato dallo studente
 * (audio, video, spartito PDF, ecc.). Nell'originale RAD/entity web questa
 * classe si chiamava "File"; qui e' stata rinominata FileMultimediale per
 * evitare la collisione con java.io.File, usata per l'I/O reale su disco.
 */
@Entity
@Table(name = "file_multimediale")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFile;

    /** Es. MP3, WAV, MP4, MOV, PDF (RAD §3.3.5 - vincolo su formati/dimensione). */
    private String formato;

    /** Dimensione in byte. */
    private long dimensione;

    /** Percorso assoluto sul filesystem locale del nodo. */
    private String percorso;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenuto_id", unique = true)
    private Contenuto contenuto;

    public File() {
    }

    public File(String formato, long dimensione, String percorso) {
        this.formato = formato;
        this.dimensione = dimensione;
        this.percorso = percorso;
    }

    public Long getIdFile() {
        return idFile;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public long getDimensione() {
        return dimensione;
    }

    public void setDimensione(long dimensione) {
        this.dimensione = dimensione;
    }

    public String getPercorso() {
        return percorso;
    }

    public void setPercorso(String percorso) {
        this.percorso = percorso;
    }

    public Contenuto getContenuto() {
        return contenuto;
    }

    public void setContenuto(Contenuto contenuto) {
        this.contenuto = contenuto;
    }
}
