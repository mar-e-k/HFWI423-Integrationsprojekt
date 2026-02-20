package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface ReceiptDTOMapper extends GenericDTOMapper<ReceiptDTO, ReceiptRequestDTO, ReceiptResponseDTO> {}