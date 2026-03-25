package de.fhdw.vendix.pos.core.persistence;

import de.fhdw.vendix.commons.api.domain.article.web.ArticleCommandApi;
import de.fhdw.vendix.commons.api.domain.article.web.ArticleQueryApi;
import de.fhdw.vendix.commons.spring.web.AbstractApiClient;
import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;
import org.springframework.stereotype.Service;

@Service
class ArticleClientAdapter extends AbstractApiClient implements ArticleQueryApi, ArticleCommandApi {


    protected ArticleClientAdapter(JwtService jwtService) {
        super(jwtService, "localhost:8080");
    }

    @Override
    protected JwtPayload buildJwtPayload() {
        return null;
    }

//    public ArticleProxyService(StoreClient storeClient) {
//        super(storeClient);
//    }
//
//    public List<ArticleDTO> findAll() {
//        return getWebClient()
//                .get()
//                .uri(ArticleEndpoints.BASE)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<ArticleDTO>>(){})
//                .block();
//    }
//
//    @Override
//    public Optional<ArticleDTO> findByID(long id) {
//        return getWebClient()
//                .get()
//                .uri(ArticleEndpoints.BY_ID, id)
//                .retrieve()
//                .bodyToMono(ArticleDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
//
//    @Override
//    public Optional<ArticleDTO> findByGTIN(long gtin) {
//        return getWebClient()
//                .get()
//                .uri(ArticleEndpoints.BY_GTIN, gtin)
//                .retrieve()
//                .bodyToMono(ArticleDTO.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
//                .blockOptional();
//    }
}