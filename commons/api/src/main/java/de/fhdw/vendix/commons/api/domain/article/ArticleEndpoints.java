package de.fhdw.vendix.commons.api.domain.article;

import de.fhdw.vendix.commons.api.structure.endpoints.EndpointProvider;

public final class ArticleEndpoints implements EndpointProvider {

    public static final String BASE = "/api/article";
    public static final String BY_ID = BASE + "/id/{id}";
    public static final String BY_GTIN =  BASE + "/gtin/{gtin}";

    private ArticleEndpoints() {}
}