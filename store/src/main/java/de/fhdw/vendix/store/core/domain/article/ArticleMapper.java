package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface ArticleMapper extends GenericEntityMapper<Article, ArticleDTO> {}