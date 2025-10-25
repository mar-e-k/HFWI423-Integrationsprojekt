package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;

import java.util.List;

public interface ArticleService {

    ArticleResponseDTO createNewArticle(ArticleRequestDTO newArticleRequestDTO);

    List<ArticleResponseDTO> findAllArticles();

    ArticleResponseDTO findArticleById(Long id);

    ArticleResponseDTO updateArticle(Long id, ArticleRequestDTO updatedArticleRequestDTO);

    void deleteArticle(Long id);
}