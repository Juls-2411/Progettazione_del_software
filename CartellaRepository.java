package com.example.gestioneafam.repository;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Cartella;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repository delle Cartelle (RAD, caso d'uso 3.2 Organizza Contenuti).
 */
public class CartellaRepository extends AbstractRepository<Cartella, Long> {

    public CartellaRepository() {
        super(Cartella.class);
    }

    public List<Cartella> findByAccount(Account account) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery(
                                "SELECT c FROM Cartella c WHERE c.account.idAccount = :idAccount ORDER BY c.nome",
                                Cartella.class)
                        .setParameter("idAccount", account.getIdAccount())
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }

    /** Verifica di unicita' del nome cartella richiesta dalla sequenza alternativa "operazione non valida". */
    public boolean esisteConNome(Account account, String nome) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                Long conteggio = em.createQuery(
                                "SELECT COUNT(c) FROM Cartella c WHERE c.account.idAccount = :idAccount "
                                        + "AND LOWER(c.nome) = :nome",
                                Long.class)
                        .setParameter("idAccount", account.getIdAccount())
                        .setParameter("nome", nome.toLowerCase())
                        .getSingleResult();
                return conteggio > 0;
            } finally {
                em.close();
            }
        });
    }
}
