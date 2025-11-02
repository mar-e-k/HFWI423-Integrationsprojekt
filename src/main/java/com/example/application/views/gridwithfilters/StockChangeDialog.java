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

        // read only --> man kanns nicht bearbeiten
        TextField name = new TextField("Article");
        name.setValue(article.getName());
        name.setReadOnly(true);

        TextField number = new TextField("Article Number");
        number.setValue(article.getArticleNumber());
        number.setReadOnly(true);

        // bearbeitbarer Bestand
        // WICHTIG: gelb unterstrichenes != null und == null nicht entfernen!!! sonst klappts nicht. Intellij ist zu optimistisch
        IntegerField stock = new IntegerField("Stock");
        stock.setMin(0);
        stock.setStepButtonsVisible(true);
        stock.setValue(article.getStockLevel() != null ? article.getStockLevel() : 0);

        FormLayout form = new FormLayout(name, number, stock);
        add(form);

        Button cancel = new Button("Cancel", e -> close());
        Button save = new Button("Save", e -> {
            Integer v = stock.getValue();
            if (v == null || v < 0) {
                Notification.show("Please enter valid Stock ( >= 0");
                return;
            }

            article.setStockLevel(v);

            // Sicherstellung, dass die Daten nicht null sind
            // WICHTIG: siehe oberes WICHTIG
            if (article.getStorageLocation() == null) {
                article.setStorageLocation("");   // oder unbekannt
            }
            // WICHTIG: siehe oberes WICHTIG
            if (article.getName() == null) {
                article.setName("No Name");
            }

            articleInfoService.save(article);
            Notification.show("Stock updated");
            if (onSuccess != null) onSuccess.run();
            close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancel, save);
    }
}
