package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ContingentResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Contingent;
import fhdw.de.einkauf_service.entity.OrderItem;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.ContingentRepository;
import fhdw.de.einkauf_service.repository.OrderItemRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.service.ContingentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContingentServiceImpl implements ContingentService {

    private final ContingentRepository contingentRepository;
    private final ArticleRepository articleRepository;
    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;

    // --- Mapper-Methode ---
    private ContingentResponseDTO mapToResponseDTO(Contingent contingent, Article article, Supplier supplier, Integer originalQuantity) {
        ContingentResponseDTO dto = new ContingentResponseDTO();
        dto.setId(contingent.getId());
        dto.setOrderId(contingent.getOrderId());
        dto.setSupplierId(contingent.getSupplierId());
        dto.setSupplierName(supplier != null ? supplier.getName() : "N/A");
        dto.setArticleId(contingent.getArticleId());
        dto.setArticleName(article != null ? article.getName() : "N/A");
        dto.setAvailableQuantity(contingent.getAvailableQuantity());
        dto.setOriginalOrderQuantity(originalQuantity);
        return dto;
    }

    /**
     * Ruft alle aktiven Kontingente ab und reichert sie mit Artikel- und Lieferantennamen an.
     */
    @Transactional(readOnly = true)
    public List<ContingentResponseDTO> getAllAvailableContingents() {
        List<Contingent> contingents = contingentRepository.findAll();

        if (contingents.isEmpty()) {
            return List.of();
        }

        // 1. Alle benötigten Artikel-IDs und Supplier-IDs sammeln
        List<Long> articleIds = contingents.stream().map(Contingent::getArticleId).toList();
        List<Long> supplierIds = contingents.stream().map(Contingent::getSupplierId).toList();


        List<Long> orderIds = contingents.stream().map(Contingent::getOrderId).distinct().toList();

        // Laden aller relevanten OrderItems
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdIn(orderIds);

        // Map erstellen: OrderId + ArticleId -> Quantity
        Map<String, Integer> originalQuantityMap = orderItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getOrder().getId() + "_" + item.getArticle().getId(),
                        OrderItem::getQuantity
                ));

        // 2. Artikel und Lieferanten in Batches laden
        Map<Long, Article> articles = articleRepository.findAllById(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, Function.identity()));
        Map<Long, Supplier> suppliers = supplierRepository.findAllById(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getId, Function.identity()));

        // 3. Mappen unter Verwendung der geladenen Daten
        return contingents.stream()
                .map(c -> {
                    String key = c.getOrderId() + "_" + c.getArticleId();
                    Integer originalQuantity = originalQuantityMap.getOrDefault(key, c.getAvailableQuantity());

                    return mapToResponseDTO(
                            c,
                            articles.get(c.getArticleId()),
                            suppliers.get(c.getSupplierId()),
                            originalQuantity
                    );
                })
                .toList();
    }
}
