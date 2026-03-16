package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

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
}