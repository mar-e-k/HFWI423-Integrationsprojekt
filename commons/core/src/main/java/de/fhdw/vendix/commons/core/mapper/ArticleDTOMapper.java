package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import org.mapstruct.Mapper;

@Mapper
public interface ArticleDTOMapper extends GenericDTOMapper<ArticleDTO, ArticleRequestDTO, ArticleResponseDTO> {}