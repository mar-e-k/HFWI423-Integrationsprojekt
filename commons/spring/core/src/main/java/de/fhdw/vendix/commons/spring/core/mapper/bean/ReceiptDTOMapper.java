package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.DTOMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = DTOMapperConfig.class)
public interface ReceiptDTOMapper extends DTOMapper<ReceiptDTO, ReceiptRequestDTO, ReceiptResponseDTO> {

    @Override
    ReceiptDTO toDomainDTO(ReceiptRequestDTO dto);

    @Override
    ReceiptResponseDTO toResponseDTO(ReceiptDTO receiptDTO);
}