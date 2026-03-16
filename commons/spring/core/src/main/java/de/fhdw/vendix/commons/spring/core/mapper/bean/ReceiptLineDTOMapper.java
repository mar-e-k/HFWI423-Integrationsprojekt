package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.dto.ReceiptLineResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                ReceiptDTOMapper.class,
                ArticleDTOMapper.class,
        }
)
public interface ReceiptLineDTOMapper extends DTOMapper<ReceiptLineDTO, ReceiptLineRequestDTO, ReceiptLineResponseDTO> {

    @Override
    ReceiptLineDTO toDomainDTO(ReceiptLineRequestDTO dto);

    @Override
    ReceiptLineResponseDTO toResponseDTO(ReceiptLineDTO receiptLineDTO);

    @Override
    List<ReceiptLineDTO> toDomainDTOs(Iterable<ReceiptLineRequestDTO> requestDTOs);

    @Override
    List<ReceiptLineResponseDTO> toResponseDTOs(Iterable<ReceiptLineDTO> domainDTOs);
}