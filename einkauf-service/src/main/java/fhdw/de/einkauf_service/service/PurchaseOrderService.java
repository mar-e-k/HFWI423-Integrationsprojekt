package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;

import java.util.List;

public interface PurchaseOrderService {
    List<OrderResponseDTO> createAndSendOrdersFromCart();

    List<OrderResponseDTO> getOrderHistory(OrderFilterDTO filter);

    OrderResponseDTO getOrderDetails(Long id);

    OrderResponseDTO reorder(Long originalOrderId, List<OrderItemRequestDTO> itemsToReorder);
}
