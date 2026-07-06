package com.example.gestioneafam.repository;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Link;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Repository dei Link di condivisione (RAD, macro caso d'uso Gestione
 * Condivisione e caso d'uso 5.1 Visualizza Contenuti Condivisi).
 */
public class LinkRepository extends AbstractRepository<Link, Long> {

    public LinkRepository() {
        super(Link.class);
    }

    /**
     * Elenco dei link dello studente con i contenuti selezionati gia'
     * caricati (JOIN FETCH): la Boundary (es. tabella "I miei link") deve
     * poterne leggere il conteggio dopo la chiusura dell'EntityManager, e
     * Link.contenutiSelezionati e' altrimenti una collezione LAZY.
     */
    public List<Link> findByAccount(Account account) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT l FROM Link l LEFT JOIN FETCH l.contenutiSelezionati "
                                        + "WHERE l.account.idAccount = :idAccount ORDER BY l.dataCreazione DESC",
                                Link.class)
                        .setParameter("idAccount", account.getIdAccount())
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }

    /**
     * Usato dall'Utente Esterno (caso d'uso 5.1 Visualizza Contenuti
     * Condivisi): recupero del link a partire dal token contenuto nell'URL
     * ricevuto, senza richiedere alcuna autenticazione. I contenuti
     * selezionati e il relativo file multimediale (necessario per
     * l'apertura del contenuto) sono caricati con JOIN FETCH, per lo
     * stesso motivo del metodo precedente: Contenuto.file e' una
     * relazione LAZY che altrimenti non sarebbe piu' accessibile dopo
     * la chiusura dell'EntityManager.
     */
    public Optional<Link> findByUrl(String url) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT l FROM Link l "
                                        + "LEFT JOIN FETCH l.contenutiSelezionati cs "
                                        + "LEFT JOIN FETCH cs.file "
                                        + "WHERE l.url = :url",
                                Link.class)
                        .setParameter("url", url)
                        .getResultStream()
                        .findFirst();
            } finally {
                em.close();
            }
        });
    }
}
