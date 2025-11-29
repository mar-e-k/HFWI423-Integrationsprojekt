package de.fhdw.kassensystem.persistance.service.proxy;

import de.fhdw.commons.api.controller.ArticleAPI;
import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.kassensystem.utility.StoreClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleProxyService extends AbstractProxyService implements ArticleAPI {

    public ArticleProxyService(StoreClient storeClient) {
        super(storeClient);
    }

    @Override
    public List<ArticleDTO> findAll() {
        return storeClient.getWebClient()
                .get()
                .uri("/api/article")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ArticleDTO>>(){})
                .block();
    }

    @Override
    public Optional<ArticleDTO> findById(Long id) {
        return storeClient.getWebClient()
                .get()
                .uri("/api/article/id/{id}", id)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    @Override
    public Optional<ArticleDTO> findByArticleNumber(String gtin) {
        return storeClient.getWebClient()
                .get()
                .uri("/api/article/gtin/{gtin}", gtin)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }
}