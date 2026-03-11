package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiptDTOMapper extends DTOMapper<ReceiptDTO, ReceiptRequestDTO, ReceiptResponseDTO> {

    ReceiptDTOMapper INSTANCE = Mappers.getMapper(ReceiptDTOMapper.class);

    @Override
    ReceiptDTO toDTO(ReceiptRequestDTO dto);

    @Override
    ReceiptResponseDTO toResponseDTO(ReceiptDTO receiptDTO);
}