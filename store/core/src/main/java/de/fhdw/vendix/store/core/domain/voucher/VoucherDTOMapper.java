package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.persistance.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.article.ArticleDTOMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptDTOMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                ReceiptDTOMapper.class,
                ArticleDTOMapper.class,
        }
)
public interface VoucherDTOMapper extends DTOMapper<VoucherDTO, VoucherRequestDTO, VoucherResponseDTO> {

    @Override
    VoucherDTO toDomainDTO(VoucherRequestDTO dto);

    @Override
    VoucherResponseDTO toResponseDTO(VoucherDTO voucherDTO);

    @Override
    List<VoucherDTO> toDomainDTOs(Iterable<VoucherRequestDTO> requestDTOs);

    @Override
    List<VoucherResponseDTO> toResponseDTOs(Iterable<VoucherDTO> domainDTOs);
}