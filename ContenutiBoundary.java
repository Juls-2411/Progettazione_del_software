package com.example.gestioneafam.boundary;

import com.example.gestioneafam.MainApp;
import com.example.gestioneafam.control.ContenutoException;
import com.example.gestioneafam.repository.DbmsException;
import com.example.gestioneafam.control.GestoreAutenticazioneControl;
import com.example.gestioneafam.control.GestoreContenutiControl;
import com.example.gestioneafam.control.SessioneCorrente;
import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Cartella;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.repository.CartellaRepository;
import com.example.gestioneafam.repository.ContenutoRepository;
import com.example.gestioneafam.util.ConsoleMailService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Boundary del macro caso d'uso Gestione Contenuti (RAD §3), corrispondente
 * al mock-up "Area Contenuti" del RAD (§3.4.5).
 */
public class ContenutiBoundary {

    @FXML private ComboBox<Cartella> cmbFiltroCartella;
    @FXML private TextField txtNuovaCartella;

    @FXML private VBox formFile;
    @FXML private VBox formLink;
    @FXML private RadioButton rbAudio;
    @FXML private RadioButton rbVideo;
    @FXML private RadioButton rbSpartito;
    @FXML private Label lblFileSelezionato;
    @FXML private TextField txtTitoloFile;
    @FXML private TextField txtDidascaliaFile;
    @FXML private TextField txtAltFile;
    @FXML private Label lblEsitoFile;

    @FXML private TextField txtUrlLink;
    @FXML private TextField txtTitoloLink;
    @FXML private TextField txtDidascaliaLink;
    @FXML private TextField txtAltLink;
    @FXML private Label lblEsitoLink;

    @FXML private TableView<Contenuto> tabellaContenuti;

    private File fileSelezionato;

    private final ContenutoRepository contenutoRepository = new ContenutoRepository();
    private final CartellaRepository cartellaRepository = new CartellaRepository();
    private final GestoreContenutiControl contenutiControl =
            new GestoreContenutiControl(contenutoRepository, cartellaRepository);
    private final GestoreAutenticazioneControl authControl =
            new GestoreAutenticazioneControl(new AccountRepository(), new ConsoleMailService());

    private static final Cartella TUTTE_LE_CARTELLE = null; // sentinella per il filtro combo

