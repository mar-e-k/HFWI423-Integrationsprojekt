package de.fhdw.vendix.pos.ui.register.article_search;

import com.vaadin.flow.spring.annotation.UIScope;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.pos.ui.register.RegisterState;
import de.fhdw.vendix.pos.web.client.store.StoreClients;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@UIScope
public class ArticleSearchController {

    private final ArticleProxyService articleProxyService;
    private final RegisterState state;

    public ArticleSearchController(StoreClients storeClients, RegisterState state) {
        this.articleProxyService = storeClients.article();
        this.state = state;
    }

    public void search(String gtin) {
        try {
            var response = articleProxyService.getArticleByGtin(gtin);
            @Nullable ArticleDTO article = response.getBody();

            if (response.getStatusCode().is2xxSuccessful()
                    && article != null) {

                state.setSelectedArticle(article);
            } else {
                state.setSelectedArticle(null);
            }

        } catch (Exception e) {
            state.setSelectedArticle(null);
        }
    }
}
