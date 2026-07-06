package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Raggruppamento libero di Contenuti definito dallo Studente AFAM
 * (RAD, caso d'uso 3.2 Organizza Contenuti).
 */
@Entity
@Table(name = "cartella")
public class Cartella {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCartella;

    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // Cartella (whole) --raggruppa (aggregazione)--> Contenuto (part)
    @OneToMany(mappedBy = "cartella")
    private List<Contenuto> contenuti = new ArrayList<>();

    public Cartella() {
    }

    public Cartella(String nome) {
        this.nome = nome;
    }

    public Long getIdCartella() {
        return idCartella;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<Contenuto> getContenuti() {
        return contenuti;
    }
}
