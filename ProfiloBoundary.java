package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.AutenticazioneException;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.GestoreProfiloControl;
import com.example.gestioneafam.control.ProfiloException;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Boundary del macro caso d'uso Gestione Profilo (RAD §2): raccoglie in
 * un'unica schermata, coerentemente con il mock-up "Editor del Profilo"
 * (§3.4.5), i casi d'uso 2.1/2.2 (dati anagrafici), 2.3 (immagine profilo),
 * 2.4 (modifica password) e 2.5 (cancellazione account).
 */
public class ProfiloBoundary {

    @FXML private ImageView imgProfilo;
    @FXML private TextField txtNome;
    @FXML private TextField txtCognome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCodiceFiscale;
    @FXML private TextField txtIndirizzo;
    @FXML private TextField txtRecapito;
    @FXML private Label lblCurriculum;
    @FXML private Label lblEsitoDati;

    @FXML private PasswordField txtPasswordAttuale;
    @FXML private PasswordField txtNuovaPassword;
    @FXML private PasswordField txtConfermaPassword;
    @FXML private Label lblEsitoPassword;

    private File curriculumSelezionato;

    private final AccountRepository accountRepository = new AccountRepository();
    private final GestoreProfiloControl profiloControl = new GestoreProfiloControl(accountRepository);
    private final GestoreAutenticazioneControl authControl =
            new GestoreAutenticazioneControl(accountRepository, new ConsoleMailService());

    @FXML
    private void initialize() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account == null) {
            MainApp.mostraSchermata("login");
            return;
        }
        popolaCampi(account);
    }

    private void popolaCampi(Account account) {
        txtNome.setText(account.getProfilo().getNome());
        txtCognome.setText(account.getProfilo().getCognome());
        txtEmail.setText(account.getProfilo().getEmail());
        txtCodiceFiscale.setText(account.getProfilo().getCodiceFiscale());
        txtIndirizzo.setText(account.getProfilo().getIndirizzo());
        txtRecapito.setText(account.getProfilo().getRecapito());
        lblCurriculum.setText(account.getProfilo().getCurriculumPath() != null
                ? "Curriculum caricato: " + new File(account.getProfilo().getCurriculumPath()).getName()
                : "Nessun curriculum caricato");
        if (account.getProfilo().getImmagineProfiloPath() != null) {
            File file = new File(account.getProfilo().getImmagineProfiloPath());
            if (file.exists()) {
                imgProfilo.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2.1/2.2 Inserisci/Modifica Dati Anagrafici
    // ------------------------------------------------------------------

    @FXML
    private void onScegliCurriculum() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleziona il curriculum (PDF)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File selezionato = chooser.showOpenDialog(imgProfilo.getScene().getWindow());
        if (selezionato != null) {
            curriculumSelezionato = selezionato;
            lblCurriculum.setText("Curriculum selezionato: " + selezionato.getName() + " (da salvare)");
        }
    }

    @FXML
    private void onSalvaDatiAnagrafici() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            Path curriculumPath = curriculumSelezionato != null ? curriculumSelezionato.toPath() : null;
            profiloControl.salvaDatiAnagrafici(
                    account, txtNome.getText(), txtCognome.getText(),
                    txtIndirizzo.getText(), txtRecapito.getText(), curriculumPath);
            curriculumSelezionato = null;
            mostraEsito(lblEsitoDati, "Dati modificati correttamente.", true);
            popolaCampi(account);
        } catch (ProfiloException e) {
            mostraEsito(lblEsitoDati, e.getMessage(), false);
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 2.3 Gestione Immagine Profilo
    // ------------------------------------------------------------------

    @FXML
    public void onCambiaImmagine() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Immagini", "*.jpg", "*.png"));

        File fileImmagineSelezionato = fileChooser.showOpenDialog(txtNome.getScene().getWindow());
        if (fileImmagineSelezionato == null) {
            return;
        }

        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            // 1. Valida (formato JPG/PNG, max 2MB - RAD §2.3) e salva tramite il Control
            profiloControl.aggiornaImmagineProfilo(account, fileImmagineSelezionato.toPath());

            // 2. Solo dopo il salvataggio, aggiorna l'interfaccia con la nuova immagine
            Image nuovaImmagine = new Image(fileImmagineSelezionato.toURI().toString());
            imgProfilo.setImage(nuovaImmagine);

            mostraInfo("Successo", "Immagine del profilo aggiornata correttamente.");
        } catch (ProfiloException e) {
            // RAD §2.3, Sequenza alternativa 1: formato non valido o dimensione eccessiva
            mostraErrore("Errore Immagine", e.getMessage());
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 2.4 Modifica Password
    // ------------------------------------------------------------------

    @FXML
    private void onAggiornaPassword() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            authControl.aggiornaPassword(account, txtPasswordAttuale.getText(),
                    txtNuovaPassword.getText(), txtConfermaPassword.getText());
            txtPasswordAttuale.clear();
            txtNuovaPassword.clear();
            txtConfermaPassword.clear();
            mostraEsito(lblEsitoPassword, "Password aggiornata con successo.", true);
        } catch (AutenticazioneException e) {
            mostraEsito(lblEsitoPassword, e.getMessage(), false);
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 2.5 Cancellazione Dati Account
    // ------------------------------------------------------------------

    @FXML
    private void onEliminaAccount() {
        // RAD §3.3.1.2: le operazioni irreversibili richiedono conferma esplicita.
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION,
                "Questa operazione eliminera' definitivamente il tuo account e tutti i dati associati "
                        + "(profilo, contenuti, link di condivisione). L'operazione non e' reversibile. Continuare?",
                ButtonType.YES, ButtonType.NO);
        conferma.setTitle("Elimina account");
        conferma.setHeaderText("Conferma eliminazione definitiva");
        Optional<ButtonType> risposta = conferma.showAndWait();
        if (risposta.isEmpty() || risposta.get() != ButtonType.YES) {
            return;
        }
        try {
            profiloControl.eliminaAccount(SessioneCorrente.getIstanza().getAccountAutenticato());
            SessioneCorrente.getIstanza().invalida();
            Alert esito = new Alert(Alert.AlertType.INFORMATION, "Account eliminato con successo.", ButtonType.OK);
            esito.setHeaderText(null);
            esito.showAndWait();
            MainApp.mostraSchermata("login");
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onTornaAllaDashboard() {
        MainApp.mostraSchermata("dashboard");
    }

    @FXML
    private void onLogout() {
        authControl.logout();
        MainApp.mostraSchermata("login");
    }

    private void mostraEsito(Label label, String messaggio, boolean successo) {
        label.getStyleClass().setAll(successo ? "success-label" : "error-label");
        label.setText(messaggio);
        label.setVisible(true);
        label.setManaged(true);
    }
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR, messaggio);
        alert.setTitle(titolo);
        alert.setHeaderText(null); // Per pulizia grafica
        alert.showAndWait();
    }

    private void mostraInfo(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, messaggio);
        alert.setTitle(titolo);
        alert.setHeaderText(null); // Per pulizia grafica
        alert.showAndWait();
    }
}
