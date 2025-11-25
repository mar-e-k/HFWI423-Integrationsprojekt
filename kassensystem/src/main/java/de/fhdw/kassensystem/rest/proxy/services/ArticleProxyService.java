package de.fhdw.kassensystem.rest.proxy.services;

import de.fhdw.commons.api.controller.ArticleApi;
import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.kassensystem.utility.FilialClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleProxyService implements ArticleApi {

    private final FilialClient filialClient;

    public ArticleProxyService(FilialClient filialClient) {
        this.filialClient = filialClient;
    }

    @Override
    public List<ArticleDTO> findAll() {
        return filialClient.getWebClient()
                .get()
                .uri("/api/article")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ArticleDTO>>() {})
                .block();
    }

    @Override
    public Optional<ArticleDTO> findById(Long id) {
        return Optional.ofNullable(filialClient.getWebClient()
                .get()
                .uri("/api/article/id/{id}", id)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .block());
    }

    @Override
    public Optional<ArticleDTO> findByArticleNumber(String articleNumber) {
        return Optional.ofNullable(filialClient.getWebClient()
                .get()
                .uri("/api/article/number/{articleNumber}", articleNumber)
                .retrieve()
                .bodyToMono(ArticleDTO.class)
                .block());
    }
}
