package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractLoginView;

@StyleSheet(Aura.STYLESHEET)
@Route(value = "login", autoLayout = false)
@PageTitle("Store Login")
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    public LoginView() {}
}