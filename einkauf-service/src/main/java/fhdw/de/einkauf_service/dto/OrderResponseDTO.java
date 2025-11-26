package fhdw.de.einkauf_service.dto;

import fhdw.de.einkauf_service.enums.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(Long id,
                               String orderNumber,
                               LocalDateTime orderDate,
                               String supplierName,
                               Long supplierId,
                               OrderStatus status,
                               Double totalAmount,
                               LocalDate expectedDeliveryDate,
                               List<OrderItemResponseDTO> items) {}
