package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.article.ArticleDTOMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptDTOMapper;
import org.mapstruct.Mapper;

import java.util.Set;

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
    Set<ReceiptLineDTO> toDomainDTOs(Iterable<ReceiptLineRequestDTO> requestDTOs);

    @Override
    Set<ReceiptLineResponseDTO> toResponseDTOs(Iterable<ReceiptLineDTO> domainDTOs);
}