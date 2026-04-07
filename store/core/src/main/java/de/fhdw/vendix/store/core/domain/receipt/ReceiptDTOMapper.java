package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.register.RegisterDTOMapper;
import de.fhdw.vendix.store.core.domain.store.StoreDTOMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreDTOMapper.class,
                RegisterDTOMapper.class
        }
)
public interface ReceiptDTOMapper extends DTOMapper<ReceiptDTO, ReceiptRequestDTO, ReceiptResponseDTO> {

    @Override
    ReceiptDTO toDomainDTO(ReceiptRequestDTO dto);

    @Override
    ReceiptResponseDTO toResponseDTO(ReceiptDTO receiptDTO);

    @Override
    Set<ReceiptDTO> toDomainDTOs(Iterable<ReceiptRequestDTO> requestDTOs);

    @Override
    Set<ReceiptResponseDTO> toResponseDTOs(Iterable<ReceiptDTO> domainDTOs);
}