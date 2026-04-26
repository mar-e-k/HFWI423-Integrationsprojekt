package com.example.application.data.orderPicking;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageLogisticRepository extends JpaRepository<MessageLogistic, Long> {

    Optional<MessageLogistic> findByStoreIdAndArticleNumber(String storeId, String articleNumber);

    @Query("select distinct m.storeId from MessageLogistic m")
    List<String> findAllStoreIds();

    List<MessageLogistic> findByStoreIdAndQuantityGreaterThanAndProcessedFalse(String storeId, long quantity);

    List<MessageLogistic> findByStoreIdAndQuantityGreaterThan(String storeId, long quantity);

    List<MessageLogistic> findByStoreId(String storeId);

    List<MessageLogistic> findByKommissionId(Long kommissionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MessageLogistic m where m.kommission.id = :kommissionId")
    List<MessageLogistic> findByKommissionIdForUpdate(@Param("kommissionId") Long kommissionId);

 	int deleteByArticleId(Long articleId);
 	
    @Query("select distinct m.storeId from MessageLogistic m where m.quantity > 0 and m.processed = false")
    List<String> findDistinctStoresWithUnprocessed();


    @Transactional
    @Modifying
    @Query("update MessageLogistic m set m.processed = true where m.storeId = :storeId and m.processed = false")
    void markStoreMessagesProcessed(@Param("storeId") String storeId);


    @Transactional
    @Modifying
    @Query("DELETE FROM MessageLogistic m WHERE m.storeId = :storeId AND m.processed = true")
    void deleteProcessedByStore(String storeId);

    @Transactional
    @Modifying
    @Query("DELETE FROM MessageLogistic m WHERE m.storeId = :storeId AND m.articleId = :articleId AND m.processed = false")
    void deleteUnprocessedByStoreAndArticleId(@Param("storeId") String storeId, @Param("articleId") Long articleId);

    @Transactional
    @Modifying
    @Query("DELETE FROM MessageLogistic m WHERE m.storeId = :storeId AND m.processed = false")
    void deleteAllUnprocessedByStore(@Param("storeId") String storeId);

 

}


