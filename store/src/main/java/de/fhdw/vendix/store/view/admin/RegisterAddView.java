package de.fhdw.vendix.store.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.aura.Aura;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.entity.Register;
import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.service.RegisterService;
import de.fhdw.vendix.store.persistence.service.StoreService;
import de.fhdw.vendix.store.view.MainView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("register")
@PageTitle("Register View")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
@StyleSheet(Aura.STYLESHEET)
public class RegisterAddView extends AppLayout implements BeforeEnterObserver {

    private final RegisterService registerService;
    private final StoreService storeService;

    private Grid<Register> registerGrid;
    private ComboBox<Store> storeComboBox;

    public RegisterAddView(RegisterService registerService, StoreService storeService) {
        this.registerService = registerService;
        this.storeService = storeService;

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
            UI ui = UI.getCurrent(); // UI-Referenz sichern BEVOR async-Aufruf
            ui.getPage().executeJs("return document.documentElement.getAttribute('theme');")
                    .then(String.class, currentClientTheme -> {
                        var themeList = ui.getElement().getThemeList();
                        boolean isClientDark = "dark".equals(currentClientTheme);

                        if (isClientDark) {
                            themeList.remove(Lumo.DARK);
                            ui.getPage().executeJs("localStorage.setItem('theme', 'light'); document.documentElement.removeAttribute('theme');");
                        } else {
                            themeList.add(Lumo.DARK);
                            ui.getPage().executeJs("localStorage.setItem('theme', 'dark'); document.documentElement.setAttribute('theme', 'dark');");
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
                createSidebarLink("Home",    VaadinIcon.HOME,    MainView.class),
                createSidebarLink("Admin",   VaadinIcon.USER,    AdminView.class),
                createSidebarLink("Accounts", VaadinIcon.GROUP,  RoleView.class),
                createSidebarLink("Kassen",  VaadinIcon.CASH,    RegisterAddView.class),
                createSidebarLink("Bestand", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Belege",  VaadinIcon.RECORDS, DailyReceiptReportingView.class),
                createSidebarLink("Lasttests", VaadinIcon.CHART_LINE, PerformanceTestView.class)
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

        H2 title = new H2("Manage Registers");
        content.add(title);

        List<Store> allStores = storeService.findAll();

        storeComboBox = new ComboBox<>("Select Store");
        storeComboBox.setItems(allStores);
        storeComboBox.setItemLabelGenerator(store -> store.getCity() + ", " + store.getStreet() + " " + store.getStreetNumber());

        registerGrid = new Grid<>(Register.class, false);
        registerGrid.addColumn(reg -> reg.getId()).setHeader("ID").setAutoWidth(true);
        registerGrid.addColumn(reg -> reg.getStore().getCity() + ", " + reg.getStore().getStreet() + " " + reg.getStore().getStreetNumber())
                .setHeader("Store")
                .setAutoWidth(true);
        registerGrid.setWidthFull();

        List<Register> allRegisters = registerService.findAll();
        registerGrid.setItems(allRegisters);

        Button addRegisterButton = new Button("Add Register", click -> {
            Store selectedStore = storeComboBox.getValue();
            if (selectedStore == null) {
                Notification.show("Please select a store first", 3000, Notification.Position.MIDDLE);
                return;
            }

            Register newRegister = new Register();
            newRegister.setStore(selectedStore);

            registerService.create(newRegister);
            Notification.show("Register added for store " + selectedStore.getCity(), 3000, Notification.Position.MIDDLE);

            registerGrid.setItems(registerService.findAll());
        });

        HorizontalLayout addLayout = new HorizontalLayout(storeComboBox, addRegisterButton);
        addLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        content.add(addLayout);

        content.add(registerGrid);
        content.setFlexGrow(1, registerGrid);

        setContent(content);
    }

    private String getPageTitle() {
        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
        return titleAnnotation != null ? titleAnnotation.value() : "Register View";
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
