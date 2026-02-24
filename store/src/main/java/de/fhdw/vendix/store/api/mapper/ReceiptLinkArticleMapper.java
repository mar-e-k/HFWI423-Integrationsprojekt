package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.receipt.dto.line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.ReceiptArticle;
import org.mapstruct.Mapper;

@Mapper
public interface ReceiptLinkArticleMapper extends GenericEntityMapper<ReceiptArticle, ReceiptLineDTO> {}