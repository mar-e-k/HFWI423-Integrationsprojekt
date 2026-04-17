package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.pos.ui.register.RegisterState;

public final class ArticleStatusPanel extends VerticalLayout {

    private final RegisterState state;

    private final Div status = new Div();
    private final Div details = new Div();

    public ArticleStatusPanel(RegisterState state) {
        this.state = state;

        setWidthFull();

        status.getStyle().set("font-weight", "bold");

        add(status, details);

        state.addListener(this::refresh);

        refresh();
    }

    private void refresh() {
        state.getSelectedArticle()
                .ifPresentOrElse(
                        this::showArticle,
                        this::showNotFound
                );
    }

    private void showArticle(ArticleDTO article) {
        status.setText("FOUND");

        details.setText(
                article.name() + " | " +
                        article.description() + " | " +
                        "€" + article.sellingPrice() + " | " +
                        "Tax: " + article.taxRate() + "%"
        );
    }

    private void showNotFound() {
        status.setText("NOT FOUND");
        details.setText("");
    }
}