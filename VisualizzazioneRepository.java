package com.example.gestioneafam.repository;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Visualizzazione;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repository delle Visualizzazioni (RAD, caso d'uso 5.1 Visualizza
 * Contenuti Condivisi / 5.2 Consulta Riscontri di Visualizzazione).
 *
 * Le Visualizzazioni sono create e lette autonomamente, senza navigare la
 * collezione LAZY Link.visualizzazioni: cosi' come discusso nel commento
 * di entity.Account sulla scelta EAGER/LAZY, un'applicazione desktop senza
 * Open Session In View chiuderebbe l'EntityManager prima che il Control
 * possa attraversare quella relazione, generando una
 * LazyInitializationException. Le query dedicate con JOIN FETCH qui sotto
 * evitano il problema.
 */
public class VisualizzazioneRepository extends AbstractRepository<Visualizzazione, Long> {

    public VisualizzazioneRepository() {
        super(Visualizzazione.class);
    }

    /** Caso d'uso 5.2: riscontri di tutti i link dello studente, con il link gia' caricato (JOIN FETCH). */
    public List<Visualizzazione> findByAccount(Account account) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT v FROM Visualizzazione v JOIN FETCH v.link l "
                                        + "WHERE l.account.idAccount = :idAccount ORDER BY v.dataOra DESC",
                                Visualizzazione.class)
                        .setParameter("idAccount", account.getIdAccount())
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }
}
