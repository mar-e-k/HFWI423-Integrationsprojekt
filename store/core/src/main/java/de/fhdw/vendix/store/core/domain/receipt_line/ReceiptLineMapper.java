package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.article.ArticleMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.embeddable.discount_override.DiscountOverrideMapper;
import de.fhdw.vendix.store.core.embeddable.price_override.PriceOverrideMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                ReceiptMapper.class,
                ArticleMapper.class,
                DiscountOverrideMapper.class,
                PriceOverrideMapper.class,
        }
)
public interface ReceiptLineMapper extends EntityMapper<ReceiptLine, ReceiptLineDTO> {

    @Override
    ReceiptLineDTO toDTO(ReceiptLine entity);

    @Override
    ReceiptLine toEntity(ReceiptLineDTO receiptLineDTO);

    @Override
    Set<ReceiptLineDTO> toDTOs(Iterable<ReceiptLine> entities);

    @Override
    Set<ReceiptLine> toEntities(Iterable<ReceiptLineDTO> receiptLineDTOS);
}