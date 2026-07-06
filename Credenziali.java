package com.example.gestioneafam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Credenziali di accesso locali dello Studente AFAM.
 * La password non viene mai memorizzata in chiaro (vedi util.PasswordUtil).
 */
@Entity
@Table(name = "credenziali")
public class Credenziali {

    @Id
    private String username; // in questo dominio corrisponde all'email istituzionale

    private String passwordHash;

    public Credenziali() {
    }

    public Credenziali(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
