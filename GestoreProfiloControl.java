package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.FileStorageUtil;

import java.nio.file.Path;
import java.util.Set;

/**
 * Implementa i flussi del macro caso d'uso Gestione Profilo (RAD §2):
 * 2.1/2.2 Inserisci/Modifica Dati Anagrafici, 2.3 Gestione Immagine
 * Profilo, 2.4 Modifica Password, 2.5 Cancellazione Dati Account.
 *
 * Nota di progetto: 2.1 e 2.2 sono realizzati da un unico metodo
 * {@link #salvaDatiAnagrafici}, idempotente rispetto alla presenza o meno
 * di dati preesistenti (la Boundary sceglie solo l'etichetta della
 * schermata, "Inserisci" o "Modifica", in base allo stato del profilo),
 * cosi' come il RAD stesso unifica altrove casi d'uso analoghi (es.
 * "Aggiungi Contenuto" per file e collegamenti).
 */
public class GestoreProfiloControl {

    private static final long DIMENSIONE_MASSIMA_IMMAGINE_BYTE = 2L * 1024 * 1024; // 2MB, RAD §2.3
    private static final Set<String> FORMATI_IMMAGINE_VALIDI = Set.of("jpg", "jpeg", "png");

    private static final long DIMENSIONE_MASSIMA_CURRICULUM_BYTE = 10L * 1024 * 1024; // 10MB
    private static final Set<String> FORMATI_CURRICULUM_VALIDI = Set.of("pdf");

    private final AccountRepository accountRepository;

    public GestoreProfiloControl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // ------------------------------------------------------------------
    // 2.1 Inserisci Dati Anagrafici / 2.2 Modifica Dati Anagrafici
    // ------------------------------------------------------------------

    public Account salvaDatiAnagrafici(Account account, String nome, String cognome,
                                        String indirizzo, String recapito, Path curriculumSelezionato) {
        if (isVuoto(nome) || isVuoto(cognome)) {
            throw new ProfiloException("Dati mancanti o non validi. Verificare i campi contrassegnati.");
        }

        account.getProfilo().setNome(nome);
        account.getProfilo().setCognome(cognome);
        account.getProfilo().setIndirizzo(indirizzo);
        account.getProfilo().setRecapito(recapito);

        if (curriculumSelezionato != null) {
            validaFile(curriculumSelezionato, FORMATI_CURRICULUM_VALIDI, DIMENSIONE_MASSIMA_CURRICULUM_BYTE,
                    "Il curriculum deve essere un PDF di dimensione inferiore a 10MB.");
            String percorso = FileStorageUtil.copiaFile(curriculumSelezionato, "curriculum");
            account.getProfilo().setCurriculumPath(percorso);
        }

        return accountRepository.save(account);
    }

    // ------------------------------------------------------------------
    // 2.3 Gestione Immagine Profilo
    // ------------------------------------------------------------------

    public Account aggiornaImmagineProfilo(Account account, Path immagineSelezionata) {
        validaFile(immagineSelezionata, FORMATI_IMMAGINE_VALIDI, DIMENSIONE_MASSIMA_IMMAGINE_BYTE,
                "Formato file non supportato o dimensione eccessiva. Selezionare un'immagine JPG o PNG inferiore a 2MB.");

        String percorso = FileStorageUtil.copiaFile(immagineSelezionata, "immagini");
        account.getProfilo().setImmagineProfiloPath(percorso);
        return accountRepository.save(account);
    }

    // ------------------------------------------------------------------
    // 2.4 Modifica Password
    // ------------------------------------------------------------------
    // Il flusso e' gia' implementato in GestoreAutenticazioneControl.aggiornaPassword,
    // dato che opera sulle stesse Credenziali usate al Login; la Boundary di
    // Gestione Profilo lo richiama direttamente per evitare di duplicare la
    // logica di verifica password attuale / criteri di sicurezza.

    // ------------------------------------------------------------------
    // 2.5 Cancellazione Dati Account
    // ------------------------------------------------------------------

    public void eliminaAccount(Account account) {
        accountRepository.delete(account);
    }

    private void validaFile(Path file, Set<String> formatiValidi, long dimensioneMassima, String messaggioErrore) {
        String estensione = FileStorageUtil.estraiEstensione(file.getFileName().toString());
        long dimensione = FileStorageUtil.dimensioneFile(file);
        if (!formatiValidi.contains(estensione) || dimensione > dimensioneMassima) {
            throw new ProfiloException(messaggioErrore);
        }
    }

    private boolean isVuoto(String valore) {
        return valore == null || valore.isBlank();
    }
}
