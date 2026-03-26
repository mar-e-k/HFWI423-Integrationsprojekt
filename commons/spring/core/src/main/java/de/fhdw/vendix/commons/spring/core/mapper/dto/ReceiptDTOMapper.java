package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreDTOMapper.class,
                RegisterDTOMapper.class,
                AccountDTOMapper.class
        }
)
public interface ReceiptDTOMapper extends DTOMapper<ReceiptDTO, ReceiptRequestDTO, ReceiptResponseDTO> {

    @Override
    ReceiptDTO toDomainDTO(ReceiptRequestDTO dto);

    @Override
    ReceiptResponseDTO toResponseDTO(ReceiptDTO receiptDTO);

    @Override
    List<ReceiptDTO> toDomainDTOs(Iterable<ReceiptRequestDTO> requestDTOs);

    @Override
    List<ReceiptResponseDTO> toResponseDTOs(Iterable<ReceiptDTO> domainDTOs);
}