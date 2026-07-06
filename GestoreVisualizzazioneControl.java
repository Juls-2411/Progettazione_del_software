package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.entity.Link;
import com.example.gestioneafam.entity.Visualizzazione;
import com.example.gestioneafam.repository.LinkRepository;
import com.example.gestioneafam.repository.VisualizzazioneRepository;
import com.example.gestioneafam.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa i flussi del macro caso d'uso Gestione Visualizzazione (RAD
 * §5): 5.1 Visualizza Contenuti Condivisi (Utente Esterno, tramite link)
 * e 5.2 Consulta Riscontri di Visualizzazione (Studente AFAM).
 */
public class GestoreVisualizzazioneControl {

    private final LinkRepository linkRepository;
    private final VisualizzazioneRepository visualizzazioneRepository;

    public GestoreVisualizzazioneControl(LinkRepository linkRepository,
                                          VisualizzazioneRepository visualizzazioneRepository) {
        this.linkRepository = linkRepository;
        this.visualizzazioneRepository = visualizzazioneRepository;
    }

    // ------------------------------------------------------------------
    // 5.1 Visualizza Contenuti Condivisi
    // ------------------------------------------------------------------

    /**
     * Passo 1 del flusso: apertura del link da parte dell'Utente Esterno.
     * Verifica che il link esista, sia attivo e non scaduto (sequenza
     * alternativa "Link non valido").
     */
    public Link apriLink(String url) {
        Link link = linkRepository.findByUrl(url)
                .orElseThrow(() -> new VisualizzazioneException("Il link non e' piu' valido o e' stato revocato."));
        if (!link.isValido()) {
            throw new VisualizzazioneException("Il link non e' piu' valido o e' stato revocato.");
        }
        return link;
    }

    /** Passo 2 (solo se il link e' protetto da password): verifica della password inserita. */
    public void verificaPassword(Link link, String passwordInserita) {
        if (link.getPasswordHash() == null) {
            return;
        }
        if (passwordInserita == null || !PasswordUtil.matches(passwordInserita, link.getPasswordHash())) {
            throw new VisualizzazioneException("Password non corretta.");
        }
    }

    /**
     * Passo finale, dopo l'accettazione del disclaimer sul diritto d'autore
     * (RAD §3.3.8): registra l'accesso e restituisce i soli contenuti
     * ancora effettivamente condivisi via link (un contenuto incluso nel
     * link ma nel frattempo reimpostato su "Privato" non e' piu'
     * accessibile, RAD §3.3 nota).
     */
    public List<Contenuto> registraAccessoERestituisciContenuti(Link link) {
        Visualizzazione visualizzazione = new Visualizzazione(LocalDateTime.now());
        visualizzazione.setLink(link);
        visualizzazioneRepository.save(visualizzazione);

        return link.getContenutiSelezionati().stream()
                .filter(c -> "CONDIVISO_VIA_LINK".equals(c.getVisibilita()))
                .toList();
    }

    // ------------------------------------------------------------------
    // 5.2 Consulta Riscontri di Visualizzazione
    // ------------------------------------------------------------------

    public List<Visualizzazione> consultaRiscontri(Account account) {
        return visualizzazioneRepository.findByAccount(account);
    }
}
