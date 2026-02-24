package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.imported.Article;
import org.mapstruct.Mapper;

@Mapper
public interface ArticleMapper extends GenericEntityMapper<Article, ArticleDTO> {}