package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.AutenticazioneException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.example.gestioneafam.repository.DbmsException;

/**
 * Boundary della fase 2 del caso d'uso 1.2 Login (verifica OTP).
 */
public class OtpBoundary {

    @FXML
    private TextField txtCodiceOtp;
    @FXML
    private Label lblErrore;

    private final GestoreAutenticazioneControl controller =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void initialize() {
        // Sequenza alternativa: se non c'e' un login in corso, si torna al login
        // (equivalente al controllo su session.getAttribute("utente_in_sospeso")
        // della bozza web).
        if (SessioneCorrente.getIstanza().getEmailInSospeso() == null) {
            MainApp.mostraSchermata("login");
        }
    }

    @FXML
    private void onVerifica() {
        try {
            controller.validaOtp(txtCodiceOtp.getText());
            MainApp.mostraSchermata("dashboard");
        } catch (AutenticazioneException e) {
            if (GestoreAutenticazioneControl.MESSAGGIO_CODICE_OTP_NON_VALIDO.equals(e.getMessage())) {
                // RAD §1.2, Seq. Alt. 4 (codice errato): il flusso "riprende
                // dal passo 4", restando sulla stessa schermata per un nuovo
                // tentativo.
                lblErrore.setText(e.getMessage());
                lblErrore.setVisible(true);
                lblErrore.setManaged(true);
            } else {
                // RAD §1.2, Seq. Alt. 3 (OTP scaduto) e le verifiche
                // difensive di sessione: il flusso si interrompe e torna
                // alla pagina di login iniziale.
                SessioneCorrente.getIstanza().invalida();
                SessioneCorrente.getIstanza().setMessaggioAvviso(e.getMessage());
                MainApp.mostraSchermata("login");
            }
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onAnnulla() {
        SessioneCorrente.getIstanza().invalida();
        MainApp.mostraSchermata("login");
    }
}
