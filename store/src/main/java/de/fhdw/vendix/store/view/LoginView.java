package de.fhdw.vendix.store.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.ui.view.AbstractLoginView;

@Route("/login")
@PageTitle("Login View")
@StyleSheet(Aura.STYLESHEET)
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    public LoginView() {

    }
}