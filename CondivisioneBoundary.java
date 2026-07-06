package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.CondivisioneException;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.GestoreCondivisioneControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.entity.Link;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.repository.ContenutoRepository;
import com.example.gestioneafam.repository.LinkRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Boundary del macro caso d'uso Gestione Condivisione (RAD §4), corrispondente
 * al mock-up "Creazione Link" del RAD (§3.4.5).
 */
public class CondivisioneBoundary {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ListView<Contenuto> listaContenutiSelezionabili;
    @FXML private DatePicker dateScadenza;
    @FXML private PasswordField txtPasswordLink;
    @FXML private Label lblEsitoCreazione;
    @FXML private VBox boxLinkGenerato;
    @FXML private Label lblLinkGenerato;
    @FXML private TableView<Link> tabellaLink;

    private final Set<Contenuto> contenutiSelezionati = new HashSet<>();
    private String ultimoLinkGenerato;

    private final ContenutoRepository contenutoRepository = new ContenutoRepository();
    private final LinkRepository linkRepository = new LinkRepository();
    private final GestoreCondivisioneControl condivisioneControl = new GestoreCondivisioneControl(linkRepository);
    private final GestoreAutenticazioneControl authControl =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    @FXML
    private void initialize() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account == null) {
            MainApp.mostraSchermata("login");
            return;
        }
        configuraListaContenuti();
        configuraTabellaLink();
        aggiornaListaContenuti();
        aggiornaTabellaLink();
    }

    private void configuraListaContenuti() {
        listaContenutiSelezionabili.setCellFactory(lv -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Contenuto contenuto, boolean empty) {
                super.updateItem(contenuto, empty);
                if (empty || contenuto == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                checkBox.setText(contenuto.getTitolo() + "  (" + contenuto.getTipo() + ")");
                checkBox.setSelected(contenutiSelezionati.contains(contenuto));
                checkBox.setOnAction(e -> {
                    if (checkBox.isSelected()) {
                        contenutiSelezionati.add(contenuto);
                    } else {
                        contenutiSelezionati.remove(contenuto);
                    }
                });
                setGraphic(checkBox);
                setText(null);
            }
        });
    }

    private void aggiornaListaContenuti() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        List<Contenuto> attivi = contenutoRepository.findByAccount(account).stream()
                .filter(c -> "ATTIVO".equals(c.getStatoArchiviazione()))
                .toList();
        listaContenutiSelezionabili.setItems(FXCollections.observableArrayList(attivi));
    }

    @SuppressWarnings("unchecked")
    private void configuraTabellaLink() {
        TableColumn<Link, String> colUrl = new TableColumn<>("URL");
        colUrl.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUrl()));
        colUrl.setPrefWidth(280);

        TableColumn<Link, String> colContenuti = new TableColumn<>("Contenuti");
        colContenuti.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(d.getValue().getContenutiSelezionati().size())));

        TableColumn<Link, String> colScadenza = new TableColumn<>("Scadenza");
        colScadenza.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getScadenza() == null ? "Nessuna scadenza" : d.getValue().getScadenza().format(FORMATO_DATA)));

        TableColumn<Link, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStato()));

        TableColumn<Link, Void> colAzioni = new TableColumn<>("Azioni");
        colAzioni.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(creaMenuAzioniLink(getTableView().getItems().get(getIndex())));
                }
            }
        });

        tabellaLink.getColumns().setAll(colUrl, colContenuti, colScadenza, colStato, colAzioni);
    }

    private MenuButton creaMenuAzioniLink(Link link) {
        MenuButton menu = new MenuButton("...");

        MenuItem copia = new MenuItem("Copia Link");
        copia.setOnAction(e -> copiaNegliAppunti(link.getUrl()));
        menu.getItems().add(copia);

        MenuItem modifica = new MenuItem("Modifica Scadenza");
        modifica.setOnAction(e -> modificaScadenza(link));
        menu.getItems().add(modifica);

        MenuItem revoca = new MenuItem("Revoca");
        revoca.setDisable(!link.isValido());
        revoca.setOnAction(e -> revocaLink(link));
        menu.getItems().add(revoca);

        return menu;
    }

    private void modificaScadenza(Link link) {
        TextInputDialog dialog = new TextInputDialog(
                link.getScadenza() != null ? link.getScadenza().toLocalDate().toString() : "");
        dialog.setTitle("Modifica Scadenza");
        dialog.setHeaderText("Inserisci la nuova data di scadenza (AAAA-MM-GG) o lascia vuoto per nessuna scadenza");
        Optional<String> risultato = dialog.showAndWait();
        if (risultato.isEmpty()) {
            return;
        }
        try {
            LocalDateTime nuovaScadenza = risultato.get().isBlank()
                    ? null
                    : java.time.LocalDate.parse(risultato.get()).atTime(23, 59);
            condivisioneControl.modificaOpzioni(link, nuovaScadenza, null);
            aggiornaTabellaLink();
        } catch (CondivisioneException e) {
            mostraErrore(e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            mostraErrore("Formato data non valido: usare AAAA-MM-GG.");
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void revocaLink(Link link) {
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION,
                "Il link " + link.getUrl() + " diventera' immediatamente inutilizzabile. Continuare?",
                ButtonType.YES, ButtonType.NO);
        conferma.setTitle("Revoca link");
        conferma.setHeaderText("Conferma revoca");
        Optional<ButtonType> risposta = conferma.showAndWait();
        if (risposta.isEmpty() || risposta.get() != ButtonType.YES) {
            return;
        }
        try {
            condivisioneControl.revocaLink(link);
            aggiornaTabellaLink();
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void aggiornaTabellaLink() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            tabellaLink.setItems(FXCollections.observableArrayList(condivisioneControl.elencaLink(account)));
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onCreaLink() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            LocalDateTime scadenza = dateScadenza.getValue() != null
                    ? dateScadenza.getValue().atTime(23, 59)
                    : null;
            Link link = condivisioneControl.creaLink(account, List.copyOf(contenutiSelezionati),
                    scadenza, txtPasswordLink.getText());

            ultimoLinkGenerato = link.getUrl();
            lblLinkGenerato.setText(link.getUrl());
            boxLinkGenerato.setVisible(true);
            boxLinkGenerato.setManaged(true);

            contenutiSelezionati.clear();
            dateScadenza.setValue(null);
            txtPasswordLink.clear();
            listaContenutiSelezionabili.refresh();
            lblEsitoCreazione.setVisible(false);
            lblEsitoCreazione.setManaged(false);
            aggiornaTabellaLink();
        } catch (CondivisioneException e) {
            lblEsitoCreazione.getStyleClass().setAll("error-label");
            lblEsitoCreazione.setText(e.getMessage());
            lblEsitoCreazione.setVisible(true);
            lblEsitoCreazione.setManaged(true);
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onCopiaLinkGenerato() {
        if (ultimoLinkGenerato != null) {
            copiaNegliAppunti(ultimoLinkGenerato);
        }
    }

    private void copiaNegliAppunti(String testo) {
        ClipboardContent content = new ClipboardContent();
        content.putString(testo);
        Clipboard.getSystemClipboard().setContent(content);
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
