package com.example.gestioneafam.control;

import com.example.gestioneafam.entity.Account;
import com.example.gestioneafam.entity.Contenuto;
import com.example.gestioneafam.entity.Link;
import com.example.gestioneafam.repository.LinkRepository;
import com.example.gestioneafam.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementa i flussi del macro caso d'uso Gestione Condivisione (RAD §4):
 * 4.1 Crea Link di Condivisione, 4.2 Gestisci Link di Condivisione
 * (modifica opzioni e revoca).
 */
public class GestoreCondivisioneControl {

    /** Prefisso simbolico dell'URL generato: in un'installazione reale corrisponderebbe
     * al dominio pubblico del nodo AFAM; qui e' un valore fisso di prototipo. */
    private static final String PREFISSO_URL = "https://afam.local/condivisi/";

    private final LinkRepository linkRepository;

    public GestoreCondivisioneControl(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    // ------------------------------------------------------------------
    // 4.1 Crea Link di Condivisione
    // ------------------------------------------------------------------

    public Link creaLink(Account account, List<Contenuto> contenutiSelezionati,
                          LocalDateTime scadenza, String password) {
        if (contenutiSelezionati == null || contenutiSelezionati.isEmpty()) {
            throw new CondivisioneException("Selezionare almeno un contenuto e una scadenza valida.");
        }
        if (scadenza != null && scadenza.isBefore(LocalDateTime.now())) {
            throw new CondivisioneException("Selezionare almeno un contenuto e una scadenza valida.");
        }

        Link link = new Link();
        link.setUrl(PREFISSO_URL + UUID.randomUUID());
        link.setDataCreazione(LocalDateTime.now());
        link.setScadenza(scadenza);
        link.setPasswordHash(password != null && !password.isBlank() ? PasswordUtil.hash(password) : null);
        link.setStato("ATTIVO");
        link.setAccount(account);
        link.getContenutiSelezionati().addAll(contenutiSelezionati);

        return linkRepository.save(link);
    }

    public List<Link> elencaLink(Account account) {
        return linkRepository.findByAccount(account);
    }

    // ------------------------------------------------------------------
    // 4.2 Gestisci Link di Condivisione
    // ------------------------------------------------------------------

    public void modificaOpzioni(Link link, LocalDateTime nuovaScadenza, String nuovaPassword) {
        if (nuovaScadenza != null && nuovaScadenza.isBefore(LocalDateTime.now())) {
            throw new CondivisioneException("Opzioni non valide: data di scadenza nel passato.");
        }
        link.setScadenza(nuovaScadenza);
        if (nuovaPassword != null && !nuovaPassword.isBlank()) {
            link.setPasswordHash(PasswordUtil.hash(nuovaPassword));
        } else {
            link.setPasswordHash(null);
        }
        linkRepository.save(link);
    }

    public void revocaLink(Link link) {
        link.setStato("REVOCATO");
        linkRepository.save(link);
    }
}
