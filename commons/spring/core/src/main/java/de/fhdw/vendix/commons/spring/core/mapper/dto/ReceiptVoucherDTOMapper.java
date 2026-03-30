package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherResponseDTO;
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
public interface ReceiptVoucherDTOMapper extends DTOMapper<ReceiptVoucherDTO, ReceiptVoucherRequestDTO, ReceiptVoucherResponseDTO> {

    @Override
    ReceiptVoucherDTO toDomainDTO(ReceiptVoucherRequestDTO dto);

    @Override
    ReceiptVoucherResponseDTO toResponseDTO(ReceiptVoucherDTO receiptVoucherDTO);

    @Override
    List<ReceiptVoucherDTO> toDomainDTOs(Iterable<ReceiptVoucherRequestDTO> requestDTOs);

    @Override
    List<ReceiptVoucherResponseDTO> toResponseDTOs(Iterable<ReceiptVoucherDTO> domainDTOs);
}