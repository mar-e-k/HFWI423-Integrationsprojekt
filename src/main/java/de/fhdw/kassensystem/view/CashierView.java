package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@Route("cashier")
@RolesAllowed("CASHIER")
public class CashierView extends BaseView {

    public CashierView() {
        super(); // Call the constructor of the BaseView
        setViewTitle("Kassenansicht");

        // Additional CashierView specific components can be added here
    }

}