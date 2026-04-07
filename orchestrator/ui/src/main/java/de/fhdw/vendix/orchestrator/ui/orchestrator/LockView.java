package de.fhdw.vendix.orchestrator.ui.orchestrator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.lock.Lock;
import de.fhdw.vendix.orchestrator.core.domain.lock.LockService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "lock", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class LockView extends VerticalLayout {

    private final LockService lockService;

    public LockView(LockService lockService) {
        this.lockService = lockService;
        add(initGrid());
    }

    private Grid<Lock> initGrid() {
        Grid<Lock> lockGrid = new Grid<>(Lock.class, false);
        lockGrid.setHeightFull();
        lockGrid.setWidthFull();
        lockGrid.addColumn(Lock::getId)
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
        lockGrid.addColumn(Lock::getInstanceUUID)
                .setHeader("Target UUID")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(Lock::getAcquiredAt)
                .setHeader("Acquired At")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.addColumn(Lock::getExpiresAt)
                .setHeader("Expires At")
                .setAutoWidth(true)
                .setSortable(true);
        lockGrid.setItems(lockService.findAll());
        return lockGrid;
    }
}