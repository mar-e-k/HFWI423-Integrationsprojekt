package de.fhdw.vendix.store.ui.business;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.store.core.domain.register.Register;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class RegisterView extends VerticalLayout {

    public RegisterView() {
        add(initGrid());
    }

    private Grid<Register> initGrid() {
        Grid<Register> registerGrid = new Grid<>(Register.class, false);
        registerGrid.setHeightFull();
        registerGrid.setWidthFull();
        registerGrid.addColumn(Register::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        registerGrid.addColumn(Register::getStore)
                .setHeader("Store")
                .setAutoWidth(true)
                .setSortable(true);
        return registerGrid;
    }
}