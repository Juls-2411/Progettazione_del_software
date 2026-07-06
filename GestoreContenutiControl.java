package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Cartella;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.entity.File;
import com.example.gestioneafam.repository.CartellaRepository;
import com.example.gestioneafam.repository.ContenutoRepository;
import com.example.gestioneafam.util.FileStorageUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementa i flussi del macro caso d'uso Gestione Contenuti (RAD §3):
 * 3.1 Aggiungi Contenuto (unifica file e collegamenti esterni, come nel
 * RAD), 3.2 Organizza Contenuti (cartelle e archiviazione), 3.3 Imposta
 * Visibilita' Contenuto, 3.4 Rimuovi Contenuto.
 */
public class GestoreContenutiControl {

    /** RAD §3.3.5: formati supportati per tipologia, in analogia col vincolo su Implementazione. */
    private static final Map<String, Set<String>> FORMATI_VALIDI = Map.of(
            "FILE_AUDIO", Set.of("mp3", "wav"),
            "FILE_VIDEO", Set.of("mp4", "mov"),
            "FILE_SPARTITO", Set.of("pdf")
    );

    private static final Map<String, Long> DIMENSIONE_MASSIMA_BYTE = Map.of(
            "FILE_AUDIO", 50L * 1024 * 1024,
            "FILE_VIDEO", 500L * 1024 * 1024,
            "FILE_SPARTITO", 20L * 1024 * 1024
    );

    private final ContenutoRepository contenutoRepository;
    private final CartellaRepository cartellaRepository;

    public GestoreContenutiControl(ContenutoRepository contenutoRepository, CartellaRepository cartellaRepository) {
        this.contenutoRepository = contenutoRepository;
        this.cartellaRepository = cartellaRepository;
    }

    // ------------------------------------------------------------------
    // 3.1 Aggiungi Contenuto
    // ------------------------------------------------------------------

    public Contenuto aggiungiContenutoFile(Account account, Path fileSelezionato, String tipo,
                                           String titolo, String didascalia, String testoAlternativo) {
        validaMetadati(titolo, didascalia, testoAlternativo);   // <-- prima era: validaMetadati(titolo, testoAlternativo);
        Set<String> formatiValidi = FORMATI_VALIDI.get(tipo);
        if (formatiValidi == null) {
            throw new ContenutoException("Tipologia di contenuto non riconosciuta.");
        }

        String estensione = FileStorageUtil.estraiEstensione(fileSelezionato.getFileName().toString());
        long dimensione = FileStorageUtil.dimensioneFile(fileSelezionato);
        if (!formatiValidi.contains(estensione) || dimensione > DIMENSIONE_MASSIMA_BYTE.get(tipo)) {
            throw new ContenutoException("Impossibile aggiungere il contenuto: file non supportato o troppo grande.");
        }

        String percorso = FileStorageUtil.copiaFile(fileSelezionato, "contenuti");

        Contenuto contenuto = new Contenuto();
        contenuto.setTitolo(titolo);
        contenuto.setDidascalia(didascalia);
        contenuto.setTestoAlternativo(testoAlternativo);
        contenuto.setTipo(tipo);
        contenuto.setAccount(account);

        File file = new File(estensione.toUpperCase(), dimensione, percorso);
        contenuto.setFile(file);

        // NOTA: non si naviga account.getContenuti() (collezione LAZY):
        // l'Account conservato in SessioneCorrente proviene da un
        // EntityManager gia' chiuso (vedi commento in entity.Account),
        // quindi basta impostare il lato proprietario della relazione
        // (Contenuto.account) e salvare il Contenuto direttamente.
        return contenutoRepository.save(contenuto);
    }

    public Contenuto aggiungiContenutoLink(Account account, String url,
                                            String titolo, String didascalia, String testoAlternativo) {
        validaMetadati(titolo, didascalia,testoAlternativo);
        if (!isUrlValido(url)) {
            throw new ContenutoException("Collegamento non valido o non raggiungibile.");
        }

        Contenuto contenuto = new Contenuto();
        contenuto.setTitolo(titolo);
        contenuto.setDidascalia(didascalia);
        contenuto.setTestoAlternativo(testoAlternativo);
        contenuto.setTipo("LINK_ESTERNO");
        contenuto.setUrlEsterno(url);
        contenuto.setAccount(account);

        return contenutoRepository.save(contenuto);
    }

