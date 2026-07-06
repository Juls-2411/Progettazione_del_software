package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.AutenticazioneException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.example.gestioneafam.repository.DbmsException;

/**
 * Boundary del caso d'uso 1.1 Registrazione (RAD). Nella bozza web questa
 * pagina esisteva solo come GET statico, senza logica di salvataggio: qui
 * viene implementato per la prima volta il flusso completo descritto nel RAD
 * (validazione, controllo unicita' email/codice fiscale, creazione account).
 */
public class RegistrazioneBoundary {

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtCognome;
    @FXML
    private TextField txtCodiceFiscale;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblEsito;

    private final GestoreAutenticazioneControl controller =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void onRegistra() {
        try {
            controller.registra(
                    txtNome.getText(),
                    txtCognome.getText(),
                    txtCodiceFiscale.getText(),
                    txtEmail.getText(),
                    txtPassword.getText());

            lblEsito.getStyleClass().setAll("success-label");
            mostraMessaggio("Registrazione avvenuta con successo! Ora puoi accedere.");
        } catch (AutenticazioneException e) {
            lblEsito.getStyleClass().setAll("error-label");
            mostraMessaggio(e.getMessage());
        }catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onTornaAlLogin() {
        MainApp.mostraSchermata("login");
    }

    private void mostraMessaggio(String messaggio) {
        lblEsito.setText(messaggio);
        lblEsito.setVisible(true);
        lblEsito.setManaged(true);
    }
}
