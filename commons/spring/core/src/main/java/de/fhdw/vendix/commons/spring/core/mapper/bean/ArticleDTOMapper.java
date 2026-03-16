package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface ArticleDTOMapper extends DTOMapper<ArticleDTO, ArticleRequestDTO, ArticleResponseDTO> {

    @Override
    ArticleDTO toDomainDTO(ArticleRequestDTO dto);

    @Override
    ArticleResponseDTO toResponseDTO(ArticleDTO articleDTO);

    @Override
    List<ArticleDTO> toDomainDTOs(Iterable<ArticleRequestDTO> requestDTOs);

    @Override
    List<ArticleResponseDTO> toResponseDTOs(Iterable<ArticleDTO> domainDTOs);
}