    public List<Contenuto> elencaContenuti(Account account) {
        return contenutoRepository.findByAccount(account);
    }

    // ------------------------------------------------------------------
    // 3.2 Organizza Contenuti
    // ------------------------------------------------------------------

    public Cartella creaCartella(Account account, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ContenutoException("Operazione non valida: nome cartella duplicato o vuoto.");
        }
        if (cartellaRepository.esisteConNome(account, nome)) {
            throw new ContenutoException("Operazione non valida: nome cartella duplicato o vuoto.");
        }
        Cartella cartella = new Cartella(nome);
        cartella.setAccount(account);
        return cartellaRepository.save(cartella);
    }

    public List<Cartella> elencaCartelle(Account account) {
        return cartellaRepository.findByAccount(account);
    }

    public void rinominaCartella(Account account, Cartella cartella, String nuovoNome) {
        if (nuovoNome == null || nuovoNome.isBlank()) {
            throw new ContenutoException("Operazione non valida: nome cartella duplicato o vuoto.");
        }
        if (!nuovoNome.equalsIgnoreCase(cartella.getNome()) && cartellaRepository.esisteConNome(account, nuovoNome)) {
            throw new ContenutoException("Operazione non valida: nome cartella duplicato o vuoto.");
        }
        cartella.setNome(nuovoNome);
        cartellaRepository.save(cartella);
    }

    public void spostaContenutoInCartella(Contenuto contenuto, Cartella cartella) {
        contenuto.setCartella(cartella);
        contenutoRepository.save(contenuto);
    }

    public void archiviaContenuto(Contenuto contenuto) {
        contenuto.setStatoArchiviazione("ARCHIVIATO");
        contenutoRepository.save(contenuto);
    }

    public void ripristinaContenuto(Contenuto contenuto) {
        contenuto.setStatoArchiviazione("ATTIVO");
        contenutoRepository.save(contenuto);
    }

    public void eliminaCartella(Cartella cartella) {
        // Non rimuove i contenuti: li "sposta" fuori dalla cartella (rimane
        // sempre disponibile la rimozione esplicita del singolo contenuto,
        // caso d'uso 3.4, distinta e irreversibile).
        // NOTA: si usa una query dedicata (ContenutoRepository.findByCartella)
        // invece di navigare cartella.getContenuti() (collezione LAZY): la
        // Cartella qui ricevuta e' un'entity detached, proveniente da un
        // EntityManager gia' chiuso (vedi repository.LinkRepository per lo
        // stesso ragionamento sulle collezioni LAZY attraversate fuori scope).
        for (Contenuto contenuto : contenutoRepository.findByCartella(cartella)) {
            contenuto.setCartella(null);
            contenutoRepository.save(contenuto);
        }
        cartellaRepository.delete(cartella);
    }

    // ------------------------------------------------------------------
    // 3.3 Imposta Visibilita' Contenuto
    // ------------------------------------------------------------------

    public void impostaVisibilita(Contenuto contenuto, String livello) {
        if (!Set.of("PRIVATO", "PUBBLICO", "CONDIVISO_VIA_LINK").contains(livello)) {
            throw new ContenutoException("Livello di visibilita' non valido.");
        }
        contenuto.setVisibilita(livello);
        contenutoRepository.save(contenuto);
    }

    // ------------------------------------------------------------------
    // 3.4 Rimuovi Contenuto
    // ------------------------------------------------------------------

    public void rimuoviContenuto(Contenuto contenuto) {
        contenutoRepository.delete(contenuto);
    }

    private void validaMetadati(String titolo, String didascalia, String testoAlternativo) {
        if (titolo == null || titolo.isBlank()
                || didascalia == null || didascalia.isBlank()
                || testoAlternativo == null || testoAlternativo.isBlank()) {
            throw new ContenutoException("Titolo, didascalia e testo alternativo sono obbligatori.");
        }
    }

    private boolean isUrlValido(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(url);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
