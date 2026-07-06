package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Boundary della Home/Dashboard (sostituisce DashboardController).
 *
 * Ogni voce del menu laterale corrisponde ora a un macro caso d'uso
 * completamente implementato, seguendo lo schema Boundary -> Control ->
 * Repository -> Entity gia' usato per l'Autenticazione: Gestione Profilo,
 * Gestione Contenuti, Gestione Condivisione, Sezione Pubblica, Riscontri
 * di Visualizzazione e Rete AFAM (requisito opzionale).
 */
public class DashboardBoundary {

    @FXML
    private Label lblBenvenuto;
    @FXML
    private Label lblContenuto;

    private final GestoreAutenticazioneControl authControl =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void initialize() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account == null) {
            MainApp.mostraSchermata("login");
            return;
        }
        String nome = account.getProfilo() != null && account.getProfilo().getNome() != null
                ? account.getProfilo().getNome()
                : account.getCredenziali().getUsername();
        lblBenvenuto.setText("Benvenuta/o, " + nome);
    }

    @FXML
    private void onProfilo() {
        MainApp.mostraSchermata("profilo");
    }

    @FXML
    private void onContenuti() {
        MainApp.mostraSchermata("contenuti");
    }

    @FXML
    private void onCondivisioni() {
        MainApp.mostraSchermata("condivisione");
    }

    @FXML
    private void onSezionePubblica() {
        MainApp.mostraSchermata("sezione-pubblica");
    }

    @FXML
    private void onRiscontri() {
        MainApp.mostraSchermata("riscontri");
    }

    @FXML
    private void onReteAfam() {
        MainApp.mostraSchermata("rete-afam");
    }

    @FXML
    private void onLogout() {
        authControl.logout();
        MainApp.mostraSchermata("login");
    }
}
