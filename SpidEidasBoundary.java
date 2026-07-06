package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.AutenticazioneException;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreSpidEidasControl;
import com.example.gestioneafam.control.GestoreSpidEidasControl.IdentitaTest;
import com.example.gestioneafam.repository.AccountRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Boundary del caso d'uso 1.4 Login con SPID/eIDAS (RAD §1.4). Realizza,
 * con un Identity Provider simulato (vedi la nota metodologica in
 * {@link GestoreSpidEidasControl}), il percorso: scelta SPID/eIDAS
 * (sequenza alternativa 1) → selezione Identity Provider/Paese → "login"
 * presso il provider → associazione al profilo locale e apertura sessione.
 */
public class SpidEidasBoundary {

    @FXML private VBox stepScelta;
    @FXML private VBox stepProvider;
    @FXML private VBox stepLogin;

    @FXML private Label lblTitoloProvider;
    @FXML private ListView<String> listaProvider;

    @FXML private ListView<IdentitaTest> listaIdentita;
    @FXML private CheckBox chkSimulaFallimento;
    @FXML private CheckBox chkSimulaTimeout;

    @FXML private Label lblErrore;

    private boolean modalitaSpid = true;
    private String providerSelezionato;

    private final GestoreSpidEidasControl spidEidasControl = new GestoreSpidEidasControl(new AccountRepository());

    @FXML
    private void initialize() {
        passaAllo(stepScelta);
    }

    // ------------------------------------------------------------------
    // Passo 1 (+ sequenza alternativa 1: scelta SPID oppure eIDAS)
    // ------------------------------------------------------------------

    @FXML
    private void onSceltaSpid() {
        modalitaSpid = true;
        mostraSelezioneProvider();
    }

    @FXML
    private void onSceltaEidas() {
        modalitaSpid = false;
        mostraSelezioneProvider();
    }

    private void mostraSelezioneProvider() {
        nascondiErrore();
        if (modalitaSpid) {
            lblTitoloProvider.setText("Seleziona il tuo Identity Provider");
            listaProvider.setItems(FXCollections.observableArrayList(spidEidasControl.elencaIdentityProviderSpid()));
        } else {
            lblTitoloProvider.setText("Seleziona il Paese di provenienza");
            listaProvider.setItems(FXCollections.observableArrayList(spidEidasControl.elencaPaesiEidas()));
        }
        passaAllo(stepProvider);
    }

    // ------------------------------------------------------------------
    // Passi 2-4: selezione del provider/Paese, reindirizzamento (simulato)
    // ------------------------------------------------------------------

    @FXML
    private void onContinuaProvider() {
        nascondiErrore();
        String selezione = listaProvider.getSelectionModel().getSelectedItem();
        if (selezione == null) {
            mostraErrore(modalitaSpid ? "Seleziona un Identity Provider per continuare."
                    : "Seleziona un Paese per continuare.");
            return;
        }
        providerSelezionato = modalitaSpid ? selezione : ("eIDAS-" + selezione);

        // Identita' disponibili presso l'IdP simulato: per coerenza dimostrativa,
        // le identita' "spid-test-*" sono proposte per SPID e le "eidas-test-*"
        // per eIDAS, cosi' come un vero IdP nazionale o estero certificherebbe
        // solo identita' del proprio ambito.
        String prefisso = modalitaSpid ? "spid-test-" : "eidas-test-";
        List<IdentitaTest> disponibili = spidEidasControl.elencaIdentitaDiTest().stream()
                .filter(i -> i.identificativoEsterno().startsWith(prefisso))
                .toList();
        listaIdentita.setItems(FXCollections.observableArrayList(disponibili));
        chkSimulaFallimento.setSelected(false);
        chkSimulaTimeout.setSelected(false);
        passaAllo(stepLogin);
    }

    @FXML
    private void onIndietroDaProvider() {
        passaAllo(stepScelta);
    }

    // ------------------------------------------------------------------
    // Passi 5-7 (+ sequenze alternative 3 e 4): "login" presso il provider
    // e associazione al profilo locale
    // ------------------------------------------------------------------

    @FXML
    private void onAccedi() {
        nascondiErrore();
        IdentitaTest identita = listaIdentita.getSelectionModel().getSelectedItem();
        if (identita == null && !chkSimulaFallimento.isSelected() && !chkSimulaTimeout.isSelected()) {
            mostraErrore("Seleziona un'identita' di test (o simula un errore) per continuare.");
            return;
        }
        try {
            spidEidasControl.autenticaConIdentitaCertificata(
                    identita, providerSelezionato, chkSimulaFallimento.isSelected(), chkSimulaTimeout.isSelected());
            MainApp.mostraSchermata("dashboard");
        } catch (AutenticazioneException e) {
            // Postcondizione delle sequenze alternative 3/4: il Sistema mostra
            // l'errore e torna alla schermata iniziale di login.
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            MainApp.mostraSchermata("login");
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onIndietroDaLogin() {
        passaAllo(stepProvider);
    }

    @FXML
    private void onTornaAlLogin() {
        MainApp.mostraSchermata("login");
    }

    private void passaAllo(VBox step) {
        stepScelta.setVisible(step == stepScelta);
        stepScelta.setManaged(step == stepScelta);
        stepProvider.setVisible(step == stepProvider);
        stepProvider.setManaged(step == stepProvider);
        stepLogin.setVisible(step == stepLogin);
        stepLogin.setManaged(step == stepLogin);
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
