package de.fhdw.vendix.orchestrator.web.controller;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.app.context.app.AppContext;
import de.fhdw.vendix.commons.spring.web.server.prometheus.api.PrometheusApi;
import de.fhdw.vendix.commons.spring.web.server.prometheus.model.TargetGroup;
import de.fhdw.vendix.orchestrator.core.domain.connection.Connection;
import de.fhdw.vendix.orchestrator.core.domain.connection.ConnectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        List<TargetGroup> targetGroups = new ArrayList<>();
        TargetGroup orchestratorTarget = new TargetGroup();
        orchestratorTarget.setTargets(List.of("host:8080"));
        orchestratorTarget.setLabels(Map.of(
                "__app__", "vendix",
                "__service_name__", TargetType.ORCHESTRATOR.name().toLowerCase(),
                "__instance__", appContext.getInstanceUUID().toString(),
                "__target_type__", TargetType.ORCHESTRATOR.name().toLowerCase(),
                "__target_id__", "N/A"
        ));

        targetGroups.add(orchestratorTarget);

        List<Connection> connections = connectionService.findAll();
        List<TargetGroup> storeAndRegisterGroups = connections.stream()
                .filter(c ->
                        c.getTarget().getType() == TargetType.STORE || c.getTarget().getType() == TargetType.REGISTER
                )
                .map(this::createPrometheusTarget)
                .toList();

        targetGroups.addAll(storeAndRegisterGroups);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(targetGroups);
    }

    private TargetGroup createPrometheusTarget(Connection connection) {
        TargetGroup targetGroup = new TargetGroup();
        targetGroup.setTargets(List.of("host:" + connection.getInstance().getPort()));
        targetGroup.setLabels(Map.of(
                "__app__", "vendix",
                "__service_name__", connection.getTarget().getType().name().toLowerCase(),
                "__instance__", connection.getInstance().getUuid().toString(),
                "__target_type__", connection.getTarget().getType().name().toLowerCase(),
                "__target_id__", connection.getTarget().getId().toString()
        ));
        return targetGroup;
    }
}