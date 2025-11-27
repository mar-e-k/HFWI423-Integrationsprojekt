package de.fhdw.fillialensystem.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.fhdw.commons.view.AbstractLoginView;
import de.fhdw.fillialensystem.utility.security.CustomUserDetails;
import jakarta.annotation.security.PermitAll;

@Route("/login")
@PageTitle("Login View")
@PermitAll
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    private final CustomUserDetails customUserDetails;

    public LoginView(CustomUserDetails customUserDetails) {
        this.customUserDetails = customUserDetails;
    }

    @Override
    protected boolean existsAtLeastOneAdminAccount() {
        return customUserDetails.existsAtLeastOneAdminAccount();
    }

    @Override
    protected boolean existsAtLeastOneCashierAccount() {
        return customUserDetails.existsAtLeastOneCashierAccount();
    }
}