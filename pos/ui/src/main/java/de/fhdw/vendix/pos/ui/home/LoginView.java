package de.fhdw.vendix.pos.ui.home;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.vendix.commons.spring.vaadin.view.AbstractLoginView;

@Route(value = "login", autoLayout = false)
@PageTitle("POS Login")
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    public LoginView() {}
}