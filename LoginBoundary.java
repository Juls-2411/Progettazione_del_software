package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.AutenticazioneException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.example.gestioneafam.repository.DbmsException;

/**
 * Boundary corrispondente a loginBoundary nel diagramma di sequenza
 * "1.2 Login" del RAD. Sostituisce AutenticazioneController.mostraPaginaLogin()
 * ed eseguiLogin(): al posto di un redirect HTTP, qui si cambia direttamente
 * la Scene mostrata nello Stage principale (vedi MainApp.mostraSchermata).
 */
public class LoginBoundary {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblErrore;

    private final GestoreAutenticazioneControl controller =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void initialize() {
        String avviso = SessioneCorrente.getIstanza().getEConsumaMessaggioAvviso();
        if (avviso != null) {
            boolean successo = SessioneCorrente.getIstanza().isMessaggioAvvisoSuccesso();
            lblErrore.getStyleClass().setAll(successo ? "success-label" : "error-label");
            mostraErrore(avviso);
        }
    }

    @FXML
    private void onAccedi() {
        nascondiErrore();
        try {
            controller.login(txtUsername.getText(), txtPassword.getText());
            MainApp.mostraSchermata("otp");
        } catch (AutenticazioneException e) {
            mostraErrore(e.getMessage());
        }catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onRegistrazione() {
        MainApp.mostraSchermata("registrazione");
    }

    @FXML
    private void onRecuperoPassword() {
        MainApp.mostraSchermata("recupero-password");
    }

    @FXML
    private void onAccediSpid() {
        MainApp.mostraSchermata("spid-eidas");
    }

    @FXML
    private void onAccessoEsterno() {
        MainApp.mostraSchermata("accesso-esterno");
    }

    private void mostraErrore(String messaggio) {
        lblErrore.setText(messaggio);
        lblErrore.setVisible(true);
        lblErrore.setManaged(true);
    }

    private void nascondiErrore() {
        lblErrore.setVisible(false);
        lblErrore.setManaged(false);
    }
}
