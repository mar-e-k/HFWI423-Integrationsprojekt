package de.fhdw.vendix.pos.ui.register;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;

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
    }
}