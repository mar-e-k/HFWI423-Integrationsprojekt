package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.domain.article.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface ArticleDTOMapper extends DTOMapper<ArticleDTO, ArticleRequestDTO, ArticleResponseDTO> {

    @Override
    ArticleDTO toDomainDTO(ArticleRequestDTO dto);

    @Override
    ArticleResponseDTO toResponseDTO(ArticleDTO articleDTO);

    @Override
    Set<ArticleDTO> toDomainDTOs(Iterable<ArticleRequestDTO> requestDTOs);

    @Override
    Set<ArticleResponseDTO> toResponseDTOs(Iterable<ArticleDTO> domainDTOs);
}