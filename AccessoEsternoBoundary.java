package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.GestoreVisualizzazioneControl;
import com.example.gestioneafam.control.VisualizzazioneException;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.entity.Link;
import com.example.gestioneafam.repository.LinkRepository;
import com.example.gestioneafam.repository.VisualizzazioneRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Boundary del caso d'uso 5.1 Visualizza Contenuti Condivisi (RAD §5.1):
 * a differenza di tutte le altre schermate, l'attore primario e' l'Utente
 * Esterno, che non possiede un account e non e' autenticato. Corrisponde
 * al mock-up "Accesso Esterno" del RAD (§3.4.5): richiesta password (se
 * prevista), disclaimer sul diritto d'autore, visualizzatore dei contenuti.
 */
public class AccessoEsternoBoundary {

    @FXML private VBox stepUrl;
    @FXML private VBox stepPassword;
    @FXML private VBox stepDisclaimer;
    @FXML private VBox stepContenuti;
    @FXML private javafx.scene.control.ScrollPane stepContenutiScroll;

    @FXML private TextField txtUrl;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkAccettaDisclaimer;
    @FXML private Button btnVisualizzaContenuti;
    @FXML private VBox listaContenuti;
    @FXML private Label lblErrore;

    private Link linkCorrente;

    private final GestoreVisualizzazioneControl visualizzazioneControl =
            new GestoreVisualizzazioneControl(new LinkRepository(), new VisualizzazioneRepository());

    @FXML
    private void initialize() {
        chkAccettaDisclaimer.selectedProperty().addListener(
                (obs, vecchio, nuovo) -> btnVisualizzaContenuti.setDisable(!nuovo));
    }

    @FXML
    private void onApriLink() {
        nascondiErrore();
        try {
            linkCorrente = visualizzazioneControl.apriLink(txtUrl.getText().trim());
            if (linkCorrente.getPasswordHash() != null) {
                passaAllo(stepPassword);
            } else {
                passaAllo(stepDisclaimer);
            }
        } catch (VisualizzazioneException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onSbloccaConPassword() {
        nascondiErrore();
        try {
            visualizzazioneControl.verificaPassword(linkCorrente, txtPassword.getText());
            passaAllo(stepDisclaimer);
        } catch (VisualizzazioneException e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onVisualizzaContenuti() {
        List<Contenuto> contenuti = visualizzazioneControl.registraAccessoERestituisciContenuti(linkCorrente);
        listaContenuti.getChildren().clear();
        if (contenuti.isEmpty()) {
            listaContenuti.getChildren().add(new Label("Nessun contenuto e' al momento disponibile tramite questo link."));
        } else {
            for (Contenuto c : contenuti) {
                listaContenuti.getChildren().add(creaRigaContenuto(c));
            }
        }
        passaAllo(stepContenuti);
    }

    private VBox creaRigaContenuto(Contenuto c) {
        VBox riga = new VBox(4);
        riga.getStyleClass().add("card");
        Label titolo = new Label(c.getTitolo() + "  [" + c.getTipo() + "]");
        titolo.getStyleClass().add("subtitle-label");
        Label didascalia = new Label(c.getDidascalia() != null ? c.getDidascalia() : "");
        didascalia.setWrapText(true);
        Button apri = new Button("Apri");
        apri.getStyleClass().add("btn-secondary");
        apri.setOnAction(e -> apriContenuto(c));
        riga.getChildren().addAll(titolo, didascalia, apri);
        return riga;
    }

    private void apriContenuto(Contenuto c) {
        try {
            if ("LINK_ESTERNO".equals(c.getTipo())) {
                Desktop.getDesktop().browse(new URI(c.getUrlEsterno()));
            } else if (c.getFile() != null) {
                Desktop.getDesktop().open(new File(c.getFile().getPercorso()));
            }
        } catch (Exception ignored) {
            // Apertura best-effort: se il sistema operativo non ha un'applicazione
            // associata, l'Utente Esterno vede semplicemente che nulla si apre.
        }
    }

    @FXML
    private void onTornaAlLogin() {
        MainApp.mostraSchermata("login");
    }

    private void passaAllo(VBox step) {
        stepUrl.setVisible(step == stepUrl);
        stepUrl.setManaged(step == stepUrl);
        stepPassword.setVisible(step == stepPassword);
        stepPassword.setManaged(step == stepPassword);
        stepDisclaimer.setVisible(step == stepDisclaimer);
        stepDisclaimer.setManaged(step == stepDisclaimer);
        stepContenuti.setVisible(step == stepContenuti);
        stepContenuti.setManaged(step == stepContenuti);
        stepContenutiScroll.setVisible(step == stepContenuti);
        stepContenutiScroll.setManaged(step == stepContenuti);
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
