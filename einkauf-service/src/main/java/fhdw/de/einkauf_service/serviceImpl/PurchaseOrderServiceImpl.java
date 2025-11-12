package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Order;
import fhdw.de.einkauf_service.entity.OrderItem;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.OrderItemRepository;
import fhdw.de.einkauf_service.repository.OrderRepository;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
        }

        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        return new OrderResponseDTO(savedOrder.getOrderNumber(), expectedDeliveryDate);
    }
}