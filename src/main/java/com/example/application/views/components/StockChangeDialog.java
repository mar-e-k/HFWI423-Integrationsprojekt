package com.example.application.views.components;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.button.Button;
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

        // Layout des Dialogs
        FormLayout form = new FormLayout(name, number, current, change);
        add(form);

        Button cancel = new Button("Cancel", e -> close());
        Button save = new Button("Save", e -> {
            int oldStock = article.getStockLevel() != null ? article.getStockLevel() : 0;
            int delta = change.getValue() != null ? change.getValue() : 0;

            //Fach-/Stammdaten absichern
            if (article.getStorageLocation() == null || article.getStorageLocation().isBlank()) {
                article.setStorageLocation("Unknown");
            }
            if (article.getName() == null || article.getName().isBlank()) {
                article.setName("Unnamed");
            }
            // Sinnvolle Plausibilitätsprüfung statt "newStock < 0":
            // Wie viele Stück sind insgesamt vorhanden (offen + alle Paletten)?
            int piecesPerPallet = article.getPiecesPerPallet() != null ? article.getPiecesPerPallet() : 0;
            int reservePallets = article.getReservePallets() != null ? article.getReservePallets() : 0;
            int totalAvailable = oldStock + reservePallets * piecesPerPallet;

            if (delta < 0 && -delta > totalAvailable) {
                Notification.show("Cannot issue more than total available (" + totalAvailable + ").");
                return;
            }

            // Service aufrufen → HIER läuft die Palettenlogik
            ArticleInfo updated = articleInfoService.applyStockChange(article, delta);

            int finalStock = updated.getStockLevel() != null ? updated.getStockLevel() : 0;

            Notification.show("Stock updated: " + oldStock + " → " + finalStock +
                    " | Of Article: " + updated.getName() +
                    " | Number: " + updated.getArticleNumber());

            if (onSuccess != null) onSuccess.run();
            close();
        });

        getFooter().add(cancel, save);
    }
}