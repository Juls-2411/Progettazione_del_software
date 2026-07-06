package com.example.gestioneafam.repository;

import com.example.gestioneafam.entity.Account;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class AccountRepository extends AbstractRepository<Account, Long> {

    public AccountRepository() {
        super(Account.class);
    }

    /** Caso d'uso Registrazione: verifica unicita' dell'email (username). */
    public Optional<Account> findByUsername(String username) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT a FROM Account a WHERE a.credenziali.username = :username", Account.class)
                        .setParameter("username", username)
                        .getResultStream()
                        .findFirst();
            } finally {
                em.close();
            }
        });
    }

    /** Caso d'uso Registrazione: verifica unicita' del codice fiscale. */
    public Optional<Account> findByCodiceFiscale(String codiceFiscale) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT a FROM Account a WHERE a.profilo.codiceFiscale = :cf", Account.class)
                        .setParameter("cf", codiceFiscale)
                        .getResultStream()
                        .findFirst();
            } finally {
                em.close();
            }
        });
    }

    /**
     * Rete AFAM (macro caso d'uso Comunicazione tra Nodi AFAM, requisito
     * opzionale): elenca gli account che hanno espresso il consenso alla
     * condivisione esterna, con filtro facoltativo su nome/cognome.
     * In questo prototipo mono-nodo la "rete condivisa" e' simulata
     * cercando tra gli account registrati sullo stesso nodo (vedi
     * control.GestoreFederazioneControl per la nota metodologica completa).
     */
    public List<Account> findConConsensoEsterno(String filtroNome) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                String jpql = "SELECT a FROM Account a WHERE a.consenso IS NOT NULL "
                        + "AND a.consenso.statoConsenso = 'CONCESSO'"
                        + (filtroNome == null || filtroNome.isBlank()
                        ? ""
                        : " AND (LOWER(a.profilo.nome) LIKE :filtro OR LOWER(a.profilo.cognome) LIKE :filtro)");
                jakarta.persistence.TypedQuery<Account> query = em.createQuery(jpql, Account.class);
                if (filtroNome != null && !filtroNome.isBlank()) {
                    query.setParameter("filtro", "%" + filtroNome.toLowerCase() + "%");
                }
                return query.getResultList();
            } finally {
                em.close();
            }
        });
    }
}
