package de.fhdw.vendix.store.core.persistance.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.store.core.persistance.EntityMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = EntityMapperConfig.class)
public interface ArticleMapper extends EntityMapper<Article, ArticleDTO> {

    @Override
    ArticleDTO toDTO(Article entity);

    @Override
    Article toEntity(ArticleDTO articleDTO);
}