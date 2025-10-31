package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.Article;
import de.fhdw.kassensystem.persistence.service.ArticleService;
import de.fhdw.kassensystem.utility.config.Roles;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@Route("/admin")
@RolesAllowed(Roles.Type.ADMIN)
@PageTitle("Admin View")
public class AdminView extends BaseView {

    private final ArticleService articleService;
    private Grid<Article> grid;
    private TextField searchField;

    public AdminView(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    protected String setTopbarTitle() {
        return "Admin-Dashboard";
    }

    @Override
    protected void init() {
        // Die UI-Initialisierung wird in initUI() verschoben, um sicherzustellen,
        // dass der Service injiziert ist.
    }

    @PostConstruct
    public void initUI() {
        // Layout auf volle Größe einstellen
        setSizeFull();
        // Zentrierung aus der BaseView aufheben, damit die Komponenten sich strecken
        setAlignItems(Alignment.STRETCH);

        // Suchfeld initialisieren
        searchField = new TextField();
        searchField.setPlaceholder("Nach Artikelnamen suchen...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());

        // Aktualisierungs-Button
        Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH));
        refreshButton.setTooltipText("Tabelle aktualisieren");
        refreshButton.addClickListener(e -> updateGrid());

        // Toolbar für Suchfeld und Button
        HorizontalLayout toolbar = new HorizontalLayout(searchField, refreshButton);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setFlexGrow(1, searchField); // Suchfeld nimmt den meisten Platz ein

        // Grid initialisieren und auf volle Größe einstellen
        grid = new Grid<>(Article.class, false);
        grid.setSizeFull(); // Wichtig: Grid soll den verfügbaren Platz füllen

        // Spalten definieren
        grid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer").setSortable(true);
        grid.addColumn(Article::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Article::getManufacturer).setHeader("Hersteller").setSortable(true);
        grid.addColumn(Article::getSellingPrice).setHeader("Verkaufspreis").setSortable(true);
        grid.addColumn(Article::getStockLevel).setHeader("Lagerbestand").setSortable(true);
        grid.addColumn(Article::getIsAvailable).setHeader("Verfügbar").setSortable(true);

        // Initiales Laden der Daten
        updateGrid();

        // Komponenten zum Layout hinzufügen
        add(toolbar, grid);
        
        // Das Grid soll den restlichen Platz einnehmen
        setFlexGrow(1, grid);
    }

    private void updateGrid() {
        String searchTerm = searchField.getValue();
        if (searchTerm == null || searchTerm.isEmpty()) {
            grid.setItems(articleService.findAll());
        } else {
            grid.setItems(articleService.findByNameContainingIgnoreCase(searchTerm));
        }
    }
}