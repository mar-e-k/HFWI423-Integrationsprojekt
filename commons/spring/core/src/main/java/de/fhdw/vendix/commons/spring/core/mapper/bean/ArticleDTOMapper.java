package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.DTOMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = DTOMapperConfig.class)
public interface ArticleDTOMapper extends DTOMapper<ArticleDTO, ArticleRequestDTO, ArticleResponseDTO> {

    @Override
    ArticleDTO toDomainDTO(ArticleRequestDTO dto);

    @Override
    ArticleResponseDTO toResponseDTO(ArticleDTO articleDTO);
}