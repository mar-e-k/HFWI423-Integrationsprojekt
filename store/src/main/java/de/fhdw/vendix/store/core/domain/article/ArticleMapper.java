package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArticleMapper extends EntityMapper<Article, ArticleDTO> {

    ArticleMapper INSTANCE = Mappers.getMapper(ArticleMapper.class);

    @Override
    ArticleDTO toDTO(Article entity);

    @Override
    Article toEntity(ArticleDTO articleDTO);
}