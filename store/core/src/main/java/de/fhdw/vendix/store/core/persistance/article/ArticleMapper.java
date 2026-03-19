package de.fhdw.vendix.store.core.persistance.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface ArticleMapper extends EntityMapper<Article, ArticleDTO> {

    @Override
    @Mapping(target = "gtin", source = "articleNumber")
    @Mapping(target = "taxRate", source = "taxRatePercent")
    @Mapping(target = "stock", source = "stockLevel")
    @Mapping(target = "isAvailable", source = "available")
    @Mapping(target = "isDeposit", source = "hasDeposit")
    ArticleDTO toDTO(Article entity);

    @Override
    @InheritInverseConfiguration
    Article toEntity(ArticleDTO articleDTO);

    @Override
    List<ArticleDTO> toDTOs(Iterable<Article> entities);

    @Override
    List<Article> toEntities(Iterable<ArticleDTO> articleDTOS);
}