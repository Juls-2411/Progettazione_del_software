package com.example.gestioneafam.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * In un'installazione web i file caricati finivano su uno storage
 * condiviso (o nel DBMS come BLOB); nel prototipo desktop, coerentemente
 * con il vincolo di packaging "per nodo" (RAD §3.3.7), i file vengono
 * copiati in una cartella dati locale all'installazione, sotto la home
 * utente, mentre il DBMS ne conserva solo il percorso (vedi
 * entity.FileMultimediale e entity.Profilo).
 */
public final class FileStorageUtil {

    private static final Path CARTELLA_DATI = Paths.get(System.getProperty("user.home"), ".afam-desktop", "storage");

    private FileStorageUtil() {
    }

    /**
     * Copia il file sorgente nella cartella dati dell'applicazione,
     * all'interno della sottocartella indicata (es. "immagini", "curriculum",
     * "contenuti"), assegnandogli un nome univoco per evitare collisioni,
     * e restituisce il percorso assoluto della copia.
     */
    public static String copiaFile(Path sorgente, String sottocartella) {
        try {
            Path destinazioneDir = CARTELLA_DATI.resolve(sottocartella);
            Files.createDirectories(destinazioneDir);
            String estensione = estraiEstensione(sorgente.getFileName().toString());
            String nomeUnivoco = UUID.randomUUID() + (estensione.isEmpty() ? "" : "." + estensione);
            Path destinazione = destinazioneDir.resolve(nomeUnivoco);
            Files.copy(sorgente, destinazione, StandardCopyOption.REPLACE_EXISTING);
            return destinazione.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static long dimensioneFile(Path percorso) {
        try {
            return Files.size(percorso);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String estraiEstensione(String nomeFile) {
        int puntoIndex = nomeFile.lastIndexOf('.');
        return puntoIndex >= 0 && puntoIndex < nomeFile.length() - 1
                ? nomeFile.substring(puntoIndex + 1).toLowerCase()
                : "";
    }
}
