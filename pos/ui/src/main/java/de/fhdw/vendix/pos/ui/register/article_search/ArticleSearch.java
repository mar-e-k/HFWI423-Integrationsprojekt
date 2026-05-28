package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhdw.vendix.pos.ui.register.RegisterState;

public final class ArticleSearch extends VerticalLayout {

    public ArticleSearch(ArticleSearchController controller, RegisterState state) {

        ArticleSearchBar searchBar = new ArticleSearchBar(controller);
        ArticleStatusPanel statusPanel = new ArticleStatusPanel(state);

        add(searchBar, statusPanel);
    }
}