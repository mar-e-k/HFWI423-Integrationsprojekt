package de.fhdw.vendix.store.core.domain.store_stock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
class StoreStockBulkRepositoryImpl implements StoreStockBulkRepository {

    private static final String DECREMENT_SQL = """
            UPDATE store_stock
            SET current_amount = GREATEST(0, current_amount - ?),
                version = version + 1
            WHERE store_id = ?
              AND article_id = ?
            """;

    private static final String INCREMENT_SQL = """
            UPDATE store_stock
            SET current_amount = current_amount + ?,
                version = version + 1
            WHERE store_id = ?
              AND article_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    StoreStockBulkRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void decrementArticles(Long storeId, Map<Long, Long> amountByArticleId) {
        if (amountByArticleId == null || amountByArticleId.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = new ArrayList<>(amountByArticleId.size());
        amountByArticleId.forEach((articleId, amount) ->
                batchArgs.add(new Object[] { amount, storeId, articleId })
        );
        jdbcTemplate.batchUpdate(DECREMENT_SQL, batchArgs);
    }

    @Override
    public void incrementArticles(Long storeId, Map<Long, Long> amountByArticleId) {
        if (amountByArticleId == null || amountByArticleId.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = new ArrayList<>(amountByArticleId.size());
        amountByArticleId.forEach((articleId, amount) ->
                batchArgs.add(new Object[] { amount, storeId, articleId })
        );
        jdbcTemplate.batchUpdate(INCREMENT_SQL, batchArgs);
    }
}
