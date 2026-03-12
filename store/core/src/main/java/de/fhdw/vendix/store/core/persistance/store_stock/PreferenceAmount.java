package de.fhdw.vendix.store.core.persistance.store_stock;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;

@Embeddable
public class PreferenceAmount {

    @Min(value = 0)
    private long minimum;

    @Min(value = 0)
    private long average;

    @Min(value = 0)
    private long max;

    protected PreferenceAmount() {}

    protected PreferenceAmount(long minimum, long average, long max) {
        this.minimum = minimum;
        this.average = average;
        this.max = max;
    }

    public long getMinimum() {
        return minimum;
    }

    public long getAverage() {
        return average;
    }

    public long getMax() {
        return max;
    }
}