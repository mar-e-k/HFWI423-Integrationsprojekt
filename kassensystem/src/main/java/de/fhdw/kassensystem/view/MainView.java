package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.api.dto.AccountDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@RolesAllowed({AccountRoleEnum.ROLE_CASHIER})
public class MainView extends AbstractMainView implements BeforeEnterObserver {
    public MainView() {
        add(new H1("MainView"));
    }

    @Override
    protected void init() {}

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AccountDTO)) {
            beforeEnterEvent.rerouteTo(LoginView.class);
        } else {
            beforeEnterEvent.rerouteTo(CashierView.class);
        }
    }
}