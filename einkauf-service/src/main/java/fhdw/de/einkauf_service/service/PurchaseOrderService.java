package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.OrderRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;

public interface PurchaseOrderService {
    OrderResponseDTO createAndSendOrder(OrderRequestDTO request);
}
