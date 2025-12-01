package de.fhdw.commons.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.commons.utility.AuthContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public abstract class AbstractMainView extends VerticalLayout implements BeforeEnterObserver {

    protected H1 viewTitle;

    public AbstractMainView() {
        UI.getCurrent().getPage().executeJs(
                "const storedTheme = localStorage.getItem('theme');" +
                        "if (storedTheme === 'dark') {" +
                        "    document.documentElement.setAttribute('theme', 'dark');" +
                        "}"
        );

        // Top Bar vorbereiten
        setupTopBar();

        setAlignItems(Alignment.CENTER);
    }

    private void setupTopBar() {
        // Haupt-Container für die Top-Bar
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topBar.setAlignItems(Alignment.CENTER);

        // Linker Bereich: Titel
        viewTitle = new H1(AopUtils.getTargetClass(this).getSimpleName());
        HorizontalLayout leftSection = new HorizontalLayout(viewTitle);
        leftSection.setJustifyContentMode(JustifyContentMode.START);
        leftSection.setWidth("33.33%");

        // Mittlerer Bereich: Optionale Buttons
        HorizontalLayout centerSection = createTopBarButtons();
        centerSection.setJustifyContentMode(JustifyContentMode.CENTER);
        centerSection.setWidth("33.33%");

        // Rechter Bereich: Uhr, Theme-Toggle, Logout
        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
        liveClockLabel.getStyle().set("font-weight", "bold");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            UI.getCurrent().getPage().executeJs("return document.documentElement.getAttribute('theme');")
                    .then(String.class, currentClientTheme -> {
                        var themeList = UI.getCurrent().getElement().getThemeList();
                        boolean isClientDark = "dark".equals(currentClientTheme);

                        if (isClientDark) {
                            themeList.remove(Lumo.DARK);
                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'light');");
                            UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme');");
                        } else {
                            themeList.add(Lumo.DARK);
                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'dark');");
                            UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
                        }
                    });
        });
        themeToggleButton.setTooltipText("Toggle dark mode");

        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
        rightSection.setAlignItems(Alignment.CENTER);
        rightSection.setJustifyContentMode(JustifyContentMode.END);
        rightSection.setSpacing(true);
        rightSection.setWidth("33.33%");

        // Alle Sektionen zur Top-Bar hinzufügen
        topBar.add(leftSection, centerSection, rightSection);
        add(topBar);

        // JavaScript Live-Uhr
        UI.getCurrent().getPage().executeJs("""
            const label = document.getElementById('live-clock-label');
            if (label) {
                setInterval(() => {
                    const now = new Date();
                    label.textContent = now.toLocaleString('de-DE', {
                        year: 'numeric', month: '2-digit', day: '2-digit',
                        hour: '2-digit', minute: '2-digit', second: '2-digit'
                    });
                }, 1000);
            }
        """);
    }

    protected void setViewTitle(String title) {
        viewTitle.setText(title);
    }

    protected HorizontalLayout createTopBarButtons() {
        return new HorizontalLayout();
    }

    protected abstract void init();

    @PostConstruct
    private void postConstructInit() {
        init();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        setAuthenticationFromSession();

        Class<?> targetView = beforeEnterEvent.getNavigationTarget();

        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null || auth.getPrincipal().toString().equalsIgnoreCase("anonymousUser")) {
            beforeEnterEvent.rerouteTo("login", QueryParameters.simple(Map.of("error", ErrorQueryParameter.LOGIN_REQUIRED.value())));
            return;
        }

        if (auth.getAuthorities().isEmpty()) {
            beforeEnterEvent.rerouteTo("login", QueryParameters.simple(Map.of("error", ErrorQueryParameter.ROLES_MISSING.value())));
            return;
        }

        boolean authorized = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authRole -> {
                    for (String requiredRole : rolesAllowed.value()) {
                        if (authRole.equals(requiredRole)) {
                            return true;
                        }
                    }
                    return false;
                });

        if (!authorized) {
            beforeEnterEvent.rerouteTo("login", QueryParameters.simple(Map.of("error", ErrorQueryParameter.ACCESS_DENIED.value())));
        }
    }

    private void setAuthenticationFromSession() {
        Object authContext = VaadinSession.getCurrent().getAttribute("auth-context");

        if (authContext instanceof AuthContext) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(authContext, null, ((AuthContext) authContext).getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}
