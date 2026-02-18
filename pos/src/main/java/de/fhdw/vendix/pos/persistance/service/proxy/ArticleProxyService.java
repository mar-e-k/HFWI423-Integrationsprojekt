package de.fhdw.vendix.pos.persistance.service.proxy;

import de.fhdw.vendix.commons.core.api.dto.ArticleDTO;
import de.fhdw.vendix.pos.utility.StoreClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleProxyService extends AbstractProxyService {

    public ArticleProxyService(StoreClient storeClient) {
        super(storeClient);
    }

    public List<ArticleDTO> findAll() {
        return getWebClient()
                .get()
                .uri("/api/article")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ArticleDTO>>(){})
                .block();
    }

    public Optional<ArticleDTO> findById(Long id) {
        return getWebClient()
                .get()
                .uri("/api/article/id/{id}", id)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    public Optional<ArticleDTO> findByArticleNumber(String gtin) {
        return getWebClient()
                .get()
                .uri("/api/article/gtin/{gtin}", gtin)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }
}