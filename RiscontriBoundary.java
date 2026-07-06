package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.GestoreVisualizzazioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Visualizzazione;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.repository.LinkRepository;
import com.example.gestioneafam.repository.VisualizzazioneRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Boundary del caso d'uso 5.2 Consulta Riscontri di Visualizzazione (RAD
 * §5.2): mostra, per ciascun link dello studente, il numero di
 * visualizzazioni registrate e la data/ora dell'ultimo accesso. Il
 * riscontro e' mantenuto a livello generale, senza alcun dato tecnico sul
 * visitatore esterno (coerente con la scelta di privacy del RAD).
 */
public class RiscontriBoundary {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<RiscontroRiga> tabellaRiscontri;
    @FXML private Label lblNessunRiscontro;

    private final GestoreVisualizzazioneControl visualizzazioneControl =
            new GestoreVisualizzazioneControl(new LinkRepository(), new VisualizzazioneRepository());
    private final GestoreAutenticazioneControl authControl =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void initialize() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account == null) {
            MainApp.mostraSchermata("login");
            return;
        }
        configuraTabella();
        aggiornaRiscontri(account);
    }

    @SuppressWarnings("unchecked")
    private void configuraTabella() {
        TableColumn<RiscontroRiga, String> colLink = new TableColumn<>("Link di Condivisione");
        colLink.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().url));
        colLink.setPrefWidth(320);

        TableColumn<RiscontroRiga, String> colNumero = new TableColumn<>("Visualizzazioni");
        colNumero.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(d.getValue().numeroVisualizzazioni)));

        TableColumn<RiscontroRiga, String> colUltimoAccesso = new TableColumn<>("Ultimo Accesso");
        colUltimoAccesso.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().ultimoAccesso.format(FORMATO_DATA)));

        tabellaRiscontri.getColumns().setAll(colLink, colNumero, colUltimoAccesso);
    }

    private void aggiornaRiscontri(Account account) {
        try {
            List<Visualizzazione> visualizzazioni = visualizzazioneControl.consultaRiscontri(account);
            if (visualizzazioni.isEmpty()) {
                // Sequenza alternativa 5.2.1: nessun riscontro disponibile.
                lblNessunRiscontro.setText("Nessuna visualizzazione registrata.");
                lblNessunRiscontro.setVisible(true);
                lblNessunRiscontro.setManaged(true);
                tabellaRiscontri.setItems(FXCollections.observableArrayList());
                return;
            }
            lblNessunRiscontro.setVisible(false);
            lblNessunRiscontro.setManaged(false);

            Map<String, List<Visualizzazione>> perLink = new LinkedHashMap<>();
            for (Visualizzazione v : visualizzazioni) {
                perLink.computeIfAbsent(v.getLink().getUrl(), k -> new ArrayList<>()).add(v);
            }
            List<RiscontroRiga> righe = new ArrayList<>();
            for (Map.Entry<String, List<Visualizzazione>> entry : perLink.entrySet()) {
                LocalDateTime ultimoAccesso = entry.getValue().stream()
                        .map(Visualizzazione::getDataOra)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                righe.add(new RiscontroRiga(entry.getKey(), entry.getValue().size(), ultimoAccesso));
            }
            tabellaRiscontri.setItems(FXCollections.observableArrayList(righe));
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

    /** Riga aggregata per la tabella: non e' un'entity JPA, solo una vista dei dati per la UI. */
    private static class RiscontroRiga {
        final String url;
        final int numeroVisualizzazioni;
        final LocalDateTime ultimoAccesso;

        RiscontroRiga(String url, int numeroVisualizzazioni, LocalDateTime ultimoAccesso) {
            this.url = url;
            this.numeroVisualizzazioni = numeroVisualizzazioni;
            this.ultimoAccesso = ultimoAccesso;
        }
    }
}
