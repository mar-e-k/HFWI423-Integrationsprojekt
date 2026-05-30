package de.fhdw.vendix.commons.spring.web.core.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NullAway")
class RequestRatePropertiesTest {

    @Test
    void appliesSafeDefaults() {
        RequestRateProperties properties = new RequestRateProperties(null, null, null);

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.maxRequests()).isEqualTo(1000L);
        assertThat(properties.resetTimeWindow()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void rejectsNegativeRequestLimit() {
        assertThatThrownBy(() -> new RequestRateProperties(true, -1L, Duration.ofSeconds(10)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
