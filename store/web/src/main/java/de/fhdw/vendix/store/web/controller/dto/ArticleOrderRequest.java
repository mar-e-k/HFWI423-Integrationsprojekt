package de.fhdw.vendix.store.web.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for the test-order endpoints.
 *
 * <pre>
 * {
 *   "storeId":   1,
 *   "articleId": 5,
 *   "amount":    10
 * }
 * </pre>
 */
public record ArticleOrderRequest(
        @NotNull @Min(1) Long storeId,
        @NotNull @Min(1) Long articleId,
        @NotNull @Min(1) Long amount
) {}
