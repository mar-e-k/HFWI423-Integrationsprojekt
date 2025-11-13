package com.example.application.views.restockView;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.springframework.data.domain.Sort;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.services.StockChangeLogService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.example.application.data.article.ArticleInfo;
import com.example.application.services.RestockService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;


@PageTitle("Restock")
@Route("restock")
@Menu(order = 6, icon = LineAwesomeIconUrl.WINDOW_RESTORE)
@Uses(Icon.class)
public class RestockView extends Div {
    private final RestockService restockService;
    private final Grid<ArticleInfo> grid = new Grid<>();

    public RestockView(RestockService restockService) {
        this.restockService = restockService;

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        // Export-Button (Anchor-Variante)
        Anchor exportButton = new Anchor();
        exportButton.setText("Exportieren");
        exportButton.getElement().setAttribute("download", true);

        // Aktualisieren-Button
        Button refreshButton = new Button("Aktualisieren", event -> {
            updateGrid();
            updateCsvDownload(exportButton);
        });




        // Grid-Spalten definieren
        grid.addColumn(ArticleInfo::getArticleNumber).setHeader("Artikelnummer");
        grid.addColumn(ArticleInfo::getName).setHeader("Name");
        grid.addColumn(ArticleInfo::getStockLevel).setHeader("Bestand");
        grid.addColumn(ArticleInfo::getMinStock).setHeader("Mindestbestand");

        grid.setHeight("300px");


        // Komponenten ins Layout
        layout.add(refreshButton, exportButton, grid);
        add(layout);

        // Initial Daten laden
        updateGrid();
        updateCsvDownload(exportButton);
    }

    private void updateGrid() {
        List<ArticleInfo> items = restockService.getArticlesToRestock();
        grid.setItems(new ArrayList<>(items));

    }

    private void updateCsvDownload(Anchor exportButton) {
        String csv = buildCsv();

        // CSV in Base64 kodieren → funktioniert in JEDER Vaadin-Version ohne StreamResource
        String base64 = Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8));

        // Data-URL als Download
        exportButton.setHref("data:text/csv;base64," + base64);
    }

    private String buildCsv() {
        List<ArticleInfo> items = restockService.getArticlesToRestock();
        StringBuilder sb = new StringBuilder("Artikelnummer;Name;Bestand;Mindestbestand\n");

        for (ArticleInfo a : items) {
            sb.append(a.getArticleNumber()).append(";")
                    .append(a.getName()).append(";")
                    .append(a.getStockLevel()).append(";")
                    .append(a.getMinStock()).append("\n");
        }

        return sb.toString();
    }
}
