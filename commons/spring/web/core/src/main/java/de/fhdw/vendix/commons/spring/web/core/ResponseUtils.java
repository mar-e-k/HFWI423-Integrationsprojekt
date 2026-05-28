package de.fhdw.vendix.commons.spring.web.core;

import org.springframework.http.ResponseEntity;

import java.util.*;

public final class ResponseUtils {

    private ResponseUtils() {}

    public static <T> Optional<T> extractBody(ResponseEntity<T> response) {
        if (response == null) {
            return Optional.empty();
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getBody());
    }

    public static <T> List<T> extractList(ResponseEntity<List<T>> response) {
        return extractBody(response).orElseGet(Collections::emptyList);
    }

    public static <T> Set<T> extractSet(ResponseEntity<Set<T>> response) {
        return extractBody(response).orElseGet(Collections::emptySet);
    }

    public static <K, V> Map<K, V> extractMap(ResponseEntity<Map<K, V>> response) {
        return extractBody(response).orElseGet(Map::of);
    }
}