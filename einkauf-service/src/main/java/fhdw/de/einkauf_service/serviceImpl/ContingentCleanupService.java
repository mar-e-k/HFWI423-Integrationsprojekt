package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.entity.Contingent;
import fhdw.de.einkauf_service.enums.OrderStatus;
import fhdw.de.einkauf_service.repository.ContingentRepository;
import fhdw.de.einkauf_service.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContingentCleanupService {
//
//    private final ContingentRepository contingentRepository;
//    private final OrderRepository orderRepository;
//
//    public ContingentCleanupService(ContingentRepository contingentRepository, OrderRepository orderRepository) {
//        this.contingentRepository = contingentRepository;
//        this.orderRepository = orderRepository;
//    }
//
//    /**
//     * Prüft alle 2 Minuten alle Kontingente.
//     */
//    @Scheduled(fixedRate = 120000)
//    @Transactional
//    public void cleanupContingents() {
//
//        // Finde alle Kontingente, deren verfügbare Menge 0 erreicht hat
//        List<Contingent> zeroContingents = contingentRepository.findAllByAvailableQuantity(0);
//
//        for (Contingent contingent : zeroContingents) {
//
//            // 1. Ursprüngliche Bestellung auf DELIVERED setzen
//            orderRepository.findById(contingent.getOrderId()).ifPresent(order -> {
//                order.setStatus(OrderStatus.DELIVERED);
//                orderRepository.save(order);
//                System.out.println("Order " + order.getOrderNumber() + " set to DELIVERED.");
//            });
//
//             2. Kontingent-Zeile löschen
//            contingentRepository.delete(contingent);
//            System.out.println("Contingent " + contingent.getId() + " deleted.");
//        }
//    }
}
