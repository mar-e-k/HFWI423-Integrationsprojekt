package com.example.application.data.restockorder;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestockOrderRepository extends JpaRepository<RestockOrder, Long> {

    boolean existsByArticleNumberAndDeliveredFalse(String articleNumber);


    // für den Wareneingang: alle genehmigten, aber noch nicht gelieferten Bestellungen
    List<RestockOrder> findByDeliveredFalseAndApprovedTrue();

}
