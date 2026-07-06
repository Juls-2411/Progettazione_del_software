package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.IdentitaDigitaleEsterna;
import com.example.gestioneafam.entity.Profilo;
import com.example.gestioneafam.entity.Sessione;
import com.example.gestioneafam.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa il caso d'uso 1.4 Login con SPID/eIDAS (RAD §3.4.2/§3.4.3.1.4):
 * selezione dell'Identity Provider (o del Paese, per eIDAS), autenticazione
 * presso il provider, associazione dell'identita' certificata al profilo
 * locale (creandolo automaticamente se non esiste, sequenza alternativa 2)
 * e apertura della sessione.
 *
 * NOTA METODOLOGICA (nello stesso spirito della nota gia' presente per la
 * Comunicazione tra Nodi AFAM): il RAD analizza l'interazione con
 * SPID/eIDAS "senza realizzarne l'implementazione completa" (RAD §1.1),
 * poiche' richiederebbe l'integrazione con Identity Provider reali esterni
 * al sistema. Questa classe implementa quindi un Identity Provider
 * *simulato*, con un piccolo insieme di identita' di test (in analogia con
 * i cosiddetti "IdP di test" forniti da AgID per gli sviluppatori SPID),
 * cosi' da poter esercitare realmente il flusso applicativo — inclusa la
 * creazione automatica del profilo, l'associazione al codice fiscale e le
 * sequenze alternative di autenticazione fallita/annullata e di timeout —
 * senza dipendere da un vero provider esterno. Sostituire l'IdP simulato
 * con un vero client SAML/OIDC verso un Identity Provider reale non
 * richiederebbe di modificare il resto dell'applicazione: solo il metodo
 * {@link #autenticaPressoProvider} andrebbe adattato al protocollo reale.
 */
public class GestoreSpidEidasControl {

    /** Identity Provider SPID accreditati (elenco semplificato a scopo dimostrativo). */
    public static final List<String> IDENTITY_PROVIDER_SPID = List.of(
            "Poste ID", "Aruba ID", "InfoCert ID", "Sielte ID", "TIM ID", "Namirial ID");

    /** Paesi europei abilitati eIDAS (elenco semplificato a scopo dimostrativo). */
    public static final List<String> PAESI_EIDAS = List.of(
            "Italia", "Francia", "Germania", "Spagna", "Portogallo");

    /**
     * Identita' di test disponibili presso l'Identity Provider simulato,
     * cosi' come un vero IdP SPID restituirebbe i dati certificati
     * dell'utente dopo l'autenticazione. Il codice fiscale e' la chiave
     * con cui il caso d'uso associa l'identita' al profilo locale.
     */
    public record IdentitaTest(String identificativoEsterno, String nome, String cognome,
                               String codiceFiscale, String email) {
        @Override
        public String toString() {
            return nome + " " + cognome + "  (CF: " + codiceFiscale + ")";
        }
    }

    private static final List<IdentitaTest> IDENTITA_DI_TEST = List.of(
            new IdentitaTest("spid-test-001", "Mario", "Rossi", "RSSMRA85M01H501U", "mario.rossi@test-spid.it"),
            new IdentitaTest("spid-test-002", "Giulia", "Bianchi", "BNCGLI90A41F205X", "giulia.bianchi@test-spid.it"),
            new IdentitaTest("eidas-test-001", "Luca", "Verdi", "VRDLCU95T10L219K", "luca.verdi@test-eidas.eu"),
            new IdentitaTest("eidas-test-002", "Anna", "Conti", "CNTNNA92E44F839Y", "anna.conti@test-eidas.eu")
    );

    private final AccountRepository accountRepository;

    public GestoreSpidEidasControl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /** Passo 2 del flusso principale: elenco degli Identity Provider SPID. */
    public List<String> elencaIdentityProviderSpid() {
        return IDENTITY_PROVIDER_SPID;
    }

    /** Sequenza alternativa 1 (Accesso tramite eIDAS): elenco dei Paesi europei abilitati. */
    public List<String> elencaPaesiEidas() {
        return PAESI_EIDAS;
    }

    /**
     * Identita' disponibili presso l'Identity Provider/nodo eIDAS simulato
     * scelto: in un IdP reale corrisponderebbe alla schermata di login del
     * provider (username/password/OTP dell'utente); qui l'utente sceglie
     * direttamente una delle identita' di test disponibili.
     */
    public List<IdentitaTest> elencaIdentitaDiTest() {
        return IDENTITA_DI_TEST;
    }

    /**
     * Passi 4-7 del flusso principale (piu' le sequenze alternative 3 e 4):
     * simula l'autenticazione presso il provider e, in caso di successo,
     * associa l'identita' certificata al profilo locale.
     *
     * @param identita               identita' di test selezionata (dati "certificati" dal provider)
     * @param provider               nome dell'Identity Provider o "eIDAS-&lt;Paese&gt;" scelto
     * @param simulaAutenticazioneFallita simula la sequenza alternativa 3 (autenticazione fallita/annullata)
     * @param simulaTimeout          simula la sequenza alternativa 4 (timeout di rete verso il provider)
     */
    public Account autenticaConIdentitaCertificata(IdentitaTest identita, String provider,
                                                   boolean simulaAutenticazioneFallita, boolean simulaTimeout) {
        // Sequenza alternativa 4 (Timeout di rete): l'attore Tempo rileva che il
        // provider esterno non risponde entro il limite stabilito.
        if (simulaTimeout) {
            throw new AutenticazioneException(
                    "Errore di connessione con il Provider. Tempo scaduto, riprovare piu' tardi.");
        }
        // Sequenza alternativa 3 (Autenticazione annullata/fallita sul provider).
        if (simulaAutenticazioneFallita) {
            throw new AutenticazioneException("Errore: Autenticazione Fallita o Annullata.");
        }

        // Passo 6: il Sistema interroga il DBMS per associare l'identita' certificata
        // al profilo locale gia' esistente dello studente...
        Account account = accountRepository.findByCodiceFiscale(identita.codiceFiscale()).orElse(null);

        if (account == null) {
            // ...sequenza alternativa 2 (Creazione automatica profilo locale): se il
            // codice fiscale non e' associato ad alcun profilo, il Sistema ne crea
            // uno nuovo a partire dai dati certificati ricevuti dal provider.
            account = new Account();
            Profilo profilo = new Profilo();
            profilo.setNome(identita.nome());
            profilo.setCognome(identita.cognome());
            profilo.setCodiceFiscale(identita.codiceFiscale());
            profilo.setEmail(identita.email());
            account.setProfilo(profilo);
            // NOTA: non viene creata alcuna Credenziali locale: un account nato da
            // SPID/eIDAS resta autenticabile solo tramite provider esterno, salvo
            // future estensioni (fuori ambito RAD) per l'impostazione di una
            // password locale. GestoreAutenticazioneControl.aggiornaPassword lo
            // gestisce esplicitamente (vedi relativo controllo Credenziali == null).
        }

        IdentitaDigitaleEsterna identitaEsterna = new IdentitaDigitaleEsterna();
        identitaEsterna.setProvider(provider);
        identitaEsterna.setIdentificativoEsterno(identita.identificativoEsterno());
        identitaEsterna.setCodiceFiscale(identita.codiceFiscale());
        identitaEsterna.setDataAssociazione(LocalDateTime.now());
        account.aggiungiIdentitaEsterna(identitaEsterna);

        // Passo 7: il Sistema autentica lo studente (nessun secondo fattore locale
        // aggiuntivo: l'autenticazione forte e' gia' garantita dal provider SPID/eIDAS).
        LocalDateTime ora = LocalDateTime.now();
        account.aggiungiSessione(new Sessione(ora, ora.plusMinutes(30)));

        account = accountRepository.save(account);
        SessioneCorrente.getIstanza().setAccountAutenticato(account);
        return account;
    }
}
