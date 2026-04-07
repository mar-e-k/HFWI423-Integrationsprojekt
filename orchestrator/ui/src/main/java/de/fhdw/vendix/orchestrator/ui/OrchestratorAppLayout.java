package de.fhdw.vendix.orchestrator.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.spring.vaadin.view.AbstractApplicationLayout;
import de.fhdw.vendix.orchestrator.ui.account.PermissionView;
import de.fhdw.vendix.orchestrator.ui.account.RoleView;
import de.fhdw.vendix.orchestrator.ui.account.UserView;
import de.fhdw.vendix.orchestrator.ui.orchestrator.ConnectionView;
import de.fhdw.vendix.orchestrator.ui.application.DebugView;
import de.fhdw.vendix.orchestrator.ui.application.RootView;
import de.fhdw.vendix.orchestrator.ui.orchestrator.LockView;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({Role.ROLE_ADMIN})
@Layout
@VaadinSessionScope
public class OrchestratorAppLayout extends AbstractApplicationLayout {

    public OrchestratorAppLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
    }

    @Override
    protected Component[] draweritems() {
        SideNav applicationHeader = new SideNav("Application");
        applicationHeader.addItem(
                new SideNavItem("Home", RootView.class, VaadinIcon.HOME.create()),
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create())
        );

        SideNav orchestratorHeader = new SideNav("Orchestrator");
        orchestratorHeader.addItem(
                new SideNavItem("Connections", ConnectionView.class, VaadinIcon.CONNECT.create()),
                new SideNavItem("Locks", LockView.class, VaadinIcon.LOCK.create())
        );

        SideNav managementHeader = new SideNav("Management");
        managementHeader.addItem(
                new SideNavItem("Users", UserView.class, VaadinIcon.USERS.create()),
                new SideNavItem("Roles", RoleView.class, VaadinIcon.USER_CARD.create()),
                new SideNavItem("Permissions", PermissionView.class, VaadinIcon.KEY.create())
        );

        SideNav externalHeader = new SideNav("External");
        externalHeader.addItem(
                new SideNavItem("Grafana", "http://localhost:3030", VaadinIcon.CHART.create()),
                new SideNavItem("Neon", "https://console.neon.tech/app/projects/bitter-feather-66001186", VaadinIcon.DATABASE.create()),
                new SideNavItem("CloudAMQP", "https://api.cloudamqp.com/console/649e89f9-9795-4012-9e9b-fbf61637e747", VaadinIcon.CLOUD.create()),
                new SideNavItem("Swagger", "http://localhost:8080/swagger", VaadinIcon.CODE.create())
        );

        return new Component[]{
                applicationHeader,
                orchestratorHeader,
                managementHeader,
                externalHeader
        };
    }

    @Override
    protected String applicationTitle() {
        return "Orchestrator";
    }
}