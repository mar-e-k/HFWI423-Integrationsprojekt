package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.commons.api.mapper.GenericMapper;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptLinkArticle;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("DuplicatedCode")
public class ReceiptLinkArticleMapper implements GenericMapper<ReceiptLinkArticle, ReceiptLinkArticleDTO> {

    @Override
    public ReceiptLinkArticle toEntity(ReceiptLinkArticleDTO dto) {
        ReceiptLinkArticle receiptLinkArticle = new ReceiptLinkArticle();
        receiptLinkArticle.setId(dto.getId());
        receiptLinkArticle.setReceipt(new Receipt(dto.getReceiptId()));
        receiptLinkArticle.setArticle(new Article(dto.getArticleId()));
        receiptLinkArticle.setPrice(dto.getPrice());
        receiptLinkArticle.setAmount(dto.getAmount());
        receiptLinkArticle.setTaxRate(dto.getTaxRate());
        receiptLinkArticle.setOverridePrice(dto.getOverridePrice());
        receiptLinkArticle.setOverrideReason(dto.getOverrideReason());
        receiptLinkArticle.setDiscountedByPercent(dto.getDiscountedByPercent());
        return receiptLinkArticle;
    }

    @Override
    public ReceiptLinkArticleDTO toDto(ReceiptLinkArticle receiptLinkArticle) {
        ReceiptLinkArticleDTO dto = new ReceiptLinkArticleDTO();
        dto.setId(receiptLinkArticle.getId());
        dto.setReceiptId(receiptLinkArticle.getReceipt().getId());
        dto.setArticleId(receiptLinkArticle.getArticle().getId());
        dto.setPrice(receiptLinkArticle.getPrice());
        dto.setAmount(receiptLinkArticle.getAmount());
        dto.setTaxRate(receiptLinkArticle.getTaxRate());
        dto.setOverridePrice(receiptLinkArticle.getOverridePrice());
        dto.setOverrideReason(receiptLinkArticle.getOverrideReason());
        dto.setDiscountedByPercent(receiptLinkArticle.getDiscountedByPercent());
        return dto;
    }
}
