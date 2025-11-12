package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderRequestDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Order;
import fhdw.de.einkauf_service.entity.OrderItem;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.OrderItemRepository;
import fhdw.de.einkauf_service.repository.OrderRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ArticleRepository articleRepository;
    private final SupplierRepository supplierRepository;

    private final EntityManager entityManager;

    // Hilfsmethode zur sicheren, sequenziellen Nummerngenerierung
    private String getNextOrderNumber() {
        // Fragt die PostgreSQL-Sequenz direkt ab.
        // Dies ist sicher und atomar in der Datenbank implementiert.
        Long nextValue = (Long) entityManager.createNativeQuery(
                "SELECT nextval('order_number_seq')"
        ).getSingleResult();

        // Fügt das Präfix "BE-" und die gefüllte Nummer hinzu (z.B. BE-10000)
        return "BE-" + nextValue;
    }

    @Override
    @Transactional
    public OrderResponseDTO createAndSendOrder(OrderRequestDTO request) {

        // 1. Validierung und Datenvorbereitung
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new EntityNotFoundException("Lieferant nicht gefunden."));

        Order order = new Order();
        order.setSupplier(supplier);

        // 2. SICHERE GENERIERUNG DER BESTELLNUMMER
        String orderNumber = getNextOrderNumber();
        order.setOrderNumber(orderNumber);

        // 3. Bestimmen des voraussichtlichen Lieferdatums (Akzeptanzkriterium 2)
        LocalDate expectedDeliveryDate = LocalDate.now().plusDays(7);
        order.setExpectedDeliveryDate(expectedDeliveryDate);

        // 4. Speichern des Bestellkopfs
        Order savedOrder = orderRepository.save(order);

        Double totalAmount = 0.00;

        // 5. Speichern der Bestellpositionen (OrderItem)
        for (OrderItemRequestDTO itemDto : request.items()) {
            Article article = articleRepository.findById(itemDto.articleId())
                    .orElseThrow(() -> new EntityNotFoundException("Artikel nicht gefunden: " + itemDto.articleId()));

            OrderItem item = new OrderItem();
            item.setOrder(savedOrder);
            item.setArticle(article);
            item.setQuantity(itemDto.quantity());

            // Preis zum Zeitpunkt der Bestellung speichern
            item.setPurchasePrice(article.getPurchasePrice());

            orderItemRepository.save(item);

            // Berechnung des Gesamtbetrags
            totalAmount += article.getPurchasePrice() * itemDto.quantity();

        }

        // 6. Bestellung finalisieren und Gesamtbetrag setzen
        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        // 7. Bestellbestätigung zurückgeben
        return new OrderResponseDTO(orderNumber, expectedDeliveryDate);
    }
}
