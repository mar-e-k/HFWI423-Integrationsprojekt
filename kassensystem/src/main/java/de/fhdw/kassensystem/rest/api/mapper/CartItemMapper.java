package de.fhdw.kassensystem.rest.api.mapper;

import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.commons.api.dto.OverrideReasonEnum;
import de.fhdw.commons.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.commons.api.mapper.GenericMapper;
import de.fhdw.kassensystem.view.cashier.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemMapper implements GenericMapper<CartItem, ReceiptLinkArticleDTO> {

    public CartItemMapper() {
        super();
    }

    @Override
    public CartItem toEntity(ReceiptLinkArticleDTO dto) {
        CartItem cartItem = new CartItem();
        cartItem.setArticle(new ArticleDTO(dto.getArticleId()));
        cartItem.setQuantity(dto.getAmount());
        cartItem.setOverriddenPrice(dto.getOverridePrice());
        cartItem.setDiscountPercent(dto.getDiscountedByPercent());
        cartItem.setDiscountedQuantity(dto.getAmount());
        return cartItem;
    }

    @Override
    public ReceiptLinkArticleDTO toDto(CartItem cartItem) {
        ReceiptLinkArticleDTO dto = new ReceiptLinkArticleDTO();
        dto.setArticleId(cartItem.getArticle().getId());
        dto.setPrice(BigDecimal.valueOf(cartItem.getArticle().getSellingPrice()));
        dto.setAmount(cartItem.getDiscountedQuantity() == 0 ? cartItem.getQuantity() : cartItem.getDiscountedQuantity());
        dto.setTaxRate(BigDecimal.valueOf(cartItem.getArticle().getTaxRatePercent()));
        dto.setOverridePrice(cartItem.getOverriddenPrice());
        dto.setOverrideReason(OverrideReasonEnum.MANUAL_OVERRIDE);
        dto.setDiscountedByPercent(cartItem.getDiscountPercent());
        return dto;
    }
}
