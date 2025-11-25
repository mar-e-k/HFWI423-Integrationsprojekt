package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionPosition;
import com.example.application.data.orderPicking.KommissionPositionRepository;
import com.example.application.data.orderPicking.KommissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class KommissionService {

    @Autowired private KommissionRepository komRepo;
    @Autowired private KommissionPositionRepository posRepo;
    @Autowired private ArticleInfoService artikelService;

    /**
     * Liefert alle Kommissionen, die noch nicht abgeschlossen sind.
     */
    public List<Kommission> getOffeneKommissionen() {
        return komRepo.findByErledigtFalseOrderByDateAsc();
    }

    /**
     * Bestätigt eine einzelne Position, ggf. mit abweichender Menge.
     */
    @Transactional
    public void bestätigePosition(Long positionId, int gelieferteMenge, String grund, String ersteller) {
        KommissionPosition pos = posRepo.findById(positionId)
                .orElseThrow(() -> new EntityNotFoundException("Position nicht gefunden"));

        ArticleInfo artikel = pos.getArtikel();

        int geplant = pos.getMenge();
        pos.setMenge(gelieferteMenge);

        // Bestandsreduktion um gelieferte Menge
        artikelService.reduceStock(artikel, gelieferteMenge);

        posRepo.save(pos);
    }

    /**
     * Prüft, ob eine Kommission abgeschlossen werden kann (alle Positionen bearbeitet).
     * Wenn ja, wird sie abgeschlossen und ausgegraut dargestellt.
     */
    //@Transactional
    /**public void schließeKommission(Long kommissionId) {
        Kommission k = komRepo.findById(kommissionId)
                .orElseThrow(() -> new EntityNotFoundException("Kommission nicht gefunden"));

        boolean alleBearbeitet = k.getPositionen()
                .stream()
                .allMatch(KommissionPosition::isBearbeitet);

        if (!alleBearbeitet) {
            throw new IllegalStateException("Kommission kann nicht abgeschlossen werden: noch offene Positionen.");
        }

        k.setFinished(true);
        komRepo.save(k);
    }*/
}