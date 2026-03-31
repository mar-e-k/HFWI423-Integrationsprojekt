package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.amqp.EinkaufEventPublisher;
import fhdw.de.einkauf_service.dto.ContingentResponseDTO;
import fhdw.de.einkauf_service.entity.*;
import fhdw.de.einkauf_service.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContingentServiceImplTest {

    @Mock
    private ContingentRepository contingentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private EinkaufEventPublisher eventPublisher;

    @InjectMocks
    private ContingentServiceImpl service;

    private Contingent buildContingent(Long id, Long orderId, Long supplierId, Long articleId, int quantity) {
        return new Contingent(id, orderId, supplierId, articleId, quantity);
    }

    private Article buildArticle(Long id, String name) {
        Article a = new Article();
        a.setId(id);
        a.setName(name);
        a.setArticleNumber("12345678");
        return a;
    }

    private Supplier buildSupplier(Long id, String name) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private OrderItem buildOrderItem(Long orderId, Long articleId, int quantity) {
        Order order = new Order();
        order.setId(orderId);

        Article article = buildArticle(articleId, "Testartikel");

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setArticle(article);
        item.setQuantity(quantity);
        item.setPurchasePrice(5.0);
        return item;
    }

    // --- deleteContingent ---

    @Test
    void deleteContingent_found_deletesAndPublishesEvent() {
        Contingent contingent = buildContingent(1L, 10L, 2L, 5L, 3);
        when(contingentRepository.findById(1L)).thenReturn(Optional.of(contingent));

        service.deleteContingent(1L);

        verify(contingentRepository).delete(contingent);
        verify(eventPublisher).publishDeleteQuota(5L);
    }

    @Test
    void deleteContingent_notFound_throwsEntityNotFoundException() {
        when(contingentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteContingent(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(eventPublisher, never()).publishDeleteQuota(anyLong());
    }

    // --- getAllAvailableContingents ---

    @Test
    void getAllAvailableContingents_emptyRepository_returnsEmptyList() {
        when(contingentRepository.findAll()).thenReturn(List.of());

        List<ContingentResponseDTO> result = service.getAllAvailableContingents();

        assertThat(result).isEmpty();
        verify(articleRepository, never()).findAllById(any());
        verify(supplierRepository, never()).findAllById(any());
    }

    @Test
    void getAllAvailableContingents_oneContingent_enrichedWithArticleAndSupplierName() {
        Contingent contingent = buildContingent(1L, 3L, 2L, 1L, 5);
        Article article = buildArticle(1L, "Bier");
        Supplier supplier = buildSupplier(2L, "BrewCo");
        OrderItem orderItem = buildOrderItem(3L, 1L, 10);

        when(contingentRepository.findAll()).thenReturn(List.of(contingent));
        when(articleRepository.findAllById(List.of(1L))).thenReturn(List.of(article));
        when(supplierRepository.findAllById(List.of(2L))).thenReturn(List.of(supplier));
        when(orderItemRepository.findAllByOrderIdIn(List.of(3L))).thenReturn(List.of(orderItem));

        List<ContingentResponseDTO> result = service.getAllAvailableContingents();

        assertThat(result).hasSize(1);
        ContingentResponseDTO dto = result.get(0);
        assertThat(dto.getArticleName()).isEqualTo("Bier");
        assertThat(dto.getSupplierName()).isEqualTo("BrewCo");
        assertThat(dto.getOriginalOrderQuantity()).isEqualTo(10);
    }

    @Test
    void getAllAvailableContingents_articleNotInMap_articleNameIsNA() {
        Contingent contingent = buildContingent(1L, 3L, 2L, 1L, 5);
        Supplier supplier = buildSupplier(2L, "BrewCo");

        when(contingentRepository.findAll()).thenReturn(List.of(contingent));
        when(articleRepository.findAllById(any())).thenReturn(List.of()); // empty — article missing
        when(supplierRepository.findAllById(any())).thenReturn(List.of(supplier));
        when(orderItemRepository.findAllByOrderIdIn(any())).thenReturn(List.of());

        List<ContingentResponseDTO> result = service.getAllAvailableContingents();

        assertThat(result.get(0).getArticleName()).isEqualTo("N/A");
    }

    @Test
    void getAllAvailableContingents_supplierNotInMap_supplierNameIsNA() {
        Contingent contingent = buildContingent(1L, 3L, 2L, 1L, 5);
        Article article = buildArticle(1L, "Bier");

        when(contingentRepository.findAll()).thenReturn(List.of(contingent));
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));
        when(supplierRepository.findAllById(any())).thenReturn(List.of()); // empty — supplier missing
        when(orderItemRepository.findAllByOrderIdIn(any())).thenReturn(List.of());

        List<ContingentResponseDTO> result = service.getAllAvailableContingents();

        assertThat(result.get(0).getSupplierName()).isEqualTo("N/A");
    }

    @Test
    void getAllAvailableContingents_orderItemNotFound_fallsBackToAvailableQuantity() {
        Contingent contingent = buildContingent(1L, 3L, 2L, 1L, 7);
        Article article = buildArticle(1L, "Bier");
        Supplier supplier = buildSupplier(2L, "BrewCo");

        when(contingentRepository.findAll()).thenReturn(List.of(contingent));
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));
        when(supplierRepository.findAllById(any())).thenReturn(List.of(supplier));
        when(orderItemRepository.findAllByOrderIdIn(any())).thenReturn(List.of()); // no matching item

        List<ContingentResponseDTO> result = service.getAllAvailableContingents();

        // Falls back to availableQuantity
        assertThat(result.get(0).getOriginalOrderQuantity()).isEqualTo(7);
    }
}
