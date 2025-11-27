package de.fhdw.kassensystem.rest.proxy.services;

import de.fhdw.commons.api.controller.ArticleAPI;
import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.kassensystem.utility.FilialClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleProxyService extends AbstractProxyService implements ArticleAPI {

    public ArticleProxyService(FilialClient filialClient) {
        super(filialClient);
    }

    @Override
    public List<ArticleDTO> findAll() {
        return filialClient.getWebClient()
                .get()
                .uri("/api/article")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ArticleDTO>>(){})
                .block();
    }

    @Override
    public Optional<ArticleDTO> findById(Long id) {
        return filialClient.getWebClient()
                .get()
                .uri("/api/article/id/{id}", id)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }

    @Override
    public Optional<ArticleDTO> findByArticleNumber(String gtin) {
        return filialClient.getWebClient()
                .get()
                .uri("/api/article/gtin/{gtin}", gtin)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .blockOptional();
    }
}