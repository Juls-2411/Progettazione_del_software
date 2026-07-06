package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity centrale del dominio: rappresenta l'account di uno Studente AFAM.
 * Tutte le altre entity legate allo studente (Credenziali, Profilo, Contenuti,
 * Link, ecc.) sono raggiungibili a partire da Account, coerentemente con il
 * diagramma delle classi (§3.4.4 del RAD).
 */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAccount;

    // Account 1---1 Credenziali (owning side: Account possiede la FK)
    //
    // NOTA SULLA SCELTA EAGER: in un'applicazione desktop senza un
    // "Open Session In View" (pattern tipico del web, dove la sessione
    // Hibernate resta aperta per tutta la richiesta HTTP), ogni
    // repository apre e chiude un EntityManager per singola operazione
    // (vedi AbstractRepository). Una relazione LAZY letta dal Control
    // DOPO che l'EntityManager e' stato chiuso genera percio' una
    // LazyInitializationException ("no Session"). Per le associazioni
    // che il livello Control usa sempre subito dopo il caricamento
    // dell'Account (credenziali, profilo, consenso, e le collezioni
    // storiche di sicurezza) si sceglie quindi EAGER: per il volume di
    // dati di un singolo studente il costo aggiuntivo e' trascurabile.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "credenziali_username", referencedColumnName = "username", unique = true)
    private Credenziali credenziali;

    // Account 1---1 Profilo
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "profilo_id", unique = true)
    private Profilo profilo;

    // Account 1---1 Consenso (consenso alla condivisione esterna, unico per studente)
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Consenso consenso;

    // Account 1---* Sessione
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Sessione> sessioni = new ArrayList<>();

    // Account 1---* CodiceOTP (storico dei codici generati nel tempo)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CodiceOTP> codiciOtp = new ArrayList<>();

    // Account 1---* CodiceRecupero
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CodiceRecupero> codiciRecupero = new ArrayList<>();

    // Account 1---* IdentitaDigitaleEsterna (SPID/eIDAS associate nel tempo)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<IdentitaDigitaleEsterna> identitaEsterne = new ArrayList<>();

    // Account 1---* Cartella (queste tre restano LAZY: non ancora usate dai
    // Control implementati; se in futuro un Control le legge dopo la
    // chiusura dell'EntityManager, applicare lo stesso ragionamento sopra,
    // oppure preferire una query con JOIN FETCH dedicata nel repository).
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cartella> cartelle = new ArrayList<>();

    // Account 1---* Contenuto
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contenuto> contenuti = new ArrayList<>();

    // Account 1---* Link
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> link = new ArrayList<>();

    public Account() {
    }

    public Long getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(Long idAccount) {
        this.idAccount = idAccount;
    }

    public Credenziali getCredenziali() {
        return credenziali;
    }

    public void setCredenziali(Credenziali credenziali) {
        this.credenziali = credenziali;
    }

    public Profilo getProfilo() {
        return profilo;
    }

    public void setProfilo(Profilo profilo) {
        this.profilo = profilo;
    }

    public Consenso getConsenso() {
        return consenso;
    }

    public void setConsenso(Consenso consenso) {
        this.consenso = consenso;
        if (consenso != null) {
            consenso.setAccount(this);
        }
    }

    public List<Sessione> getSessioni() {
        return sessioni;
    }

    public List<CodiceOTP> getCodiciOtp() {
        return codiciOtp;
    }

    public List<CodiceRecupero> getCodiciRecupero() {
        return codiciRecupero;
    }

    public List<IdentitaDigitaleEsterna> getIdentitaEsterne() {
        return identitaEsterne;
    }

    public List<Cartella> getCartelle() {
        return cartelle;
    }

    public List<Contenuto> getContenuti() {
        return contenuti;
    }

    public List<Link> getLink() {
        return link;
    }

    public void aggiungiCodiceOtp(CodiceOTP codice) {
        codiciOtp.add(codice);
        codice.setAccount(this);
    }

    public void aggiungiCodiceRecupero(CodiceRecupero codice) {
        codiciRecupero.add(codice);
        codice.setAccount(this);
    }

    public void aggiungiSessione(Sessione sessione) {
        sessioni.add(sessione);
        sessione.setAccount(this);
    }

    /** Caso d'uso 1.4 Login con SPID/eIDAS: associa una nuova identita' esterna certificata. */
    public void aggiungiIdentitaEsterna(IdentitaDigitaleEsterna identita) {
        identitaEsterne.add(identita);
        identita.setAccount(this);
    }
}
