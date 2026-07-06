package com.example.gestioneafam.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Punto unico di accesso alla persistenza JPA.
 * In una web app Spring Boot questa gestione era automatica (auto-configuration);
 * in un'applicazione desktop standalone va istanziata esplicitamente all'avvio
 * e chiusa correttamente all'uscita (vedi MainApp).
 */
public final class PersistenceManager {

    private static final String PERSISTENCE_UNIT_NAME = "afamPU";
    private static EntityManagerFactory entityManagerFactory;

    private PersistenceManager() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return entityManagerFactory;
    }

    public static EntityManager newEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    /**
     * Forza la chiusura e la ricreazione della EntityManagerFactory.
     * Rappresenta, in questo prototipo desktop con database locale H2, il
     * tentativo di "riconnessione" al DBMS richiesto dal caso d'uso 7.1
     * Errore Comunicazione DBMS: in un'installazione con DBMS su rete
     * separata questo sarebbe il punto in cui si ristabilisce la
     * connessione TCP; qui si ricrea semplicemente la factory, cosi' da
     * intercettare comunque un eventuale blocco del file di database.
     */
    public static synchronized void riavvia() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }
}

