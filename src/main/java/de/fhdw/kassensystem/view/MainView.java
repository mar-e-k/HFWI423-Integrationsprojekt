package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.view.admin.AdminView;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@PermitAll
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            if (auth.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals(AccountRoleEnum.ADMIN.getAuthority()))) {
                event.rerouteTo(AdminView.class);
            } else {
                event.rerouteTo(CashierView.class);
            }
        } else {
            event.rerouteTo(LoginView.class);
        }
    }
}