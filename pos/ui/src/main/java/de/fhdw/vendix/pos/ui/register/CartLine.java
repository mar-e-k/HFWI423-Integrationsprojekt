package de.fhdw.vendix.pos.ui.register;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.PriceOverrideDTO;
import de.fhdw.vendix.pos.ui.register.receipt_view.PriceResult;

import java.math.BigDecimal;
import java.util.Objects;

public record CartLine(
        ReceiptLineDTO line,
        ArticleDTO article
) {
    public CartLine {
        if (line == null) {
            throw new IllegalArgumentException("Parameter 'line' cannot be null");
        }
        if (article == null) {
            throw new IllegalArgumentException("Parameter 'line' cannot be null");
        }
        if (!Objects.equals(line.articleId(), article.id())) {
            throw new IllegalArgumentException("Line article ID and article ID do not match");
        }
    }

    public PriceResult calculatePrice() {

        BigDecimal baseUnitPrice = article.sellingPrice();
        BigDecimal quantity = BigDecimal.valueOf(line.articleAmount());

        BigDecimal originalTotal = baseUnitPrice.multiply(quantity);

        PriceOverrideDTO priceOverride = line.priceOverride();
        DiscountOverrideDTO discountOverride = line.discountOverride();

        boolean overridden = priceOverride != null;
        boolean discounted = discountOverride != null;

        BigDecimal finalUnitPrice = baseUnitPrice;

        if (priceOverride != null) {
            finalUnitPrice = priceOverride.amount();
        }

        BigDecimal total = finalUnitPrice.multiply(quantity);

        if (discountOverride != null) {
            BigDecimal discountFactor = BigDecimal.ONE
                    .subtract(discountOverride.amount()
                            .divide(BigDecimal.valueOf(100)));

            total = total.multiply(discountFactor);
        }

        return new PriceResult(
                originalTotal,
                total,
                discounted,
                overridden
        );
    }

    public static ReceiptLineDTO toReceiptLine(CartLine cartLine) {
        return new ReceiptLineDTO(
                0L,
                0L,
                cartLine.line().articleId(),
                cartLine.line().articleAmount(),
                cartLine.line().discountOverride(),
                cartLine.line().priceOverride()
        );
    }
}