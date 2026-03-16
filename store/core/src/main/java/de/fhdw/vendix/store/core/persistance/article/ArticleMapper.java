package de.fhdw.vendix.store.core.persistance.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface ArticleMapper extends EntityMapper<Long, Article, ArticleDTO> {

    @Override
    ArticleDTO toDTO(Article entity);

    @Override
    Article toEntity(ArticleDTO articleDTO);

    @Override
    List<ArticleDTO> toDTOs(Iterable<Article> entities);

    @Override
    List<Article> toEntities(Iterable<ArticleDTO> articleDTOS);
}