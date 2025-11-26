package fhdw.de.einkauf_service.dto;

public record OrderItemResponseDTO(Long articleId, String articleName, Integer quantity, Double purchasePrice) {}