    @FXML
    private void initialize() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account == null) {
            MainApp.mostraSchermata("login");
            return;
        }
        configuraTabella();
        configuraComboCartelle();
        aggiornaTabella();
    }

    private void configuraComboCartelle() {
        cmbFiltroCartella.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cartella cartella) {
                return cartella == null ? "Tutte le cartelle" : cartella.getNome();
            }

            @Override
            public Cartella fromString(String string) {
                return null;
            }
        });
        cmbFiltroCartella.setOnAction(e -> aggiornaTabella());
    }

    private void ricaricaComboCartelle() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        Cartella selezionata = cmbFiltroCartella.getValue();
        Long idSelezionata = selezionata == null ? null : selezionata.getIdCartella();
        List<Cartella> cartelle = contenutiControl.elencaCartelle(account);
        cmbFiltroCartella.setItems(FXCollections.observableArrayList(cartelle));
        cmbFiltroCartella.getItems().add(0, TUTTE_LE_CARTELLE);
        Cartella daRiselezionare = idSelezionata == null
                ? TUTTE_LE_CARTELLE
                : cartelle.stream().filter(c -> idSelezionata.equals(c.getIdCartella())).findFirst().orElse(TUTTE_LE_CARTELLE);
        cmbFiltroCartella.setValue(daRiselezionare);
    }

    @SuppressWarnings("unchecked")
    private void configuraTabella() {
        TableColumn<Contenuto, String> colTitolo = new TableColumn<>("Titolo");
        colTitolo.setCellValueFactory(new PropertyValueFactory<>("titolo"));

        TableColumn<Contenuto, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        TableColumn<Contenuto, String> colCartella = new TableColumn<>("Cartella");
        colCartella.setCellValueFactory(dati -> {
            Cartella c = dati.getValue().getCartella();
            return new javafx.beans.property.SimpleStringProperty(c == null ? "(root)" : c.getNome());
        });

        TableColumn<Contenuto, String> colVisibilita = new TableColumn<>("Visibilita'");
        colVisibilita.setCellValueFactory(new PropertyValueFactory<>("visibilita"));

        TableColumn<Contenuto, String> colStato = new TableColumn<>("Stato");
        colStato.setCellValueFactory(new PropertyValueFactory<>("statoArchiviazione"));

        TableColumn<Contenuto, Void> colAzioni = new TableColumn<>("Azioni");
        colAzioni.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Contenuto contenuto = getTableView().getItems().get(getIndex());
                    setGraphic(creaMenuAzioni(contenuto));
                }
            }
        });

        tabellaContenuti.getColumns().setAll(colTitolo, colTipo, colCartella, colVisibilita, colStato, colAzioni);
    }

    private MenuButton creaMenuAzioni(Contenuto contenuto) {
        MenuButton menu = new MenuButton("...");

        MenuItem pubblico = new MenuItem("Imposta Pubblico");
        pubblico.setOnAction(e -> impostaVisibilita(contenuto, "PUBBLICO"));
        MenuItem privato = new MenuItem("Imposta Privato");
        privato.setOnAction(e -> impostaVisibilita(contenuto, "PRIVATO"));
        MenuItem condiviso = new MenuItem("Imposta Condiviso via Link");
        condiviso.setOnAction(e -> impostaVisibilita(contenuto, "CONDIVISO_VIA_LINK"));

        menu.getItems().addAll(pubblico, privato, condiviso, new SeparatorMenuItem());

        MenuItem organizza = new MenuItem("Sposta in: (root)");
        organizza.setOnAction(e -> spostaContenuto(contenuto, null));
        menu.getItems().add(organizza);
        for (Cartella cartella : contenutiControl.elencaCartelle(SessioneCorrente.getIstanza().getAccountAutenticato())) {
            MenuItem sposta = new MenuItem("Sposta in: " + cartella.getNome());
            sposta.setOnAction(e -> spostaContenuto(contenuto, cartella));
            menu.getItems().add(sposta);
        }

        menu.getItems().add(new SeparatorMenuItem());
        boolean archiviato = "ARCHIVIATO".equals(contenuto.getStatoArchiviazione());
        MenuItem archivia = new MenuItem(archiviato ? "Ripristina" : "Archivia");
        archivia.setOnAction(e -> {
            if (archiviato) {
                contenutiControl.ripristinaContenuto(contenuto);
            } else {
                contenutiControl.archiviaContenuto(contenuto);
            }
            aggiornaTabella();
        });
        menu.getItems().add(archivia);

        MenuItem rimuovi = new MenuItem("Rimuovi Definitivamente");
        rimuovi.setOnAction(e -> rimuoviContenuto(contenuto));
        menu.getItems().add(rimuovi);

        return menu;
    }

    private void impostaVisibilita(Contenuto contenuto, String livello) {
        try {
            contenutiControl.impostaVisibilita(contenuto, livello);
            aggiornaTabella();
        } catch (ContenutoException e) {
            mostraErrore(e.getMessage());
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void spostaContenuto(Contenuto contenuto, Cartella cartella) {
        try {
            contenutiControl.spostaContenutoInCartella(contenuto, cartella);
            aggiornaTabella();
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void rimuoviContenuto(Contenuto contenuto) {
        // RAD §3.3.1.2: conferma esplicita per operazioni irreversibili (caso d'uso 3.4 Rimuovi Contenuto).
        Alert conferma = new Alert(Alert.AlertType.CONFIRMATION,
                "Questa operazione eliminera' definitivamente \"" + contenuto.getTitolo() + "\". "
                        + "Per nascondere temporaneamente un contenuto usa \"Archivia\" invece di rimuoverlo. Continuare?",
                ButtonType.YES, ButtonType.NO);
        conferma.setTitle("Rimuovi contenuto");
        conferma.setHeaderText("Conferma rimozione definitiva");
        Optional<ButtonType> risposta = conferma.showAndWait();
        if (risposta.isEmpty() || risposta.get() != ButtonType.YES) {
            return;
        }
        try {
            contenutiControl.rimuoviContenuto(contenuto);
            aggiornaTabella();
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void aggiornaTabella() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            ricaricaComboCartelle();
            List<Contenuto> tutti = contenutiControl.elencaContenuti(account);
            Cartella filtro = cmbFiltroCartella.getValue();
            List<Contenuto> filtrati = filtro == null
                    ? tutti
                    : tutti.stream()
                        .filter(c -> c.getCartella() != null
                                && filtro.getIdCartella().equals(c.getCartella().getIdCartella()))
                        .toList();
            tabellaContenuti.setItems(FXCollections.observableArrayList(filtrati));
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 3.2 Organizza Contenuti: cartelle
    // ------------------------------------------------------------------

    @FXML
    private void onCreaCartella() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            contenutiControl.creaCartella(account, txtNuovaCartella.getText());
            txtNuovaCartella.clear();
            aggiornaTabella();
        } catch (ContenutoException e) {
            mostraErrore(e.getMessage());
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onEliminaCartella() {
        Cartella selezionata = cmbFiltroCartella.getValue();
        if (selezionata == null) {
            mostraErrore("Seleziona una cartella dal filtro per eliminarla.");
            return;
        }
        try {
            contenutiControl.eliminaCartella(selezionata);
            aggiornaTabella();
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onRinominaCartella() {
        Cartella selezionata = cmbFiltroCartella.getValue();
        if (selezionata == null) {
            mostraErrore("Seleziona una cartella dal filtro per rinominarla.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selezionata.getNome());
        dialog.setTitle("Rinomina cartella");
        dialog.setHeaderText(null);
        dialog.setContentText("Nuovo nome:");
        Optional<String> risposta = dialog.showAndWait();
        if (risposta.isEmpty()) {
            return;
        }
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            contenutiControl.rinominaCartella(account, selezionata, risposta.get());
            aggiornaTabella();
        } catch (ContenutoException e) {
            mostraErrore(e.getMessage());
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 3.1 Aggiungi Contenuto
    // ------------------------------------------------------------------

    @FXML
    private void onMostraFormFile() {
        mostraForm(formFile);
    }

    @FXML
    private void onMostraFormLink() {
        mostraForm(formLink);
    }

    @FXML
    private void onAnnullaForm() {
        nascondiForm(formFile);
        nascondiForm(formLink);
    }

    @FXML
    private void onScegliFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleziona il file da caricare");
        File selezionato = chooser.showOpenDialog(tabellaContenuti.getScene().getWindow());
        if (selezionato != null) {
            fileSelezionato = selezionato;
            lblFileSelezionato.setText("File selezionato: " + selezionato.getName());
        }
    }

    @FXML
    private void onConfermaFile() {
        if (fileSelezionato == null) {
            mostraEsito(lblEsitoFile, "Seleziona un file prima di confermare.", false);
            return;
        }
        if (!accettaAutocertificazione()) {
            return;
        }
        String tipo = rbAudio.isSelected() ? "FILE_AUDIO" : rbVideo.isSelected() ? "FILE_VIDEO" : "FILE_SPARTITO";
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            contenutiControl.aggiungiContenutoFile(account, fileSelezionato.toPath(), tipo,
                    txtTitoloFile.getText(), txtDidascaliaFile.getText(), txtAltFile.getText());
            fileSelezionato = null;
            lblFileSelezionato.setText("Nessun file selezionato");
            txtTitoloFile.clear();
            txtDidascaliaFile.clear();
            txtAltFile.clear();
            nascondiForm(formFile);
            aggiornaTabella();
            mostraSuccesso("Contenuto aggiunto correttamente.");   // <-- questa riga qui
        } catch (ContenutoException e) {
            mostraEsito(lblEsitoFile, e.getMessage(), false);
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    @FXML
    private void onConfermaLink() {
        if (!accettaAutocertificazione()) {
            return;
        }
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        try {
            contenutiControl.aggiungiContenutoLink(account, txtUrlLink.getText(),
                    txtTitoloLink.getText(), txtDidascaliaLink.getText(), txtAltLink.getText());
            txtUrlLink.clear();
            txtTitoloLink.clear();
            txtDidascaliaLink.clear();
            txtAltLink.clear();
            nascondiForm(formLink);
            aggiornaTabella();
            mostraSuccesso("Contenuto aggiunto correttamente.");   // <-- e anche qui
        } catch (ContenutoException e) {
            mostraEsito(lblEsitoLink, e.getMessage(), false);
        } catch (DbmsException e) {
            MainApp.gestisciErroreCritico(e.getMessage());
        }
    }

    private void mostraForm(VBox form) {
        form.setVisible(true);
        form.setManaged(true);
    }

    private void nascondiForm(VBox form) {
        form.setVisible(false);
        form.setManaged(false);
    }

    private boolean accettaAutocertificazione() {
        Alert autocertificazione = new Alert(Alert.AlertType.CONFIRMATION,
                "Dichiaro, sotto la mia responsabilità, di essere l'autore del contenuto che sto caricando "
                        + "o di detenerne i diritti di utilizzo, e sollevo l'istituzione AFAM da ogni "
                        + "responsabilità in merito alla sua originalità e alla sua pubblicazione.",
                ButtonType.YES, ButtonType.NO);
        autocertificazione.setTitle("Autocertificazione della titolarità");
        autocertificazione.setHeaderText("Prima di continuare, conferma quanto segue:");
        Optional<ButtonType> risposta = autocertificazione.showAndWait();
        return risposta.isPresent() && risposta.get() == ButtonType.YES;
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING, messaggio, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

/**conferma esplicita ("Lo Studente AFAM preme il pulsante 'OK'"). */
 private void mostraSuccesso(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, messaggio, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostraEsito(Label label, String messaggio, boolean successo) {
        label.getStyleClass().setAll(successo ? "success-label" : "error-label");
        label.setText(messaggio);
        label.setVisible(true);
        label.setManaged(true);
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
