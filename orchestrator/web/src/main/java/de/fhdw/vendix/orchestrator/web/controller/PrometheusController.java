package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.spring.security.context.app.AppContext;
import de.fhdw.vendix.commons.spring.web.server.prometheus.api.PrometheusApi;
import de.fhdw.vendix.commons.spring.web.server.prometheus.model.TargetGroup;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
class PrometheusController implements PrometheusApi {

    private final ConnectionService connectionService;
    private final AppContext appContext;

    PrometheusController(ConnectionService connectionService, AppContext appContext) {
        this.connectionService = connectionService;
        this.appContext = appContext;
    }

    @Override
    public ResponseEntity<List<TargetGroup>> getPrometheusTargets() {
        TargetGroup orchestratorGroup = new TargetGroup();
        orchestratorGroup.setTargets(List.of("host:8080"));
        orchestratorGroup.setLabels(Map.of(
                "__app__", appContext.getApplicationName(),
                "__instance__", appContext.getInstanceUUID().toString(),
                "__machine__", appContext.getHostname(),
                "__environment__", "local"
        ));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(List.of(orchestratorGroup));
    }
}