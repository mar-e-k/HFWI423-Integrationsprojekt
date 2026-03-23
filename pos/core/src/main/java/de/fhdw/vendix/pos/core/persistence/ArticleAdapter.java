package de.fhdw.vendix.pos.core.persistence;

import org.springframework.stereotype.Service;

@Service
public class ArticleAdapter  {

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