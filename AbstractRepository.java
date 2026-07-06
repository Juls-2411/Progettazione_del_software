package com.example.gestioneafam.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Repository generico basato su EntityManager: fornisce le stesse operazioni
 * di base che, nella bozza web, venivano ereditate gratuitamente da
 * JpaRepository&lt;T, ID&gt; (Spring Data). Qui l'accesso ai dati e' esplicito,
 * come richiesto in un'applicazione desktop senza framework Spring.
 *
 * Realizza inoltre, in un unico punto, il macro caso d'uso 7 "Comportamento
 * Eccezionale" / caso d'uso 7.1 "Errore Comunicazione DBMS" (RAD §3.4.2 e
 * §3.4.3.7): ogni operazione persistente passa da
 * {@link #eseguiConGestioneErrori(Supplier)}, che intercetta un'interruzione
 * della comunicazione con il DBMS, sospende l'operazione, effettua fino a 3
 * tentativi di riconnessione temporizzati (attore Tempo) e, solo se tutti
 * falliscono, propaga una {@link DbmsException} che il Boundary chiamante
 * dovra' gestire chiudendo la sessione in modo sicuro. Cosi' la logica di
 * gestione dell'errore non e' duplicata nei singoli Control, in linea con
 * la nota metodologica del RAD sul caso d'uso 7.1.
 */
public abstract class AbstractRepository<T, ID> {

    private static final int TENTATIVI_MASSIMI = 3;
    private static final long ATTESA_TRA_TENTATIVI_MS = 5000; //attesa di 5 secondi

    /**
     * Gestore da invocare quando si rileva un'interruzione della
     * comunicazione con il DBMS, PRIMA di avviare i tentativi di
     * riconnessione automatici (RAD §3.4.3.7, passi 2-3: "Il sistema mostra
     * a video il messaggio di errore: 'Errore: Connessione Persa'. Lo
     * studente AFAM seleziona l'opzione 'OK'..."). Di default e' un no-op:
     * questa classe di persistenza non deve dipendere da JavaFX, quindi il
     * gestore vero viene registrato una sola volta all'avvio
     * dell'applicazione da control.GestoreConnessioneControl (vedi MainApp).
     */
    private static volatile Runnable notificaConnessionePersa = () -> { };

    private final Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /** Registra il gestore UI da eseguire alla rilevazione dell'errore, prima
     *  dei tentativi di riconnessione. Vedi il commento su {@link #notificaConnessionePersa}. */
    public static void impostaNotificaConnessionePersa(Runnable notifica) {
        notificaConnessionePersa = notifica != null ? notifica : () -> { };
    }

    public T save(T entity) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                T merged = em.contains(entity) ? entity : em.merge(entity);
                tx.commit();
                return merged;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
    }

    public Optional<T> findById(ID id) {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return Optional.ofNullable(em.find(entityClass, id));
            } finally {
                em.close();
            }
        });
    }

    public List<T> findAll() {
        return eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            try {
                return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                        .getResultList();
            } finally {
                em.close();
            }
        });
    }

    public void delete(T entity) {
        eseguiConGestioneErrori(() -> {
            EntityManager em = PersistenceManager.newEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                T managed = em.contains(entity) ? entity : em.merge(entity);
                em.remove(managed);
                tx.commit();
                return null;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        });
    }

    /**
     * Esegue un'operazione JPA applicando, in caso di errore di
     * comunicazione con il DBMS, la sequenza descritta dal caso d'uso 7.1:
     * sospensione dell'operazione, tentativi di riconnessione temporizzati
     * e, se tutti falliscono, chiusura sicura tramite {@link DbmsException}.
     * Le sottoclassi (es. AccountRepository) possono richiamarlo per le
     * proprie query dedicate, cosi' da beneficiare della stessa protezione.
     */
    protected <R> R eseguiConGestioneErrori(Supplier<R> operazione) {
        try {
            return operazione.get();
        } catch (PersistenceException | IllegalStateException erroreOriginale) {
            // RAD §3.4.3.7, passi 2-3: notifica l'interruzione ("Errore:
            // Connessione Persa") ed attende l'OK dello Studente AFAM PRIMA
            // di avviare i tentativi di riconnessione automatici.
            notificaConnessionePersa.run();
            for (int tentativo = 1; tentativo <= TENTATIVI_MASSIMI; tentativo++) {
                attendi(ATTESA_TRA_TENTATIVI_MS);
                try {
                    PersistenceManager.riavvia();
                    return operazione.get();
                } catch (PersistenceException | IllegalStateException erroreTentativo) {
                    if (tentativo == TENTATIVI_MASSIMI) {
                        throw new DbmsException(
                                "Errore critico: Impossibile comunicare con il database. Riprovare piu' tardi.",
                                erroreTentativo);
                    }
                }
            }
            // Punto teoricamente irraggiungibile: il ramo precedente rilancia
            // sempre al tentativo finale. Presente solo per completezza del tipo.
            throw new DbmsException(
                    "Errore critico: Impossibile comunicare con il database. Riprovare piu' tardi.",
                    erroreOriginale);
        }
    }

    private void attendi(long millisecondi) {
        try {
            Thread.sleep(millisecondi);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}