package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.spring.annotation.UIScope;
import de.fhdw.vendix.commons.spring.web.api.store.ArticleApi;
import de.fhdw.vendix.pos.ui.register.RegisterState;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class ArticleSearchController {

    private final ArticleApi articleApi;
    private final RegisterState state;

    public ArticleSearchController(ArticleApi articleApi, RegisterState state) {
        this.articleApi = articleApi;
        this.state = state;
    }

    public void search(String gtin) {
        try {
            var response = articleApi.getArticleByGtin(gtin);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                state.setSelectedArticle(response.getBody());

            } else {
                state.setSelectedArticle(null);
            }

        } catch (Exception e) {
            state.setSelectedArticle(null);
        }
    }
}