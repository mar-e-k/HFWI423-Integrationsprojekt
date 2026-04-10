package de.fhdw.vendix.store.core.domain.store_stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface StoreStockRepository extends JpaRepository<StoreStock, Long> {
    Optional<StoreStock> findByStoreIdAndArticleId(Long storeId, Long articleId);
}