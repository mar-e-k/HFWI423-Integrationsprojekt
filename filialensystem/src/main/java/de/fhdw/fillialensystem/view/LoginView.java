package de.fhdw.fillialensystem.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.commons.view.AbstractLoginView;

@Route("/login")
@PageTitle("Login View")
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    public LoginView() {
        super();
    }
}