package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.pos.ui.register.RegisterState;

public final class ArticleSearch extends VerticalLayout {

    private final ArticleProxyService service;
    private final RegisterState state;

    public ArticleSearch(ArticleProxyService service, RegisterState state) {
        this.service = service;
        this.state = state;

        ArticleSearchBar searchBar = new ArticleSearchBar(this::searchArticle);
        ArticleStatusPanel statusPanel = new ArticleStatusPanel();

        add(searchBar, statusPanel);
    }

    private void searchArticle(String gtin) {
        try {
            var response = service.getArticleByGtin(gtin);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                state.setSelectedArticle(response.getBody());
            } else {
                state.setSelectedArticle(null);
            }

        } catch (Exception e) {
            state.setSelectedArticle(null);
        }
    }
}