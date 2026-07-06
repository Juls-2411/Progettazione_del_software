package com.example.gestioneafam.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Identita digitale nazionale/europea (SPID/eIDAS) associata al profilo
 * locale dello studente (RAD, caso d'uso 1.4 Login con SPID/eIDAS).
 * In questa fase di analisi/prototipazione l'interazione con i provider
 * reali non e' implementata: vedi util.MailService e control per la
 * predisposizione dell'interfaccia.
 */
@Entity
@Table(name = "identita_digitale_esterna")
public class IdentitaDigitaleEsterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIdentitaEsterna;

    /** Es. SPID, eIDAS. */
    private String provider;

    private String identificativoEsterno;
    private String codiceFiscale;
    private LocalDateTime dataAssociazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public IdentitaDigitaleEsterna() {
    }

    public Long getIdIdentitaEsterna() {
        return idIdentitaEsterna;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdentificativoEsterno() {
        return identificativoEsterno;
    }

    public void setIdentificativoEsterno(String identificativoEsterno) {
        this.identificativoEsterno = identificativoEsterno;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public LocalDateTime getDataAssociazione() {
        return dataAssociazione;
    }

    public void setDataAssociazione(LocalDateTime dataAssociazione) {
        this.dataAssociazione = dataAssociazione;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
