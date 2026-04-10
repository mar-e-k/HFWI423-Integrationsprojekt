package de.fhdw.vendix.store.core.embeddable.preference_amount;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Embeddable
@SuppressWarnings("NullAway")
public class PreferenceAmount {

    @NotNull(message = "Preference amount min cannot be null")
    @Min(value = 1, message = "Preference amount min must be at least 1")
    @Column(nullable = false, name = "pref_amount_min")
    private Long min;

    @NotNull(message = "Preference amount avg cannot be null")
    @Min(value = 1, message = "Preference amount avg must be at least 1")
    @Column(nullable = false, name = "pref_amount_avg")
    private Long avg;

    @NotNull(message = "Preference amount max cannot be null")
    @Min(value = 1, message = "Preference amount max must be at least 1")
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