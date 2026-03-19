package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.account.port.AccountCommandPort;
import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.structure.port.CrudCommandPort;
import de.fhdw.vendix.commons.api.structure.port.CrudQueryPort;
import de.fhdw.vendix.commons.security.spring.authentication.AuthenticationHandler;
import de.fhdw.vendix.commons.ui.vaadin.view.AbstractLoginView;
import org.springframework.security.crypto.password.PasswordEncoder;

@StyleSheet(Aura.STYLESHEET)
@Route(value = "login", autoLayout = false)
@PageTitle("Store Login")
@AnonymousAllowed
public class LoginView extends AbstractLoginView {

    public LoginView(
            AuthenticationHandler authenticationHandler,
            PasswordEncoder passwordEncoder,
            AccountQueryPort accountQueryPort,
            AccountCommandPort accountCommandPort,
            CrudQueryPort<AccountDTO, Long> crudQueryPort,
            CrudCommandPort<AccountDTO, Long> crudCommandPort
    ) {
        super(authenticationHandler, passwordEncoder, accountQueryPort, accountCommandPort, crudQueryPort, crudCommandPort);
    }
}