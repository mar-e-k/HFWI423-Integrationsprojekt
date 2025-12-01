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
        // discountedQuantity in CartItem ist für die Rabattberechnung auf Frontend-Seite.
        // Beim Mappen zurück zur Entität setzen wir es auf 0, da die Rabattlogik im Backend
        // auf Basis von discountedByPercent und dem Gesamtbetrag neu berechnet wird.
        cartItem.setDiscountedQuantity(0);
        cartItem.setDepositStatus(dto.getDepositStatus()); // DepositStatus setzen
        return cartItem;
    }

    @Override
    public ReceiptLinkArticleDTO toDto(CartItem cartItem) {
        ReceiptLinkArticleDTO dto = new ReceiptLinkArticleDTO();
        dto.setArticleId(cartItem.getArticle().getId());
        // Verwende getBaseUnitPrice(), das bereits überschriebene Preise und Pfandstatus berücksichtigt
        dto.setPrice(cartItem.getBaseUnitPrice());
        dto.setAmount(cartItem.getQuantity()); // Gesamte Menge des Artikels
        dto.setTaxRate(BigDecimal.valueOf(cartItem.getArticle().getTaxRatePercent()));
        dto.setOverridePrice(cartItem.getOverriddenPrice()); // Der ursprünglich überschriebene Preis, falls vorhanden
        // Setze OverrideReason nur, wenn ein Preis überschrieben wurde
        dto.setOverrideReason(cartItem.getOverriddenPrice() != null ? OverrideReasonEnum.MANUAL_OVERRIDE : null);
        dto.setDiscountedByPercent(cartItem.getDiscountPercent());
        dto.setDepositStatus(cartItem.getDepositStatus()); // DepositStatus setzen
        return dto;
    }
}
