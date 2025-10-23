package de.fhdw.kassensystem.view;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@RolesAllowed("ADMIN")
public class AdminView extends BaseView {

    public AdminView() {
        super();
        setViewTitle("Admin-Dashboard");

        // Hier kommt die weitere Admin-Implementierung hin
    }
}