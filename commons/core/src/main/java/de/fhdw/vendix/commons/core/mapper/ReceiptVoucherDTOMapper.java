package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReceiptVoucherDTOMapper extends DTOMapper<ReceiptVoucherDTO, ReceiptVoucherRequestDTO, ReceiptVoucherResponseDTO> {

    ReceiptVoucherDTOMapper INSTANCE = Mappers.getMapper(ReceiptVoucherDTOMapper.class);

    @Override
    ReceiptVoucherDTO toDTO(ReceiptVoucherRequestDTO dto);

    @Override
    ReceiptVoucherResponseDTO toResponseDTO(ReceiptVoucherDTO receiptVoucherDTO);
}