package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiptLineDTOMapper extends DTOMapper<ReceiptLineDTO, ReceiptLineRequestDTO, ReceiptLineResponseDTO> {

    ReceiptLineDTOMapper INSTANCE = Mappers.getMapper(ReceiptLineDTOMapper.class);

    @Override
    ReceiptLineDTO toDTO(ReceiptLineRequestDTO dto);

    @Override
    ReceiptLineResponseDTO toResponseDTO(ReceiptLineDTO receiptLineDTO);
}