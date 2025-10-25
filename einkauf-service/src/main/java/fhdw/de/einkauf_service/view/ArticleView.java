package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.serviceImpl.ArticleServiceImpl;

import java.util.List;

// @Route("/") macht dies zur Startseite Ihrer Anwendung (http://localhost:8080)
@Route("")
public class ArticleView extends VerticalLayout {

    private final ArticleServiceImpl articleServiceImpl;
    private final Grid<ArticleResponseDTO> grid = new Grid<>(ArticleResponseDTO.class);

    // Vaadin verwendet Spring-Dependency-Injection über den Konstruktor
    public ArticleView(ArticleServiceImpl articleServiceImpl) {
        this.articleServiceImpl = articleServiceImpl;

        // Layout-Einstellungen
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // 1. Grid konfigurieren
        configureGrid();

        // 2. Daten laden und anzeigen
        updateList();

        // 3. Komponenten zum Layout hinzufügen
        add(grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        // Definieren Sie, welche Spalten angezeigt werden sollen
        grid.setColumns("articleNumber", "name", "unit", "purchasePrice", "taxRatePercent", "sellingPrice");

        // Optional: Benutzerfreundliche Spaltenüberschriften setzen
        grid.getColumnByKey("articleNumber").setHeader("GTIN");
        grid.getColumnByKey("sellingPrice").setHeader("VK Preis");
    }

    private void updateList() {
        // Ein leeres Filter-DTO erstellen.
        // Da alle Felder null sind, werden alle Artikel zurückgegeben.
        ArticleFilterDTO emptyFilter = new ArticleFilterDTO();

        // Ruft den bestehenden Service auf, um alle Artikel zu holen
        List<ArticleResponseDTO> articles = articleServiceImpl.findFilteredArticles(emptyFilter);
        grid.setItems(articles);
    }
}