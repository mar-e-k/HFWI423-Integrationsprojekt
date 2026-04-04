package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.spring.web.server.prometheus.api.PrometheusApi;
import de.fhdw.vendix.commons.spring.web.server.prometheus.model.TargetGroup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class PrometheusController implements PrometheusApi {

    @Override
    public ResponseEntity<List<TargetGroup>> getPrometheusTargets() {
        TargetGroup targetGroup = new TargetGroup(List.of("localhost:8080"));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(List.of(targetGroup));
    }
}