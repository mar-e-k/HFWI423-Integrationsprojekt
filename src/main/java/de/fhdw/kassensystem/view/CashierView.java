package de.fhdw.kassensystem.view;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("cashier")
@RolesAllowed("CASHIER")
public class CashierView extends BaseView {

    @Override
    protected String getViewTitle() {
        return "Kassenansicht";
    }

    @Override
    protected void initView() {
        // Hier kommt die weitere Cashier-Implementierung hin
    }

}