package de.fhdw.vendix.store.ui.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.core.domain.account.Account;
import de.fhdw.vendix.store.core.domain.account_role.AccountRole;
import de.fhdw.vendix.store.core.domain.account_role.AccountRoleService;
import de.fhdw.vendix.store.core.domain.account.AccountService;
import de.fhdw.vendix.store.ui.MainView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Route("/roles")
@PageTitle("Roles View")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class RoleView extends AppLayout implements BeforeEnterObserver {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;
    private final PasswordEncoder passwordEncoder;

    private final Grid<Account> accountGrid = new Grid<>(Account.class, false);

    public RoleView(AccountService accountService, AccountRoleService accountRoleService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
        this.passwordEncoder = passwordEncoder;

        createHeader();
        addToDrawer(createSidebar());
    }

    private void createHeader() {
        H1 viewTitle = new H1(getPageTitle());
        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

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
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rightSection.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        addToNavbar(header);

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

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);

        sidebar.add(
                createSidebarLink("Home", VaadinIcon.HOME, MainView.class),
                createSidebarLink("Admin View", VaadinIcon.USER, AdminView.class),
                createSidebarLink("Role View", VaadinIcon.GROUP, RoleView.class),
                createSidebarLink("Register View", VaadinIcon.CASH, RegisterAddView.class),
                createSidebarLink("Stock View", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Daily Receipt Reporting", VaadinIcon.RECORDS, DailyReceiptReportingView.class)
        );
        return sidebar;
    }

    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
        com.vaadin.flow.component.icon.Icon i = new com.vaadin.flow.component.icon.Icon(icon);
        Span textSpan = new Span(text);
        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");

        RouterLink link = new RouterLink(navigationTarget);
        link.add(i, textSpan);
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("padding", "var(--lumo-space-s)");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        link.getStyle().set("transition", "background-color 0.2s");
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--lumo-body-text-color)");

        return link;
    }

    @PostConstruct
    public void initUI() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        //----------------------------------------------------------
        // FORM BEREICH
        //----------------------------------------------------------
        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Passwort");
        passwordField.setRequired(true);

        Select<AccountRoleEnum> roleSelect = new Select<>();
        roleSelect.clear();
        roleSelect.setItems(List.of(AccountRoleEnum.CASHIER, AccountRoleEnum.ADMIN));
        roleSelect.setLabel("Rolle");
        roleSelect.setRequiredIndicatorVisible(true);

        Button createUserBtn = new Button("Neuen Benutzer anlegen");

        FormLayout formLayout = new FormLayout(
                usernameField,
                passwordField,
                roleSelect,
                createUserBtn
        );

        VerticalLayout wrapper = new VerticalLayout(formLayout);
        wrapper.setWidth("400px");
        content.add(wrapper);

        //----------------------------------------------------------
        // TABELLE MIT ACCOUNTS
        //----------------------------------------------------------
        accountGrid.addColumn(Account::getUuid)
                .setHeader("Personalnummer")
                .setSortable(true)
                .setAutoWidth(true);

        accountGrid.addColumn(Account::getUsername)
                .setHeader("Username")
                .setSortable(true)
                .setAutoWidth(true);

        // Rollen-Spalte mit Bearbeitungsmöglichkeit
        accountGrid.addComponentColumn(account -> {
            Select<AccountRoleEnum> roleEditor = new Select<>();
            roleEditor.clear();
            roleEditor.setItems(List.of(AccountRoleEnum.CASHIER, AccountRoleEnum.ADMIN));
            roleEditor.setValue(account.getRole().getRole());

            Button saveButton = new Button("Speichern");
            saveButton.setVisible(false);

            roleEditor.addValueChangeListener(event -> saveButton.setVisible(true));

            saveButton.addClickListener(e -> {
                Optional<AccountRole> newRoleOpt = accountRoleService.findByRole(roleEditor.getValue());
                if (newRoleOpt.isPresent()) {
                    account.setAccountRole(newRoleOpt.get());
                    accountService.update(account);
                    Notification.show("Rolle aktualisiert!", 2000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    saveButton.setVisible(false);
                    refreshGrid();
                } else {
                    Notification.show("Fehler: Rolle nicht gefunden!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            HorizontalLayout editorLayout = new HorizontalLayout(roleEditor, saveButton);
            editorLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            return editorLayout;
        }).setHeader("Rolle").setSortable(true).setComparator(Comparator.comparing(account -> account.getRole().getRole().name())).setAutoWidth(true);


        // Löschen-Button-Spalte
        accountGrid.addComponentColumn(account -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.getElement().setProperty("title", "Benutzer löschen");

            deleteButton.addClickListener(e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Benutzer löschen?");
                dialog.setText("Möchten Sie den Benutzer '" + account.getUsername() + "' wirklich löschen?");
                dialog.setConfirmText("Ja");
                dialog.setCancelText("Nein");
                dialog.addConfirmListener(event -> {
                    try {
                        // Hier ID statt Objekt übergeben
                        accountService.delete(account.getId());
                        Notification.show("Benutzer erfolgreich gelöscht!", 2000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        refreshGrid();
                    } catch (Exception ex) {
                        Notification.show("Fehler beim Löschen: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                dialog.open();
            });

            return deleteButton;
        }).setHeader("Löschen").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        refreshGrid();
        content.add(accountGrid);
        content.setFlexGrow(1, accountGrid);

        //----------------------------------------------------------
        // EVENT: BENUTZER ANLEGEN
        //----------------------------------------------------------
        createUserBtn.addClickListener(e -> {

            if (usernameField.isEmpty()
                    || passwordField.isEmpty()
                    || roleSelect.isEmpty()) {

                Notification.show("Bitte alle Felder ausfüllen!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            Optional<AccountRole> existingRole =
                    accountRoleService.findByRole(roleSelect.getValue());

            AccountRole role = existingRole.orElseGet(() -> accountRoleService.create(new AccountRole(roleSelect.getValue())));

            Account account = new Account();
            account.setUuid(UUID.randomUUID().toString());
            account.setUsername(usernameField.getValue());
            account.setPassword(passwordEncoder.encode(passwordField.getValue()));
            account.setAccountRole(role);

            try {
                accountService.create(account);
                Notification.show("Benutzer erfolgreich angelegt!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Felder leeren nach erfolgreicher Eingabe
                usernameField.clear();
                passwordField.clear();
                roleSelect.clear();

                refreshGrid();

            } catch (Exception ex) {
                String errorMessage = "Fehler beim Anlegen des Benutzers: " + (ex.getMessage() != null ? ex.getMessage() : "Unbekannter Fehler");
                Notification.show(errorMessage, 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                // In einer echten Anwendung würde man die Ausnahme hier auch loggen.
            }
        });

        setContent(content);
    }

    private void refreshGrid() {
        accountGrid.setItems(accountService.findAll());
    }

    private String getPageTitle() {
        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
        return titleAnnotation != null ? titleAnnotation.value() : "Roles View";
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Class<?> targetView = beforeEnterEvent.getNavigationTarget();

        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            beforeEnterEvent.rerouteTo("login");
            return;
        }

        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean authorized = Arrays.stream(rolesAllowed.value())
                .anyMatch(userAuthorities::contains);

        if (!authorized) {
            beforeEnterEvent.rerouteTo("login");
        }
    }
}
