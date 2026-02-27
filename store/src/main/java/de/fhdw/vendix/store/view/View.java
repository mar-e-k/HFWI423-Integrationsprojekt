package de.fhdw.vendix.store.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.vendix.commons.ui.view.AbstractView;

@Route("/login")
@PageTitle("Login View")
@AnonymousAllowed
public class View extends AbstractView {
    public View() {}
}