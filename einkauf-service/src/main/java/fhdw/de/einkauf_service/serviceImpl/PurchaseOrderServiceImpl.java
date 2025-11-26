package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderItemResponseDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.entity.*;
import fhdw.de.einkauf_service.query.OrderSpecifications;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.ContingentRepository;
import fhdw.de.einkauf_service.repository.OrderItemRepository;
import fhdw.de.einkauf_service.repository.OrderRepository;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ArticleRepository articleRepository;
    private final EntityManager entityManager;
    private final ShoppingCartSession cartSession;
    private final ContingentRepository contingentRepository;

    private String getNextOrderNumber() {
        Long nextValue = (Long) entityManager.createNativeQuery(
                "SELECT nextval('order_number_seq')"
        ).getSingleResult();
        return "BE-" + nextValue;
    }

    /**
     * Erstellt eine oder mehrere Bestellungen basierend auf dem aktuellen Warenkorb-Inhalt,
     * gruppiert nach dem Lieferanten, der im Artikel hinterlegt ist.
     * @return Liste der OrderResponseDTOs für jede ausgelöste Bestellung.
     */
    @Transactional
    @Override
    public List<OrderResponseDTO> createAndSendOrdersFromCart() {

        Map<Long, Integer> currentCartItems = cartSession.getItems();

        if (currentCartItems.isEmpty()) {
            throw new IllegalStateException("Der Warenkorb ist leer und kann nicht bestellt werden.");
        }

        // 1. Artikelinformationen abrufen
        Map<Long, Article> articles = articleRepository.findAllById(currentCartItems.keySet())
                .stream()
                .collect(Collectors.toMap(Article::getId, Function.identity()));

        // 2. Gruppierung der Bestellpositionen nach Lieferant
        Map<Supplier, List<OrderItemRequestDTO>> groupedOrders = currentCartItems.entrySet().stream()
                .map(entry -> {
                    Article article = articles.get(entry.getKey());
                    if (article == null || article.getSupplier() == null) {
                        throw new EntityNotFoundException("Artikel oder Lieferant für ID " + entry.getKey() + " nicht gefunden.");
                    }
                    return Map.entry(
                            article.getSupplier(), // Key ist das Supplier-Objekt
                            new OrderItemRequestDTO(entry.getKey(), entry.getValue())
                    );
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // 3. Für jede Gruppe eine separate Bestellung erstellen
        List<OrderResponseDTO> responses = groupedOrders.entrySet().stream()
                .map(entry -> placeSingleOrder(entry.getKey(), entry.getValue()))
                .toList();

        // 4. Warenkorb leeren
        cartSession.clearCart();

        return responses;
    }

    /**
     * Führt die Logik zum Speichern einer einzelnen Bestellung durch.
     */
    private OrderResponseDTO placeSingleOrder(Supplier supplier, List<OrderItemRequestDTO> items) {

        Order order = new Order();
        order.setSupplier(supplier);
        order.setOrderNumber(getNextOrderNumber());

        LocalDate expectedDeliveryDate = LocalDate.now().plusDays(7);
        order.setExpectedDeliveryDate(expectedDeliveryDate);

        Order savedOrder = orderRepository.save(order);
        Double totalAmount = 0.00;

        for (OrderItemRequestDTO itemDto : items) {
            Article article = articleRepository.findById(itemDto.articleId()).orElseThrow();

            Double itemTotal = article.getPurchasePrice() * itemDto.quantity();

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setArticle(article);
            item.setQuantity(itemDto.quantity());
            item.setPurchasePrice(article.getPurchasePrice());

            orderItemRepository.save(item);
            totalAmount = totalAmount + itemTotal;

            Contingent contingent = new Contingent();
            contingent.setOrderId(savedOrder.getId());
            contingent.setSupplierId(supplier.getId());
            contingent.setArticleId(itemDto.articleId());
            contingent.setAvailableQuantity(itemDto.quantity());

            contingentRepository.save(contingent);
        }

        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);
        return mapOrderToResponseDTO(savedOrder);
    }


    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDTO> getOrderHistory(OrderFilterDTO filter) {

        // 1. Spezifikation erstellen
        Specification<Order> specification = OrderSpecifications.filterOrders(filter);

        // 2. Repository mit Spezifikation aufrufen
        return orderRepository.findAll(specification).stream()
                .map(this::mapOrderToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponseDTO getOrderDetails(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bestellung mit ID " + id + " nicht gefunden."));
        return mapOrderToResponseDTO(order);
    }

    @Transactional
    @Override
    public OrderResponseDTO reorder(Long originalOrderId, List<OrderItemRequestDTO> itemsToReorder) {

        // 1. Ursprüngliche Bestellung und Lieferant finden
        Order originalOrder = orderRepository.findById(originalOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Ursprüngliche Bestellung " + originalOrderId + " nicht gefunden."));

        Supplier supplier = originalOrder.getSupplier();

        // --- PRÜFUNG DER VERFÜGBARKEIT
        if (!supplier.getIsActive()) {
            throw new IllegalStateException("Lieferant " + supplier.getName() + " ist nicht mehr aktiv.");
        }

        // 2. Artikelprüfungen und Mapping zur Vorbereitung der Bestellung
        List<OrderItemRequestDTO> validItems = itemsToReorder.stream()
                .filter(itemDto -> {
                    Article article = articleRepository.findById(itemDto.articleId())
                            .orElseThrow(() -> new EntityNotFoundException("Artikel " + itemDto.articleId() + " nicht gefunden."));

                    // Prüfen, ob Artikel noch verfügbar ist (angenommen, das Feld existiert in Article)
                    if (Boolean.FALSE.equals(article.getIsAvailable())) {
                        System.out.println("WARNUNG: Artikel " + article.getName() + " ist nicht mehr verfügbar und wird übersprungen.");
                        return false;
                    }
                    return true;
                })
                .toList();

        if (validItems.isEmpty()) {
            throw new IllegalStateException("Keine gültigen Positionen zum Wiederbestellen vorhanden.");
        }

        // 3. Auslösen der neuen Bestellung
        return placeSingleOrder(supplier, validItems);
    }




    /**
     *
     * Mapper
     *
     */

    private OrderResponseDTO mapOrderToResponseDTO(Order order) {

        List<OrderItem> items = orderItemRepository.findAllByOrderId(order.getId());

        List<OrderItemResponseDTO> itemDtos = items.stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getArticle().getId(),
                        item.getArticle().getName(),
                        item.getQuantity(),
                        item.getPurchasePrice()
                )).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getSupplier().getName(),
                order.getSupplier().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getExpectedDeliveryDate(),
                itemDtos
        );
    }
}