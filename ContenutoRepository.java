package com.example.gestioneafam.repository;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Cartella;
import com.example.gestioneafam.entity.Contenuto;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repository dei Contenuti (RAD, macro caso d'uso Gestione Contenuti).
 */
public class ContenutoRepository extends AbstractRepository<Contenuto, Long> {

    public ContenutoRepository() {
        super(Contenuto.class);
    }

    /**
     * Elenco dei contenuti di uno studente, usato dall'Area Contenuti e
     * dalla creazione dei link. La cartella e' caricata con JOIN FETCH:
     * Contenuto.cartella e' una relazione LAZY e la Boundary la legge
     * (colonna "Cartella" della tabella) dopo la chiusura dell'EntityManager.
     */
    public List<Contenuto> findByAccount(Account account) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT c FROM Contenuto c LEFT JOIN FETCH c.cartella "
                                        + "WHERE c.account.idAccount = :idAccount ORDER BY c.idContenuto DESC",
                                Contenuto.class)
                        .setParameter("idAccount", account.getIdAccount())
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }

    /** Caso d'uso 3.2 Organizza Contenuti (eliminazione cartella): contenuti attualmente in una cartella. */
    public List<Contenuto> findByCartella(Cartella cartella) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT c FROM Contenuto c WHERE c.cartella.idCartella = :idCartella",
                                Contenuto.class)
                        .setParameter("idCartella", cartella.getIdCartella())
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }

    /**
     * Sezione Pubblica (RAD §3.2, requisito funzionale 8 Gestione Contenuti):
     * elenco dei contenuti che qualunque studente autenticato del nodo puo'
     * consultare perche' marcati come pubblici. Account e Profilo del
     * proprietario sono caricati con JOIN FETCH per poter mostrare il nome
     * dell'autore dopo la chiusura dell'EntityManager.
     */
    public List<Contenuto> findPubbliciAttivi() {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT c FROM Contenuto c LEFT JOIN FETCH c.account a LEFT JOIN FETCH a.profilo "
                                        + "WHERE c.visibilita = 'PUBBLICO' AND c.statoArchiviazione = 'ATTIVO' "
                                        + "ORDER BY c.idContenuto DESC",
                                Contenuto.class)
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }
}
