package de.fhdw.vendix.commons.ui.view;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.security.auth.AuthContext;
import de.fhdw.vendix.commons.security.auth.AuthContextHolder;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@StyleSheet(Aura.STYLESHEET)
public abstract class AbstractView extends VerticalLayout implements BeforeEnterObserver {

    public AbstractView() {}

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<AuthContext> authContext = AuthContextHolder.current();

        if (authContext.isEmpty() || authContext.get().getRoles().isEmpty()) {
            beforeEnterEvent.rerouteTo("/login");
            return;
        }

        Set<String> userAuthorities = authContext.get().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        RolesAllowed rolesAllowed = beforeEnterEvent.getNavigationTarget().getAnnotation(RolesAllowed.class);
        if (rolesAllowed != null && Arrays.stream(rolesAllowed.value()).noneMatch(userAuthorities::contains)) {
            beforeEnterEvent.rerouteTo("/logout");
        }
    }
}