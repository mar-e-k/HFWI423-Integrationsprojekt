package fhdw.de.einkauf_service.dto;

import java.time.LocalDate;

public record OrderResponseDTO(String orderNumber, LocalDate expectedDeliveryDate) {}
