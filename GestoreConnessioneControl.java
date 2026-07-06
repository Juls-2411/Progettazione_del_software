package com.example.gestioneafam.control;

import com.example.gestioneafam.repository.AbstractRepository;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class GestoreConnessioneControl {

    private GestoreConnessioneControl() {
    }

    public static void attiva() {
        AbstractRepository.impostaNotificaConnessionePersa(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore: Connessione Persa", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }
}