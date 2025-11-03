package com.example.application.views.gridwithfilters;

import com.example.application.data.ArticleInfo;
import com.example.application.data.ChangeType;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;

public class StockChangeDialog extends Dialog {

    private final ArticleInfoService articleInfoService;
    private final ArticleInfo article;

    public StockChangeDialog(ArticleInfoService articleInfoService,
                             ArticleInfo article,
                             Runnable onSuccess) {
        this.articleInfoService = articleInfoService;
        this.article = article;

        setHeaderTitle("Change Stock");
        // Textfeld zum einsehen des Artikelnamens --> keine Änderung zugelassen
        TextField name = new TextField("Article");
        name.setValue(article.getName());
        name.setReadOnly(true);
        // Textfeld zum einsehen der Artikelnummer --> keine Änderung zugelassen
        TextField number = new TextField("Article Number");
        number.setValue(article.getArticleNumber());
        number.setReadOnly(true);
        // Zahlenfeld zum einsehen des aktuellen Bestandes --> keine Änderung zugelassen
        IntegerField current = new IntegerField("Current Stock");
        current.setValue(article.getStockLevel());
        current.setReadOnly(true);

        // Feld zum addieren einer Änderung zum aktuellem Bestand
        IntegerField change = new IntegerField("Change (+/-)");
        change.setValue(0);

        // Angabe der Änderungsart mithilfe eines Drop down menüs -> Combo Box. Nur vorgefertigte eingaben
        ComboBox<ChangeType> changeType = new ComboBox<>("Type of Change");
        changeType.setItems(ChangeType.values());
        changeType.setItemLabelGenerator(ct -> switch (ct) {
            case ISSUE -> "Issue";
            case TRANSFER -> "Transfer";
            case ADJUSTMENT -> "Correction";
            case RECEIPT -> "Receipt";
        });
        changeType.setRequired(true);
        // Layout des Dialogs
        FormLayout form = new FormLayout(name, number, current, change, changeType);
        add(form);

        Button cancel = new Button("Cancel", e -> close());
        Button save = new Button("Save", e -> {
            // WICHTIG: gelb unterstrichenes != null und == null nicht entfernen!!! sonst klappts nicht. Intellij ist zu optimistisch
            // \_> während der runtime könnte "null" existieren
            int oldStock = article.getStockLevel() != null ? article.getStockLevel() : 0;
            int delta = change.getValue() != null ? change.getValue() : 0;
            int newStock = oldStock + delta;

            if (newStock < 0) {
                Notification.show("Stock cannot be negative.");
                return;
            }

            // Wegen oben im "WICHTIG:" genannten Problem von Intellij
            // WICHTIG: siehe oberes WICHTIG
            if (article.getStorageLocation() == null || article.getStorageLocation().isBlank()) {
                article.setStorageLocation("Unknown");
            }
            // WICHTIG: siehe oberes WICHTIG
            if (article.getName() == null || article.getName().isBlank()) {
                article.setName("Unnamed");
            }
            // Wenn nicht vorgefertigte eingaben dann == Empty -> fehler
            if (changeType.isEmpty()) {
                Notification.show("Please select a type of change");
                return;
            }
            // Aktualisierung der Daten und des Grids. Leitet Änderungen an Dokumentation weiter
            articleInfoService.applyStockChange(article, delta, changeType.getValue(), null, null);
            // Popup nach der Aktualisierung
            Notification.show("Stock updated: " + oldStock + " → " + newStock + " | Of Article: " + article.getName() +" | Number: "+ article.getArticleNumber() );
            if (onSuccess != null) onSuccess.run();
            close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancel, save);
    }
}