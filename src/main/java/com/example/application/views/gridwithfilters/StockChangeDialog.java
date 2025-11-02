package com.example.application.views.gridwithfilters;

import com.example.application.data.ArticleInfo;
import com.example.application.services.ArticleInfoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

        TextField name = new TextField("Article");
        name.setValue(article.getName());
        name.setReadOnly(true);

        TextField number = new TextField("Article Number");
        number.setValue(article.getArticleNumber());
        number.setReadOnly(true);

        IntegerField current = new IntegerField("Current Stock");
        current.setValue(article.getStockLevel());
        current.setReadOnly(true);

        // addiert die änderung zu aktuellem Bestand
        IntegerField change = new IntegerField("Change (+/-)");
        change.setValue(0);

        FormLayout form = new FormLayout(name, number, current, change);
        add(form);

        Button cancel = new Button("Cancel", e -> close());
        Button save = new Button("Save", e -> {
            // WICHTIG: gelb unterstrichende != null und == null nicht entfernen!!! sonst klappts nicht. Intellij ist zu optimistisch
            // wärend der runtime könnte "null" existieren
            int oldStock = article.getStockLevel() != null ? article.getStockLevel() : 0;
            int delta = change.getValue() != null ? change.getValue() : 0;
            int newStock = oldStock + delta;

            if (newStock < 0) {
                Notification.show("Stock cannot be negative.");
                return;
            }

            // Wegen oben im "WICHTIG:" genannten Problem con Intellij
            // WICHTIG: siehe oberes WICHTIG
            if (article.getStorageLocation() == null || article.getStorageLocation().isBlank()) {
                article.setStorageLocation("unknown");
            }
            // WICHTIG: siehe oberes WICHTIG
            if (article.getName() == null || article.getName().isBlank()) {
                article.setName("Unnamed");
            }

            article.setStockLevel(newStock);
            articleInfoService.save(article);
            Notification.show("Stock updated: " + oldStock + " → " + newStock);
            if (onSuccess != null) onSuccess.run();
            close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancel, save);
    }
}
