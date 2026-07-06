package com.example.gestioneafam.control;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Realizza la Sequenza alternativa 1 del caso d'uso 1.5 Logout (RAD §3.4.2,
 * §3.4.3.1.5): il logout automatico innescato dall'attore Tempo dopo il
 * periodo massimo di inattivita' consentito (qui 30 minuti, come da esempio
 * del RAD).
 *
 * In una web app il timeout di inattivita' e' naturalmente gestito dal
 * server (scadenza della HttpSession) indipendentemente dalla UI. In
 * un'applicazione desktop non esiste un simile orologio esterno: il ruolo
 * dell'attore Tempo e' qui realizzato con un PauseTransition di JavaFX,
 * riavviato ad ogni evento di mouse/tastiera rilevato sulla Scene corrente
 * (vedi #osserva, richiamato da MainApp ad ogni cambio schermata).
 */
public final class MonitorInattivita {

    private static final Duration TIMEOUT_INATTIVITA = Duration.minutes(30);

    private static final PauseTransition timer = new PauseTransition(TIMEOUT_INATTIVITA);

    static {
        timer.setOnFinished(evento -> scadutoPerInattivita());
    }

    private MonitorInattivita() {
    }

    /**
     * Da richiamare dopo ogni cambio di schermata (MainApp.mostraSchermata):
     * aggancia il rilevamento di attivita' alla nuova Scene e riavvia il
     * timer. Le Scene precedenti (con i rispettivi filtri) vengono
     * semplicemente scartate dal garbage collector alla successiva
     * navigazione, come gia' avviene oggi per ogni Scene sostituita.
     */
    public static void osserva(Scene scene) {
        scene.addEventFilter(MouseEvent.ANY, MonitorInattivita::registraAttivita);
        scene.addEventFilter(KeyEvent.ANY, MonitorInattivita::registraAttivita);
        riavvia();
    }

    private static void registraAttivita(Event evento) {
        if (SessioneCorrente.getIstanza().isAutenticato()) {
            riavvia();
        }
    }

    private static void riavvia() {
        timer.stop();
        timer.playFromStart();
    }

    private static void scadutoPerInattivita() {
        if (!SessioneCorrente.getIstanza().isAutenticato()) {
            return; // nessuna sessione da chiudere (es. utente fermo sulla schermata di login)
        }
        // Stessa logica del logout manuale (chiude la Sessione lato DBMS,
        // marcandola CHIUSA, ed invalida lo stato in memoria) - evita di
        // duplicare la logica, coerentemente con la nota metodologica del RAD.
        new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService()).logout();

        SessioneCorrente.getIstanza().setMessaggioAvviso(
                "Sessione scaduta per prolungata inattivita'. Effettuare nuovamente l'accesso.");
        MainApp.mostraSchermata("login");
    }
}