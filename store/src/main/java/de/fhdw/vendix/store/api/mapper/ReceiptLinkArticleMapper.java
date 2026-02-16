package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.OverrideReasonEnum;
import de.fhdw.vendix.commons.core.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.store.persistence.entity.Receipt;
import de.fhdw.vendix.store.persistence.entity.ReceiptArticle;
import de.fhdw.vendix.store.persistence.entity.imported.Article;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("DuplicatedCode")
public class ReceiptLinkArticleMapper implements GenericMapper<ReceiptArticle, ReceiptLinkArticleDTO> {

    @Override
    public ReceiptArticle toEntity(ReceiptLinkArticleDTO dto) {
        ReceiptArticle receiptArticle = new ReceiptArticle();
        receiptArticle.setId(dto.getId());
        receiptArticle.setReceipt(new Receipt(dto.getReceiptId()));
        receiptArticle.setArticle(new Article(dto.getArticleId()));
        receiptArticle.setPrice(dto.getPrice());
        receiptArticle.setAmount(dto.getAmount());
        receiptArticle.setTaxRate(dto.getTaxRate());
        receiptArticle.setOverridePrice(dto.getOverridePrice());
        receiptArticle.setOverrideReason(dto.getOverrideReason() != null ? dto.getOverrideReason().name() : null);
        receiptArticle.setDiscountedByPercent(dto.getDiscountedByPercent());
        receiptArticle.setDepositStatus(dto.getDepositStatus());
        return receiptArticle;
    }

    @Override
    public ReceiptLinkArticleDTO toDto(ReceiptArticle receiptArticle) {
        ReceiptLinkArticleDTO dto = new ReceiptLinkArticleDTO();
        dto.setId(receiptArticle.getId());
        dto.setReceiptId(receiptArticle.getReceipt().getId());
        dto.setArticleId(receiptArticle.getArticle().getId());
        dto.setPrice(receiptArticle.getPrice());
        dto.setAmount(receiptArticle.getAmount());
        dto.setTaxRate(receiptArticle.getTaxRate());
        dto.setOverridePrice(receiptArticle.getOverridePrice());
        dto.setOverrideReason(receiptArticle.getOverrideReason() != null ? OverrideReasonEnum.valueOf(receiptArticle.getOverrideReason()) : null);
        dto.setDiscountedByPercent(receiptArticle.getDiscountedByPercent());
        dto.setDepositStatus(receiptArticle.getDepositStatus());
        return dto;
    }
}
