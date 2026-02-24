package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.structure.mapper.GenericEntityMapper;
import de.fhdw.vendix.store.persistence.entity.Receipt;
import org.mapstruct.Mapper;

@Mapper
public interface ReceiptMapper extends GenericEntityMapper<Receipt, ReceiptDTO> {}