package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;

public final class ArticleStatusPanel extends VerticalLayout {

    private final Div status = new Div();
    private final Div details = new Div();

    public ArticleStatusPanel() {
        setWidthFull();
        status.getStyle().set("font-weight", "bold");
        add(status, details);
    }

    public void showArticle(ArticleDTO article) {
        status.setText("FOUND");

        details.setText(
                article.name() + " | " +
                        article.description() + " | " +
                        "€" + article.sellingPrice() + " | " +
                        "Tax: " + article.taxRate() + "%"
        );
    }

    public void showNotFound() {
        status.setText("NOT FOUND");
        details.setText("");
    }

    public void showError(String msg) {
        status.setText(msg);
        details.setText("");
    }

    public void showError(Throwable error) {
        status.setText(error.getMessage());
        details.setText("");
    }
}