package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherResponseDTO;
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
public interface ReceiptVoucherDTOMapper extends DTOMapper<ReceiptVoucherDTO, ReceiptVoucherRequestDTO, ReceiptVoucherResponseDTO> {

    @Override
    ReceiptVoucherDTO toDomainDTO(ReceiptVoucherRequestDTO dto);

    @Override
    ReceiptVoucherResponseDTO toResponseDTO(ReceiptVoucherDTO receiptVoucherDTO);

    @Override
    Set<ReceiptVoucherDTO> toDomainDTOs(Iterable<ReceiptVoucherRequestDTO> requestDTOs);

    @Override
    Set<ReceiptVoucherResponseDTO> toResponseDTOs(Iterable<ReceiptVoucherDTO> domainDTOs);
}