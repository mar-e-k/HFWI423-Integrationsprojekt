package de.fhdw.vendix.commons.api.article;

import de.fhdw.vendix.commons.api.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.article.dto.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.article.dto.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.marker.mapper.GenericDTOMapper;
import org.mapstruct.Mapper;

@Mapper
public interface ArticleDTOMapper extends GenericDTOMapper<ArticleDTO, ArticleRequestDTO, ArticleResponseDTO> {}