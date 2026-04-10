package de.fhdw.vendix.commons.spring.vaadin.view;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import de.fhdw.vendix.commons.spring.web.handler.SessionAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AbstractLoginView.class);

    private final LoginForm loginForm;

    protected AbstractLoginView() {
        this.loginForm = new LoginForm();
        configureLoginForm();
    }

    private void configureLoginForm() {
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.setAction("login");
        super.setAlignItems(Alignment.CENTER);
        super.setJustifyContentMode(JustifyContentMode.CENTER);
        super.setSizeFull();
        super.add(loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        WrappedSession session = VaadinSession.getCurrent().getSession();
        String errorCode = (String) session.getAttribute(SessionAttribute.LOGIN_ERROR.name());

        if (errorCode != null) {
            String errorTitle = resolveErrorCodeTitle(errorCode);
            String errorMessage = resolveErrorCodeMessage(errorCode);
            loginForm.setError(true);
            loginForm.showErrorMessage(errorTitle, errorMessage);
            session.setAttribute(SessionAttribute.LOGIN_ERROR.name(), null);
        }
    }

    private String resolveErrorCodeTitle(String errorCode) {
        return switch (errorCode) {
            case "bad_credentials" -> "Login failed";
            case "account_locked" -> "Account locked";
            case "account_disabled" -> "Account unavailable";
            default -> "Login error";
        };
    }

    private String resolveErrorCodeMessage(String errorCode) {
        return switch (errorCode) {
            case "bad_credentials" -> "The username or password you entered is incorrect. Please try again.";
            case "account_locked" -> "Your account has been temporarily locked. Please try again later or contact support.";
            case "account_disabled" -> "Your account is currently disabled. Please contact support if you believe this is a mistake.";
            default -> "We couldn’t sign you in. Please try again or contact support if the problem persists.";
        };
    }
}