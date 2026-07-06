package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Consenso;
import com.example.gestioneafam.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa i flussi del macro caso d'uso Comunicazione tra Nodi AFAM
 * (RAD §6, requisito opzionale): 6.2 Gestisci Consenso Condivisione
 * Esterna, 6.1 Consulta Identita' Digitale su Nodo Esterno, 6.3 Fornisci
 * Identita' a Nodo AFAM Esterno.
 *
 * NOTA METODOLOGICA (in continuita' con la nota gia' presente per
 * SPID/eIDAS in GestoreAutenticazioneControl): il RAD analizza
 * l'interoperabilita' fra nodi come requisito opzionale, "di cui il
 * documento analizza requisiti, soluzioni progettuali e implicazioni
 * senza fornirne l'implementazione completa" (RAD §1.1). In questo
 * prototipo desktop mono-nodo non esiste una rete di installazioni AFAM
 * realmente separate con cui dialogare: i casi d'uso 6.1 e 6.3 sono
 * quindi *simulati* interrogando gli altri account registrati sullo
 * stesso nodo locale, che qui rappresentano gli "studenti di altre
 * istituzioni" ai soli fini dimostrativi. La logica applicativa
 * (verifica del consenso del titolare prima di ogni consultazione,
 * interpretazione dell'assenza di risposta come diniego, sola lettura)
 * e' pero' implementata fedelmente: sostituire la ricerca locale con
 * un'interrogazione via rete verso un vero Nodo AFAM Esterno (control
 * "Fornisci Identita' a Nodo AFAM Esterno" del RAD) non richiederebbe
 * modifiche a questa classe, solo un adattatore di trasporto diverso.
 */
public class GestoreFederazioneControl {

    private final AccountRepository accountRepository;

    public GestoreFederazioneControl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // ------------------------------------------------------------------
    // 6.2 Gestisci Consenso Condivisione Esterna
    // ------------------------------------------------------------------

    public void aggiornaConsenso(Account account, boolean concedi) {
        Consenso consenso = account.getConsenso();
        if (consenso == null) {
            consenso = new Consenso();
            account.setConsenso(consenso);
        }
        consenso.setStatoConsenso(concedi ? "CONCESSO" : "NEGATO");
        consenso.setDataAggiornamento(LocalDateTime.now());
        accountRepository.save(account);
    }

    // ------------------------------------------------------------------
    // 6.1 Consulta Identita' Digitale su Nodo AFAM Esterno (simulato)
    // ------------------------------------------------------------------

    /** Ricerca profili con criterio su nome/cognome tra gli account con consenso concesso, escluso il proprio. */
    public List<Account> cercaProfili(Account richiedente, String filtroNome) {
        return accountRepository.findConConsensoEsterno(filtroNome).stream()
                .filter(a -> !a.getIdAccount().equals(richiedente.getIdAccount()))
                .toList();
    }

    /**
     * Consultazione in sola lettura di un'identita' esterna: ripete la
     * verifica del consenso anche qui (e non solo nella ricerca), come
     * richiesto dal caso d'uso 6.1 e dal requisito non funzionale 3.4
     * (assenza di risposta/consenso = diniego, mai autorizzazione).
     */
    public Account consultaIdentita(Long idAccountRichiesto) {
        Account account = accountRepository.findById(idAccountRichiesto)
                .orElseThrow(() -> new FederazioneException("Profilo non disponibile o non condiviso."));
        if (account.getConsenso() == null || !"CONCESSO".equals(account.getConsenso().getStatoConsenso())) {
            throw new FederazioneException("Profilo non disponibile o non condiviso.");
        }
        return account;
    }
}
