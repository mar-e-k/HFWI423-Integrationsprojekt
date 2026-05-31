package de.fhdw.vendix.commons.spring.web.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingHeaderTest {

    @Test
    void exposesStableGatewayHeaderNames() {
        assertThat(RoutingHeader.STORE_ROUTING.getHeader()).isEqualTo("X-Store-ID");
        assertThat(RoutingHeader.REGISTER_ROUTING.getHeader()).isEqualTo("X-Register-ID");
    }
}
