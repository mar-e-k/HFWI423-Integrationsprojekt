package de.fhdw.kassensystem.view;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@RolesAllowed("ADMIN")
public class AdminView extends BaseView {

    public AdminView() {
        super(); // Call the constructor of the BaseView
        setViewTitle("Admin-Dashboard");

        // Additional AdminView specific components can be added here
    }
}