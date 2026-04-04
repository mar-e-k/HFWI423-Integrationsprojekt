package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.store.core.domain.article.Article;
import de.fhdw.vendix.store.core.domain.store.Store;
import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmount;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "article_id"}))
public class StoreStock extends AbstractSpringDataAuditingEntity<Long> {

    @ManyToOne(optional = false)
    private Store store;

    @ManyToOne(optional = false)
    private Article article;

    @Column(nullable = false)
    @Min(value = 0)
    private Long currentAmount;

    @Embedded
    private PreferenceAmount preferenceAmount;

    protected StoreStock() {}

    protected StoreStock(
            Store store,
            Article article,
            Long currentAmount,
            PreferenceAmount preferenceAmount
    ) {
        this.store = store;
        this.article = article;
        this.currentAmount = currentAmount;
        this.preferenceAmount = preferenceAmount;
    }

    @Default
    protected StoreStock(
            @Nullable Long id,
            Store store,
            Article article,
            Long currentAmount,
            PreferenceAmount preferenceAmount
    ) {
        super(id);
        this.store = store;
        this.article = article;
        this.currentAmount = currentAmount;
        this.preferenceAmount = preferenceAmount;
    }

    public Store getStore() {
        return store;
    }

    public Article getArticle() {
        return article;
    }

    public long getCurrentAmount() {
        return currentAmount;
    }

    public PreferenceAmount getPreferenceAmount() {
        return preferenceAmount;
    }

    public void restockArticle(long amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Parameter 'amount' must be greater than 0.");
        }
        this.currentAmount += amount;
    }
}