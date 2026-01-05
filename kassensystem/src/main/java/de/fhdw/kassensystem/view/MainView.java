package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.ui.view.AbstractView;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed(AccountRoleEnum.ROLE_CASHIER)
public class MainView extends AbstractView implements BeforeEnterObserver {

    public MainView() {
        UI.getCurrent().navigate(CashierView.class);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        super.beforeEnter(beforeEnterEvent);
        beforeEnterEvent.rerouteTo(CashierView.class);
    }
}