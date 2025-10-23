package fhdw.de.einkauf_service.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.ArticleResponse;
import fhdw.de.einkauf_service.service.ArticleService;

import java.util.List;

// @Route("/") macht dies zur Startseite Ihrer Anwendung (http://localhost:8080)
@Route("")
public class ArticleView extends VerticalLayout {

    private final ArticleService articleService;
    private final Grid<ArticleResponse> grid = new Grid<>(ArticleResponse.class);

    // Vaadin verwendet Spring-Dependency-Injection über den Konstruktor
    public ArticleView(ArticleService articleService) {
        this.articleService = articleService;

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
        // Ruft den bestehenden Service auf, um alle Artikel zu holen
        List<ArticleResponse> articles = articleService.findAllArticles();
        grid.setItems(articles);
    }
}