package de.fhdw.commons.rest;

import reactor.core.publisher.Mono;

public interface GenericClient {
    Mono<Void> checkStatus();
}