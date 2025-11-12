package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.OrderResponseDTO;

import java.util.List;

public interface PurchaseOrderService {
    List<OrderResponseDTO> createAndSendOrdersFromCart();
}
