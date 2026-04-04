package de.fhdw.vendix.store.core.embeddable.preference_amount;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;

@Embeddable
@SuppressWarnings("NullAway")
public class PreferenceAmount {

    @Min(value = 0)
    @Column(nullable = false, name = "pref_amount_min")
    private Long min;

    @Min(value = 0)
    @Column(nullable = false, name = "pref_amount_avg")
    private Long avg;

    @Min(value = 0)
    @Column(nullable = false, name = "pref_amount_max")
    private Long max;

    public PreferenceAmount() {}

    @Default
    public PreferenceAmount(Long min, Long avg, Long max) {
        this.min = min;
        this.avg = avg;
        this.max = max;
    }

    public Long getMin() {
        return min;
    }

    public Long getAvg() {
        return avg;
    }

    public Long getMax() {
        return max;
    }
}