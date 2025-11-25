package de.fhdw.commons.api.controller;

import de.fhdw.commons.api.dto.ArticleDTO;

import java.util.List;
import java.util.Optional;

public interface ArticleApi {
    List<ArticleDTO> findAll();
    Optional<ArticleDTO> findById(Long id);
    Optional<ArticleDTO> findByArticleNumber(String articleNumber);
}