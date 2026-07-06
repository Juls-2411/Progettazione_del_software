package com.example.gestioneafam;

import com.example.gestioneafam.control.GestoreConnessioneControl;
import com.example.gestioneafam.control.MonitorInattivita;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.repository.PersistenceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Classe di avvio dell'applicazione desktop.
 *
 * Nella bozza web, GestioneAfamApplication (Spring Boot) avviava un server
 * HTTP e affidava la navigazione al browser tramite redirect. Qui non esiste
 * ne' un server ne' un browser: MainApp apre direttamente una finestra
 * (Stage) e gestisce la navigazione tra le schermate cambiando la Scene al
 * suo interno, con lo stesso identico percorso previsto dal RAD:
 * Login -> (OTP) -> Dashboard, con Registrazione e Recupero Password
 * raggiungibili dalla schermata di Login.
 */
public class MainApp extends Application {

    private static Stage stagePrincipale;

    @Override
    public void start(Stage stage) {
        stagePrincipale = stage;
        stage.setTitle("Sistema Gestione Identita Digitale AFAM");
        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/prova.jpg"))));
        stage.setResizable(true);
        // RAD §7.1, passi 2-3: attiva la notifica "Errore: Connessione Persa"
        // che precede i tentativi automatici di riconnessione.
        GestoreConnessioneControl.attiva();
        mostraSchermata("login");
        stage.show();
    }

    @Override
    public void stop() {
        // Chiusura pulita della persistenza allo spegnimento dell'applicazione:
        // in una web app se ne occupava il container Spring alla shutdown del server.
        PersistenceManager.shutdown();
    }

    /**
     * Cambia la schermata mostrata nella finestra principale.
     *
     * @param nomeFxml nome del file FXML (senza estensione) in
     *                 resources/com/example/gestioneafam/fxml/
     */
    public static void mostraSchermata(String nomeFxml) {
        try {
            String path = "/com/example/gestioneafam/fxml/" + nomeFxml + ".fxml";
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(path));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(Objects.requireNonNull(
                    MainApp.class.getResource("/com/example/gestioneafam/css/style.css")).toExternalForm());
            stagePrincipale.setScene(scene);
            stagePrincipale.sizeToScene();
            stagePrincipale.centerOnScreen();
            // RAD §1.5, Seq. Alt. 1 (logout automatico per inattività):
            // ogni nuova schermata viene osservata dal monitor di inattività.
            MonitorInattivita.osserva(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile caricare la schermata: " + nomeFxml, e);
        }
    }

    /**
     * Gestione del caso d'uso 7.1 Errore Comunicazione DBMS quando i
     * tentativi di riconnessione automatici (vedi
     * repository.AbstractRepository) sono falliti: mostra il messaggio
     * critico, chiude la sessione in modo sicuro e reindirizza al login,
     * esattamente come previsto dalla postcondizione della sequenza
     * alternativa del caso d'uso.
     */
    public static void gestisciErroreCritico(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR, messaggio);
        alert.setTitle("Errore critico");
        alert.setHeaderText("Errore di comunicazione con il database");
        alert.showAndWait();
        SessioneCorrente.getIstanza().invalida();
        mostraSchermata("login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}