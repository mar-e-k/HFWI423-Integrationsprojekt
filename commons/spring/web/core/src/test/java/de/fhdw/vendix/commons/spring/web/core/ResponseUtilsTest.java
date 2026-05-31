package de.fhdw.vendix.commons.spring.web.core;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NullAway")
class ResponseUtilsTest {

    @Test
    void extractBodyReturnsBodyOnlyForSuccessfulResponses() {
        assertThat(ResponseUtils.extractBody(ResponseEntity.ok("body"))).contains("body");
        assertThat(ResponseUtils.extractBody(ResponseEntity.notFound().build())).isEmpty();
        assertThat(ResponseUtils.extractBody(null)).isEmpty();
    }

    @Test
    void collectionExtractorsReturnEmptyCollectionsForMissingBodies() {
        assertThat(ResponseUtils.extractList(ResponseEntity.ok(List.of("a", "b")))).containsExactly("a", "b");
        assertThat(ResponseUtils.extractList(ResponseEntity.noContent().build())).isEmpty();
        assertThat(ResponseUtils.extractSet(ResponseEntity.ok(Set.of("a")))).containsExactly("a");
        assertThat(ResponseUtils.extractMap(ResponseEntity.ok(Map.of("a", 1)))).containsEntry("a", 1);
    }
}
