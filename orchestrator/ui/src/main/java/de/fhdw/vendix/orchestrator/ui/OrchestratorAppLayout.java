package de.fhdw.vendix.orchestrator.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.commons.spring.vaadin.layout.AbstractApplicationLayout;
import de.fhdw.vendix.orchestrator.ui.application.DebugView;
import de.fhdw.vendix.orchestrator.ui.application.PerformanceTestView;
import de.fhdw.vendix.orchestrator.ui.application.RootView;
import de.fhdw.vendix.orchestrator.ui.management.*;
import de.fhdw.vendix.orchestrator.ui.orchestrator.ConnectionView;
import de.fhdw.vendix.orchestrator.ui.orchestrator.LockView;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({Role.Constants.ADMIN})
@Layout
@VaadinSessionScope
public class OrchestratorAppLayout extends AbstractApplicationLayout {

    private final VerticalLayout upperDrawer = new VerticalLayout();
    private final VerticalLayout lowerDrawer = new VerticalLayout();

    public OrchestratorAppLayout(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        configureUpperDrawer();
        configureLowerDrawer();
        initLayout();
    }

    private void configureUpperDrawer() {
        SideNav applicationHeader = new SideNav("Application");
        applicationHeader.addItem(
                new SideNavItem("Home",  RootView.class,  VaadinIcon.HOME.create()),
                new SideNavItem("Debug", DebugView.class, VaadinIcon.COGS.create())
        );

        SideNav orchestratorHeader = new SideNav("Orchestrator");
        orchestratorHeader.addItem(
                new SideNavItem("Connections", ConnectionView.class,     VaadinIcon.CONNECT.create()),
                new SideNavItem("Locks",       LockView.class,           VaadinIcon.LOCK.create())
        );

        SideNav managementHeader = new SideNav("Management");
        managementHeader.addItem(
                new SideNavItem("Stores",    StoreView.class,      VaadinIcon.SHOP.create()),
                new SideNavItem("Registers", RegisterView.class,   VaadinIcon.DESKTOP.create())
        );

        // ── Lasttests ──────────────────────────────────────────────────────
        SideNav performanceHeader = new SideNav("Testing");
        performanceHeader.addItem(
                new SideNavItem("Performance", PerformanceTestView.class, VaadinIcon.DASHBOARD.create())
        );

        upperDrawer.add(applicationHeader, orchestratorHeader, managementHeader, performanceHeader);
    }

    private void configureLowerDrawer() {
        SideNav externalHeader = new SideNav("External");
        externalHeader.addItem(
                new SideNavItem("Grafana",    "http://localhost:3000",                                                          VaadinIcon.CHART.create()),
                new SideNavItem("Neon",       "https://console.neon.tech/app/projects/bitter-feather-66001186",                 VaadinIcon.DATABASE.create()),
                new SideNavItem("CloudAMQP",  "https://api.cloudamqp.com/console/649e89f9-9795-4012-9e9b-fbf61637e747",        VaadinIcon.CLOUD.create()),
                new SideNavItem("Swagger",    "http://localhost:8080/swagger",                                                  VaadinIcon.CODE.create())
        );

        lowerDrawer.add(externalHeader);
    }

    @Override
    protected VerticalLayout drawer() {
        Div spacer = new Div();

        VerticalLayout drawer = new VerticalLayout(upperDrawer, spacer, lowerDrawer);
        drawer.setHeightFull();
        drawer.expand(spacer);
        drawer.setPadding(false);

        return drawer;
    }

    @Override
    protected String applicationTitle() {
        return "Orchestrator";
    }
}