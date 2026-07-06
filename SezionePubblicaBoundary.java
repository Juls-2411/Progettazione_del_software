package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.repository.ContenutoRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Boundary della Sezione Pubblica: elenco, consultabile da qualunque
 * Studente AFAM autenticato del nodo, dei contenuti che i loro
 * proprietari hanno marcato come pubblici (RAD §3.2, requisito
 * funzionale n. 8 di Gestione Contenuti, e mock-up "Sezione Pubblica"
 * §3.4.5). E' una consultazione di sola lettura: non fa parte dei casi
 * d'uso con scrittura persistente e non e' quindi soggetta al caso
 * d'uso 7.1 Errore Comunicazione DBMS se non per la sola lettura.
 */
public class SezionePubblicaBoundary {

    @FXML private TextField txtFiltro;
    @FXML private TableView<Contenuto> tabellaPubblici;

    private final ContenutoRepository contenutoRepository = new ContenutoRepository();
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
        aggiornaElenco();
    }

    @SuppressWarnings("unchecked")
    private void configuraTabella() {
        TableColumn<Contenuto, String> colTitolo = new TableColumn<>("Titolo");
        colTitolo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTitolo()));
        colTitolo.setPrefWidth(260);

        TableColumn<Contenuto, String> colAutore = new TableColumn<>("Studente");
        colAutore.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getAccount().getProfilo().getNome() + " " + d.getValue().getAccount().getProfilo().getCognome()));

        TableColumn<Contenuto, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTipo()));

        TableColumn<Contenuto, String> colDidascalia = new TableColumn<>("Didascalia");
        colDidascalia.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDidascalia() == null ? "" : d.getValue().getDidascalia()));
        colDidascalia.setPrefWidth(280);

        tabellaPubblici.getColumns().setAll(colTitolo, colAutore, colTipo, colDidascalia);
    }

    @FXML
    private void onFiltra() {
        aggiornaElenco();
    }

    private void aggiornaElenco() {
        try {
            List<Contenuto> pubblici = contenutoRepository.findPubbliciAttivi();
            String filtro = txtFiltro.getText() == null ? "" : txtFiltro.getText().trim().toLowerCase();
            List<Contenuto> filtrati = filtro.isEmpty()
                    ? pubblici
                    : pubblici.stream()
                        .filter(c -> c.getTitolo().toLowerCase().contains(filtro)
                                || (c.getAccount().getProfilo().getNome() + " " + c.getAccount().getProfilo().getCognome())
                                        .toLowerCase().contains(filtro))
                        .toList();
            tabellaPubblici.setItems(FXCollections.observableArrayList(filtrati));
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
}
