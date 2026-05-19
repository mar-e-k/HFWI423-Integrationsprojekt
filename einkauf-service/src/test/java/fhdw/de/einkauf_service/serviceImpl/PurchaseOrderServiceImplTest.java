package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.amqp.EventPublisherPort;
import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.entity.*;
import fhdw.de.einkauf_service.enums.OrderStatus;
import fhdw.de.einkauf_service.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ShoppingCartSession cartSession;

    @Mock
    private ContingentRepository contingentRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private EventPublisherPort einkaufEventPublisher;

    @InjectMocks
    private PurchaseOrderServiceImpl service;

    @Mock
    private Query nativeQuery;

    @BeforeEach
    void setupNativeQuery() {
        when(entityManager.createNativeQuery("SELECT nextval('order_number_seq')")).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(1L);
    }

    // --- Helpers ---

    private Supplier buildActiveSupplier(Long id, String name) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setName(name);
        s.setActive(true);
        return s;
    }

    private Article buildAvailableArticle(Long id, Supplier mainSupplier) {
        Article a = new Article();
        a.setId(id);
        a.setName("Testartikel-" + id);
        a.setArticleNumber("1234567" + id);
        a.setPurchasePrice(10.0);
        a.setAvailable(true);
        a.setMainSupplier(mainSupplier);
        return a;
    }

    private Order buildSavedOrder(Long id, String orderNumber, Supplier supplier) {
        Order o = new Order();
        o.setId(id);
        o.setOrderNumber(orderNumber);
        o.setSupplier(supplier);
        o.setStatus(OrderStatus.PLACED);
        o.setTotalAmount(0.0);
        return o;
    }

    // ======================================
    // createAndSendOrdersFromCart
    // ======================================

    @Test
    void createAndSendOrdersFromCart_happyPath_singleSupplier_createsOrderAndPublishesEvent() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Article article = buildAvailableArticle(1L, supplier);
        Order savedOrder = buildSavedOrder(100L, "BE-1", supplier);

        when(cartSession.getItems()).thenReturn(Map.of(1L, 2));
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(orderItemRepository.findAllByOrderId(100L)).thenReturn(List.of());

        List<OrderResponseDTO> result = service.createAndSendOrdersFromCart();

        assertThat(result).hasSize(1);
        verify(contingentRepository).save(any(Contingent.class));
        verify(einkaufEventPublisher).publishNewQuota(1L, 2L);
        verify(cartSession).clearCart();
    }

    @Test
    void createAndSendOrdersFromCart_emptyCart_throwsIllegalStateException() {
        when(cartSession.getItems()).thenReturn(Map.of());

        assertThatThrownBy(() -> service.createAndSendOrdersFromCart())
                .isInstanceOf(IllegalStateException.class);

        verify(cartSession, never()).clearCart();
    }

    @Test
    void createAndSendOrdersFromCart_articleUnavailable_throwsIllegalStateException() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Article article = buildAvailableArticle(1L, supplier);
        article.setAvailable(false);

        when(cartSession.getItems()).thenReturn(Map.of(1L, 1));
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));

        assertThatThrownBy(() -> service.createAndSendOrdersFromCart())
                .isInstanceOf(IllegalStateException.class);

        verify(cartSession, never()).clearCart();
    }

    @Test
    void createAndSendOrdersFromCart_articleHasNoMainSupplier_throwsIllegalStateException() {
        Article article = buildAvailableArticle(1L, null); // no main supplier

        when(cartSession.getItems()).thenReturn(Map.of(1L, 1));
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));

        assertThatThrownBy(() -> service.createAndSendOrdersFromCart())
                .isInstanceOf(IllegalStateException.class);

        verify(cartSession, never()).clearCart();
    }

    @Test
    void createAndSendOrdersFromCart_cartClearedAfterOrderPlaced() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Article article = buildAvailableArticle(1L, supplier);
        Order savedOrder = buildSavedOrder(100L, "BE-1", supplier);

        when(cartSession.getItems()).thenReturn(Map.of(1L, 1));
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(orderItemRepository.findAllByOrderId(100L)).thenReturn(List.of());

        service.createAndSendOrdersFromCart();

        InOrder inOrder = inOrder(orderRepository, cartSession);
        inOrder.verify(orderRepository, atLeastOnce()).save(any(Order.class));
        inOrder.verify(cartSession).clearCart();
    }

    // ======================================
    // getOrderHistory
    // ======================================

    @Test
    @SuppressWarnings("unchecked")
    void getOrderHistory_returnsFilteredList() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Order order = buildSavedOrder(1L, "BE-1", supplier);

        when(orderRepository.findAll(any(Specification.class))).thenReturn(List.of(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of());

        List<OrderResponseDTO> result = service.getOrderHistory(new OrderFilterDTO());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).orderNumber()).isEqualTo("BE-1");
    }

    // ======================================
    // getOrderDetails
    // ======================================

    @Test
    void getOrderDetails_found_returnsDTO() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Order order = buildSavedOrder(1L, "BE-1", supplier);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of());

        OrderResponseDTO result = service.getOrderDetails(1L);

        assertThat(result.orderNumber()).isEqualTo("BE-1");
    }

    @Test
    void getOrderDetails_notFound_throwsEntityNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrderDetails(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ======================================
    // reorder
    // ======================================

    @Test
    void reorder_happyPath_createsNewOrder() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Order originalOrder = buildSavedOrder(10L, "BE-1", supplier);
        Article article = buildAvailableArticle(1L, supplier);
        Order newOrder = buildSavedOrder(11L, "BE-2", supplier);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(originalOrder));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);
        when(orderItemRepository.findAllByOrderId(11L)).thenReturn(List.of());

        List<OrderItemRequestDTO> items = List.of(new OrderItemRequestDTO(1L, 3));
        OrderResponseDTO result = service.reorder(10L, items);

        assertThat(result.orderNumber()).isEqualTo("BE-2");
        verify(einkaufEventPublisher).publishNewQuota(1L, 3L);
    }

    @Test
    void reorder_originalOrderNotFound_throwsEntityNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reorder(99L, List.of(new OrderItemRequestDTO(1L, 1))))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void reorder_supplierInactive_throwsIllegalStateException() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        supplier.setActive(false);
        Order originalOrder = buildSavedOrder(10L, "BE-1", supplier);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(originalOrder));

        assertThatThrownBy(() -> service.reorder(10L, List.of(new OrderItemRequestDTO(1L, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reorder_allItemsUnavailable_throwsIllegalStateException() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Order originalOrder = buildSavedOrder(10L, "BE-1", supplier);
        Article unavailableArticle = buildAvailableArticle(1L, supplier);
        unavailableArticle.setAvailable(false);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(originalOrder));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(unavailableArticle));

        assertThatThrownBy(() -> service.reorder(10L, List.of(new OrderItemRequestDTO(1L, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Keine gültigen");
    }

    @Test
    void reorder_someItemsUnavailable_placesOrderForAvailableItemsOnly() {
        Supplier supplier = buildActiveSupplier(1L, "BrewCo");
        Order originalOrder = buildSavedOrder(10L, "BE-1", supplier);
        Article available = buildAvailableArticle(1L, supplier);
        Article unavailable = buildAvailableArticle(2L, supplier);
        unavailable.setAvailable(false);
        Order newOrder = buildSavedOrder(11L, "BE-2", supplier);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(originalOrder));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(available));
        when(articleRepository.findById(2L)).thenReturn(Optional.of(unavailable));
        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);
        when(orderItemRepository.findAllByOrderId(11L)).thenReturn(List.of());

        List<OrderItemRequestDTO> items = List.of(
                new OrderItemRequestDTO(1L, 2),
                new OrderItemRequestDTO(2L, 3)
        );

        OrderResponseDTO result = service.reorder(10L, items);

        assertThat(result).isNotNull();
        // Only article 1 is available → publishNewQuota called once for article 1
        verify(einkaufEventPublisher, times(1)).publishNewQuota(anyLong(), anyLong());
        verify(einkaufEventPublisher).publishNewQuota(1L, 2L);
    }
}
