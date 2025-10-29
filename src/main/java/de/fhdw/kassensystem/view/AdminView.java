package de.fhdw.kassensystem.view;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@RolesAllowed("ADMIN")
public class AdminView extends BaseView {

    @Override
    protected String getViewTitle() {
        return "Admin-Dashboard";
    }

    @Override
    protected void initView() {
        // Hier kommt die weitere Admin-Implementierung hin
    }
}