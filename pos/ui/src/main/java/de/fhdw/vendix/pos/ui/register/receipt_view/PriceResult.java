package de.fhdw.vendix.pos.ui.register.receipt_view;

import java.math.BigDecimal;

public record PriceResult(
        BigDecimal originalTotal,
        BigDecimal finalTotal,
        boolean discounted,
        boolean overridden
) {}