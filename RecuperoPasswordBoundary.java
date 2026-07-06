package com.example.gestioneafam.boundary;

import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.AutenticazioneException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.example.gestioneafam.repository.DbmsException;

/**
 * Boundary del caso d'uso 1.3 Recupero Password (RAD).
 * I tre "step" (email, codice, nuova password) corrispondono alle tre
 * schermate separate descritte nel flusso del RAD; qui sono realizzati
 * come sezioni della stessa vista, mostrate/nascoste in sequenza.
 */
public class RecuperoPasswordBoundary {

    @FXML
    private VBox stepEmail;
    @FXML
    private VBox stepCodice;
    @FXML
    private VBox stepNuovaPassword;

    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtCodice;
    @FXML
    private PasswordField txtNuovaPassword;
    @FXML
    private PasswordField txtConfermaPassword;
    @FXML
    private Label lblErrore;

    private final GestoreAutenticazioneControl controller =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void onInviaCodice() {
        try {
            controller.richiediRecuperoPassword(txtEmail.getText());
            passaAllo(stepCodice);
        } catch (AutenticazioneException e) {
            mostraErrore(e.getMessage());
        }catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onVerificaCodice() {
        try {
            controller.validaCodiceRecupero(txtCodice.getText());
            passaAllo(stepNuovaPassword);
        } catch (AutenticazioneException e) {
            mostraErrore(e.getMessage());
        }catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onModificaPassword() {
        try {
            controller.modificaPassword(txtNuovaPassword.getText(), txtConfermaPassword.getText());
            SessioneCorrente.getIstanza().setMessaggioAvviso("Password Modificata Con Successo!", true);
            MainApp.mostraSchermata("login");
        } catch (AutenticazioneException e) {
            mostraErrore(e.getMessage());
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onTornaAlLogin() {
        MainApp.mostraSchermata("login");
    }

    private void passaAllo(VBox step) {
        nascondiErrore();
        stepEmail.setVisible(step == stepEmail);
        stepEmail.setManaged(step == stepEmail);
        stepCodice.setVisible(step == stepCodice);
        stepCodice.setManaged(step == stepCodice);
        stepNuovaPassword.setVisible(step == stepNuovaPassword);
        stepNuovaPassword.setManaged(step == stepNuovaPassword);
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
