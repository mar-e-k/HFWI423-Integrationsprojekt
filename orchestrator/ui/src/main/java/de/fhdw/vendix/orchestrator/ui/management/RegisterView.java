package de.fhdw.vendix.orchestrator.ui.management;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRole;
import de.fhdw.vendix.orchestrator.core.domain.register.Register;import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = OrchestratorAppLayout.class)
@RolesAllowed(KeycloakRole.Constants.ADMIN)
public class RegisterView extends VerticalLayout {

    public RegisterView() {
        add(initGrid());
    }

    private Grid<Register> initGrid() {
        Grid<Register> registerGrid = new Grid<>(Register.class, false);
        registerGrid.setHeightFull();
        registerGrid.setWidthFull();
        registerGrid.addColumn(Register::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        registerGrid.addColumn(Register::getId)
                .setHeader("Store")
                .setAutoWidth(true)
                .setSortable(true);
        return registerGrid;
    }
}