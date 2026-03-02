package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper
public interface ReceiptLinkArticleMapper extends EntityMapper<ReceiptArticle, ReceiptLineDTO> {}