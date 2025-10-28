package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.config.Roles;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@PermitAll
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(Roles.ADMIN.name()));

        // Weiterleitung zur richtigen View, falls Admin dann dahin, sonst immer zur CashierView
        if (isAdmin) {
            event.rerouteTo(AdminView.class);
        } else {
            event.rerouteTo(CashierView.class);
        }
    }
}