package de.fhdw.kassensystem.view;

import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import jakarta.annotation.security.RolesAllowed;

@Route("")
@RolesAllowed(AccountRoleEnum.ROLE_CASHIER)
public class MainView extends AbstractMainView {

    public MainView() {
        super();
    }

    @Override
    protected void init() {

    }
}