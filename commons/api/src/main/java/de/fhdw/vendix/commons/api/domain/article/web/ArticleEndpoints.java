package de.fhdw.vendix.commons.api.domain.article.web;

import de.fhdw.vendix.commons.api.structure.web.WebEndpoint;

import java.util.Set;

public final class ArticleEndpoints implements WebEndpoint {

    public static final String BASE = "/api/article";

    public static final String BY_ID = BASE + "/id/{id}";
    public static final String BY_GTIN =  BASE + "/gtin/{gtin}";

    private ArticleEndpoints() {}

    @Override
    public String getBasePath() {
        return BASE;
    }

    @Override
    public Set<String> getEndpoints() {
        return Set.of(
                BY_ID,
                BY_GTIN
        );
    }
}