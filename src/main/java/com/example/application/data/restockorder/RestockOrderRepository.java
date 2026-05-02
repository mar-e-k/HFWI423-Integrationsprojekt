package com.example.application.data.restockorder;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestockOrderRepository extends JpaRepository<RestockOrder, Long> {

    boolean existsByArticleNumberAndDeliveredFalse(String articleNumber);

    @Query("SELECT r.articleNumber FROM RestockOrder r WHERE r.articleNumber IN :articleNumbers AND r.delivered = false")
    List<String> findOpenOrderArticleNumbers(@Param("articleNumbers") Collection<String> articleNumbers);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RestockOrder r where r.id = :id")
    Optional<RestockOrder> findByIdForUpdate(@jakarta.annotation.Nonnull Long id);

    // fur den Wareneingang: alle genehmigten, aber noch nicht gelieferten Bestellungen
    List<RestockOrder> findByDeliveredFalseAndApprovedTrue();

    /**
     * Setzt delivered=true atomar, aber nur wenn es noch false ist.
     * Gibt 1 zurueck wenn die Zeile aktualisiert wurde, 0 wenn ein anderer Thread schneller war.
     * Ersetzt den fehleranfaelligen JPA-Pessimistic-Lock fuer Concurrent-Szenarien.
     */
    @Modifying
    @Query("UPDATE RestockOrder r SET r.delivered = true WHERE r.id = :id AND r.delivered = false")
    int markDeliveredIfOpen(@jakarta.annotation.Nonnull @org.springframework.data.repository.query.Param("id") Long id);

    @Modifying
    @Query("delete from RestockOrder r where r.articleNumber like 'SIM-%'")
    int deleteAllSimOrders();

    /**
     * Loescht offene SIM-Bestellungen, fuer die kein Artikel mehr existiert.
     * Noetig wenn Artikel geloescht und mit gleicher Nummer neu angelegt wurden.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        DELETE FROM restock_order
         WHERE article_number LIKE 'SIM-%'
           AND delivered = false
           AND approved  = true
           AND NOT EXISTS (
               SELECT 1 FROM article_info ai
                WHERE ai.article_number = restock_order.article_number
           )
        """, nativeQuery = true)
    int deleteOrphanedSimOrders();

}
