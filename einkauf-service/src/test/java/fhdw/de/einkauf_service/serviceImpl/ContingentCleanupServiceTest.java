package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.entity.Contingent;
import fhdw.de.einkauf_service.entity.Order;
import fhdw.de.einkauf_service.enums.OrderStatus;
import fhdw.de.einkauf_service.repository.ContingentRepository;
import fhdw.de.einkauf_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContingentCleanupServiceTest {

    @Mock
    private ContingentRepository contingentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ContingentCleanupService service;

    private Contingent buildContingent(Long id, Long orderId) {
        return new Contingent(id, orderId, 1L, 1L, 0);
    }

    private Order buildOrder(Long id, String orderNumber) {
        Order o = new Order();
        o.setId(id);
        o.setOrderNumber(orderNumber);
        o.setStatus(OrderStatus.PLACED);
        return o;
    }

    @Test
    void cleanupContingents_noZeroContingents_noOrdersUpdated() {
        when(contingentRepository.findAllByAvailableQuantity(0)).thenReturn(List.of());

        service.cleanupContingents();

        verify(orderRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cleanupContingents_oneZeroContingent_setsOrderToDelivered() {
        Contingent contingent = buildContingent(1L, 10L);
        Order order = buildOrder(10L, "BE-1");

        when(contingentRepository.findAllByAvailableQuantity(0)).thenReturn(List.of(contingent));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        service.cleanupContingents();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    void cleanupContingents_orderNotFound_doesNotThrow() {
        Contingent contingent = buildContingent(1L, 99L);

        when(contingentRepository.findAllByAvailableQuantity(0)).thenReturn(List.of(contingent));
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatCode(() -> service.cleanupContingents()).doesNotThrowAnyException();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cleanupContingents_multipleZeroContingents_allRelatedOrdersSetToDelivered() {
        Contingent c1 = buildContingent(1L, 10L);
        Contingent c2 = buildContingent(2L, 20L);
        Order o1 = buildOrder(10L, "BE-1");
        Order o2 = buildOrder(20L, "BE-2");

        when(contingentRepository.findAllByAvailableQuantity(0)).thenReturn(List.of(c1, c2));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(o1));
        when(orderRepository.findById(20L)).thenReturn(Optional.of(o2));

        service.cleanupContingents();

        assertThat(o1.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(o2.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository, times(2)).save(any(Order.class));
    }
}
