package de.fhdw.vendix.commons.api.utility.checkout;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Eine einzelne Bon-Position beim Checkout.
 *
 * @param articleId      ID des Artikels aus der Artikel-Tabelle
 * @param articleAmount  Stückzahl (mindestens 1)
 * @param discountPercent Optionaler Rabatt in Prozent (0–100), null = kein Rabatt
 */
public record CheckoutLineDTO(
        Long articleId,
        Long articleAmount,
        @Nullable BigDecimal discountPercent
) implements EmbeddableDTO {

    public CheckoutLineDTO {
        if (articleId == null || articleId < 1) {
            throw new IllegalArgumentException("CheckoutLineDTO: 'articleId' muss >= 1 sein");
        }
        if (articleAmount == null || articleAmount < 1) {
            throw new IllegalArgumentException("CheckoutLineDTO: 'articleAmount' muss >= 1 sein");
        }
        if (discountPercent != null) {
            if (discountPercent.compareTo(BigDecimal.ZERO) < 0
                    || discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("CheckoutLineDTO: 'discountPercent' muss zwischen 0 und 100 liegen");
            }
        }
    }
}