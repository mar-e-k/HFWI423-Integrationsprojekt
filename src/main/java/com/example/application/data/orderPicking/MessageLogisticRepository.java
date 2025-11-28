package com.example.application.data.orderPicking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageLogisticRepository extends JpaRepository<MessageLogistic, Integer> {

    /**
     * Liefert alle Artikel für einen Store, deren Lagerbestand unter dem Zielbestand liegt.
     *
     * @param storeId die ID des Stores
     * @return Liste unterbestandsgeführter MessageLogistic-Einträge
     */
    @Query("""
        SELECT m FROM MessageLogistic m
        WHERE m.storeId = :storeId
          AND m.stockLevel < m.targetStockLevel
    """)
    List<MessageLogistic> findUnderstocked(@Param("storeId") String storeId);

}
