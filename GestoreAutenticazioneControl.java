package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.CodiceOTP;
import com.example.gestioneafam.entity.CodiceRecupero;
import com.example.gestioneafam.entity.Credenziali;
import com.example.gestioneafam.entity.Profilo;
import com.example.gestioneafam.entity.Sessione;
import com.example.gestioneafam.repository.AccountRepository;
import com.example.gestioneafam.util.MailService;
import com.example.gestioneafam.util.OtpGenerator;
import com.example.gestioneafam.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementa i flussi descritti nel RAD §1 (Registrazione, Login, Recupero
 * Password, Login con SPID/eIDAS - non implementato, Logout).
 *
 * Corrisponde alla classe Control "gestoreAutenticazioneControl" nei
 * diagrammi di sequenza del RAD (§3.4.3.1): i suoi metodi pubblici
 * ricalcano deliberatamente i nomi dei messaggi nei sequence diagram
 * (registra, login, validaOTP, recuperaPassword, validaCodice,
 * modificaPassword, logout) per rendere tracciabile la corrispondenza
 * tra analisi e implementazione.
 */
public class GestoreAutenticazioneControl {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final int OTP_VALIDITA_MINUTI = 3;
    private static final int RECUPERO_VALIDITA_MINUTI = 15;
    public static final String MESSAGGIO_CODICE_OTP_NON_VALIDO = "Codice OTP non valido.";

    private final AccountRepository accountRepository;
    private final MailService mailService;

    public GestoreAutenticazioneControl(AccountRepository accountRepository, MailService mailService) {
        this.accountRepository = accountRepository;
        this.mailService = mailService;
    }

    // ------------------------------------------------------------------
    // 1.1 Registrazione
    // ------------------------------------------------------------------

    public Account registra(String nome, String cognome, String codiceFiscale,
                            String email, String password) {
        if (isVuoto(nome) || isVuoto(cognome) || isVuoto(codiceFiscale)
                || isVuoto(email) || isVuoto(password)) {
            throw new AutenticazioneException("Dati mancanti o non validi. Verificare i campi contrassegnati.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new AutenticazioneException("Dati mancanti o non validi. Verificare i campi contrassegnati.");
        }
        if (accountRepository.findByUsername(email).isPresent()
                || accountRepository.findByCodiceFiscale(codiceFiscale).isPresent()) {
            throw new AutenticazioneException("Utente gia' registrato nel sistema.");
        }

        Account account = new Account();

        Profilo profilo = new Profilo();
        profilo.setNome(nome);
        profilo.setCognome(cognome);
        profilo.setCodiceFiscale(codiceFiscale);
        profilo.setEmail(email);
        account.setProfilo(profilo);

        Credenziali credenziali = new Credenziali(email, PasswordUtil.hash(password));
        account.setCredenziali(credenziali);

        return accountRepository.save(account);
    }

    // ------------------------------------------------------------------
    // 1.2 Login (fase 1: verifica credenziali + generazione OTP)
    // ------------------------------------------------------------------

    public void login(String username, String password) {
        if (isVuoto(username) || isVuoto(password)) {
            throw new AutenticazioneException("Campi obbligatori mancanti. Assicurati di aver compilato tutti i dati.");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AutenticazioneException("UserName e/o Password non valide."));

        if (!PasswordUtil.matches(password, account.getCredenziali().getPasswordHash())) {
            throw new AutenticazioneException("UserName e/o Password non valide.");
        }

        String codice = OtpGenerator.generaCodice(6);
        LocalDateTime ora = LocalDateTime.now();
        CodiceOTP otp = new CodiceOTP(codice, ora, ora.plusMinutes(OTP_VALIDITA_MINUTI));
        account.aggiungiCodiceOtp(otp);
        accountRepository.save(account);

        SessioneCorrente.getIstanza().setEmailInSospeso(username);
        SessioneCorrente.getIstanza().setOtpAtteso(codice);

        mailService.inviaCodiceOtp(account.getProfilo().getEmail(), codice);
    }

    // ------------------------------------------------------------------
    // 1.2 Login (fase 2: verifica OTP e apertura sessione)
    // ------------------------------------------------------------------

    public Account validaOtp(String codiceInserito) {
        SessioneCorrente corrente = SessioneCorrente.getIstanza();
        String username = corrente.getEmailInSospeso();
        if (username == null) {
            throw new AutenticazioneException("Nessun login in corso. Effettuare nuovamente l'accesso.");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AutenticazioneException("Sessione non valida. Effettuare nuovamente l'accesso."));

        CodiceOTP ultimoOtp = account.getCodiciOtp().stream()
                .filter(c -> "ATTIVO".equals(c.getStato()))
                .reduce((first, second) -> second) // l'ultimo generato
                .orElseThrow(() -> new AutenticazioneException("Codice OTP scaduto. Effettuare un nuovo Login."));

        if (ultimoOtp.isScaduto()) {
            ultimoOtp.setStato("SCADUTO");
            accountRepository.save(account);
            throw new AutenticazioneException("Codice OTP scaduto. Effettuare un nuovo Login.");
        }

        if (!ultimoOtp.getCodice().equals(codiceInserito)) {
            throw new AutenticazioneException(MESSAGGIO_CODICE_OTP_NON_VALIDO);
        }

        ultimoOtp.setStato("USATO");
        LocalDateTime ora = LocalDateTime.now();
        account.aggiungiSessione(new Sessione(ora, ora.plusMinutes(30)));
        accountRepository.save(account);

        corrente.setOtpAtteso(null);
        corrente.setEmailInSospeso(null);
        corrente.setAccountAutenticato(account);

        return account;
    }

    // ------------------------------------------------------------------
    // 1.3 Recupero Password
    // ------------------------------------------------------------------

    public void richiediRecuperoPassword(String email) {
        Account account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new AutenticazioneException("Nessun account associato a questa email."));

        String codice = OtpGenerator.generaCodice(6);
        LocalDateTime ora = LocalDateTime.now();
        CodiceRecupero codiceRecupero = new CodiceRecupero(codice, ora, ora.plusMinutes(RECUPERO_VALIDITA_MINUTI));
        account.aggiungiCodiceRecupero(codiceRecupero);
        accountRepository.save(account);

        SessioneCorrente.getIstanza().setEmailRecuperoInSospeso(email);
        SessioneCorrente.getIstanza().setCodiceRecuperoAtteso(codice);

        mailService.inviaCodiceRecuperoPassword(email, codice);
    }

