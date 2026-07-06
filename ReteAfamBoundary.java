package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.FederazioneException;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.GestoreFederazioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Boundary del macro caso d'uso Comunicazione tra Nodi AFAM (RAD §6,
 * requisito opzionale): 6.2 Gestisci Consenso Condivisione Esterna e 6.1
 * Consulta Identita' Digitale su Nodo Esterno. Vedi la nota metodologica
 * in {@link GestoreFederazioneControl} sulla simulazione mono-nodo.
 */
public class ReteAfamBoundary {

    @FXML private CheckBox chkConsenso;
    @FXML private Label lblStatoConsenso;
    @FXML private TextField txtFiltro;
    @FXML private ListView<Account> listaRisultati;
    @FXML private Label lblDettaglioNome;
    @FXML private Label lblDettaglioCurriculum;
    @FXML private Label lblEsito;

    private final GestoreFederazioneControl federazioneControl =
            new GestoreFederazioneControl(new AccountRepository());
    private final GestoreAutenticazioneControl authControl =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void initialize() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account == null) {
            MainApp.mostraSchermata("login");
            return;
        }
        boolean concesso = account.getConsenso() != null && "CONCESSO".equals(account.getConsenso().getStatoConsenso());
        chkConsenso.setSelected(concesso);
        aggiornaLabelConsenso(concesso);

        listaRisultati.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getProfilo().getNome() + " " + item.getProfilo().getCognome());
            }
        });
        listaRisultati.getSelectionModel().selectedItemProperty().addListener((obs, vecchio, nuovo) -> {
            if (nuovo != null) {
                mostraDettaglio(nuovo);
            }
        });
    }

    @FXML
    private void onSalvaConsenso() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            federazioneControl.aggiornaConsenso(account, chkConsenso.isSelected());
            aggiornaLabelConsenso(chkConsenso.isSelected());
            mostraEsito("Consenso aggiornato.", true);
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onCerca() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            List<Account> risultati = federazioneControl.cercaProfili(account, txtFiltro.getText());
            listaRisultati.setItems(FXCollections.observableArrayList(risultati));
            lblDettaglioNome.setText("Seleziona un profilo dai risultati per consultarlo in sola lettura.");
            lblDettaglioCurriculum.setText("");
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void mostraDettaglio(Account risultato) {
        try {
            Account confermato = federazioneControl.consultaIdentita(risultato.getIdAccount());
            lblDettaglioNome.setText(
                    "MODALITA' SOLA LETTURA — " + confermato.getProfilo().getNome() + " " + confermato.getProfilo().getCognome());
            lblDettaglioCurriculum.setText(confermato.getProfilo().getCurriculumPath() != null
                    ? "Curriculum disponibile: " + confermato.getProfilo().getCurriculumPath()
                    : "Nessun curriculum pubblicato.");
        } catch (FederazioneException e) {
            mostraErrore(e.getMessage());
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void aggiornaLabelConsenso(boolean concesso) {
        lblStatoConsenso.setText(concesso
                ? "Il tuo profilo e' attualmente consultabile dagli altri nodi AFAM della rete condivisa."
                : "Il tuo profilo NON e' consultabile da altri nodi AFAM.");
    }

    private void mostraEsito(String messaggio, boolean successo) {
        lblEsito.getStyleClass().setAll(successo ? "success-label" : "error-label");
        lblEsito.setText(messaggio);
        lblEsito.setVisible(true);
        lblEsito.setManaged(true);
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING, messaggio, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
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
}
