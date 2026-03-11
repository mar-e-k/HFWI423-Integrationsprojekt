package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArticleDTOMapper extends DTOMapper<ArticleDTO, ArticleRequestDTO, ArticleResponseDTO> {

    ArticleDTOMapper INSTANCE = Mappers.getMapper(ArticleDTOMapper.class);

    @Override
    ArticleDTO toDTO(ArticleRequestDTO dto);

    @Override
    ArticleResponseDTO toResponseDTO(ArticleDTO articleDTO);
}