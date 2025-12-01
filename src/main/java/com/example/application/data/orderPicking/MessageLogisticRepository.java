package com.example.application.data.orderPicking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MessageLogisticRepository extends JpaRepository<MessageLogistic, Long> {

    Optional<MessageLogistic> findByStoreIdAndArticleNumber(String storeId, String articleNumber);

    @Query("select distinct m.storeId from MessageLogistic m")
    List<String> findAllStoreIds();

    List<MessageLogistic> findByStoreIdAndQuantityGreaterThan(String storeId, long min);

}
