package de.fhdw.vendix.orchestrator.ui.orchestrator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.distributed_lock.DistributedLock;
import de.fhdw.vendix.orchestrator.core.domain.distributed_lock.DistributedLockService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "lock", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class LockView extends VerticalLayout {

    private final DistributedLockService distributedLockService;

    public LockView(DistributedLockService distributedLockService) {
        this.distributedLockService = distributedLockService;
        add(initGrid());
    }

    private Grid<DistributedLock> initGrid() {
        Grid<DistributedLock> lockGrid = new Grid<>(DistributedLock.class, false);
        lockGrid.setHeightFull();
        lockGrid.setWidthFull();
        lockGrid.addColumn(DistributedLock::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(l -> l.getTarget().getId())
                .setHeader("Target Id")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(l -> l.getTarget().getType())
                .setHeader("Target Type")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(DistributedLock::getInstanceUUID)
                .setHeader("Target UUID")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(DistributedLock::getAcquiredAt)
                .setHeader("Acquired At")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(DistributedLock::getExpiresAt)
                .setHeader("Expires At")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.setItems(distributedLockService.findAll());
        return lockGrid;
    }
}