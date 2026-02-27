package de.fhdw.vendix.pos.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.pos.view.cashier.CashierView;
import de.fhdw.vendix.commons.ui.view.BaseView;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed(AccountRoleEnum.ROLE_CASHIER)
public class MainView extends BaseView implements BeforeEnterObserver {

    public MainView() {
        UI.getCurrent().navigate(CashierView.class);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        super.beforeEnter(beforeEnterEvent);
        beforeEnterEvent.rerouteTo(CashierView.class);
    }
}