    public void validaCodiceRecupero(String codiceInserito) {
        SessioneCorrente corrente = SessioneCorrente.getIstanza();
        String email = corrente.getEmailRecuperoInSospeso();
        if (email == null) {
            throw new AutenticazioneException("Nessuna richiesta di recupero in corso.");
        }

        Account account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new AutenticazioneException("Richiedere un nuovo link di recupero."));

        CodiceRecupero ultimo = account.getCodiciRecupero().stream()
                .filter(c -> "ATTIVO".equals(c.getStato()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new AutenticazioneException("Codice scaduto. Richiedere un nuovo link di recupero."));

        if (ultimo.isScaduto()) {
            ultimo.setStato("SCADUTO");
            accountRepository.save(account);
            throw new AutenticazioneException("Codice scaduto. Richiedere un nuovo link di recupero.");
        }

        if (!ultimo.getCodice().equals(codiceInserito)) {
            throw new AutenticazioneException("Il Codice Inserito Non e' Valido.");
        }

        // Il codice resta ATTIVO finche' la password non e' stata effettivamente
        // cambiata (in linea con il flusso del RAD, che valida il codice
        // e poi mostra la pagina della nuova password come passo separato).
    }

    public void modificaPassword(String nuovaPassword, String confermaPassword) {
        SessioneCorrente corrente = SessioneCorrente.getIstanza();
        String email = corrente.getEmailRecuperoInSospeso();
        if (email == null) {
            throw new AutenticazioneException("Nessuna richiesta di recupero in corso.");
        }
        if (!nuovaPassword.equals(confermaPassword)) {
            throw new AutenticazioneException("ERRORE: Le password non corrispondono.");
        }

        Account account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new AutenticazioneException("Richiedere un nuovo link di recupero."));

        account.getCredenziali().setPasswordHash(PasswordUtil.hash(nuovaPassword));
        account.getCodiciRecupero().stream()
                .filter(c -> "ATTIVO".equals(c.getStato()))
                .forEach(c -> c.setStato("USATO"));
        accountRepository.save(account);

        corrente.setCodiceRecuperoAtteso(null);
        corrente.setEmailRecuperoInSospeso(null);
    }

    // ------------------------------------------------------------------
    // 1.4 Modifica Password (utente gia' autenticato, RAD §2.4)
    // ------------------------------------------------------------------

    public void aggiornaPassword(Account account, String passwordAttuale, String nuovaPassword, String conferma) {
        if (account.getCredenziali() == null) {
            // Account creato/autenticato tramite SPID/eIDAS (RAD §1.4): non possiede
            // credenziali locali, quindi non esiste una "password attuale" da verificare.
            throw new AutenticazioneException(
                    "Questo account e' associato a un'identita' SPID/eIDAS e non dispone di una password locale.");
        }
        if (!PasswordUtil.matches(passwordAttuale, account.getCredenziali().getPasswordHash())) {
            throw new AutenticazioneException("La password attuale inserita non e' corretta.");
        }
        if (!nuovaPassword.equals(conferma) || nuovaPassword.length() < 8) {
            throw new AutenticazioneException("Le nuove password non coincidono o non rispettano i criteri di sicurezza.");
        }
        account.getCredenziali().setPasswordHash(PasswordUtil.hash(nuovaPassword));
        accountRepository.save(account);
    }

    // ------------------------------------------------------------------
    // 1.5 Logout
    // ------------------------------------------------------------------

    public void logout() {
        Account account = SessioneCorrente.getIstanza().getAccountAutenticato();
        if (account != null) {
            account.getSessioni().stream()
                    .filter(s -> "ATTIVA".equals(s.getStato()))
                    .forEach(s -> s.setStato("CHIUSA"));
            accountRepository.save(account);
        }
        SessioneCorrente.getIstanza().invalida();
    }

    private boolean isVuoto(String valore) {
        return valore == null || valore.isBlank();
    }
}
