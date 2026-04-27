package de.fhdw.vendix.orchestrator.ui.orchestrator;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "connection", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.Constants.ADMIN)
public class ConnectionView extends VerticalLayout {

    public ConnectionView() {}
}