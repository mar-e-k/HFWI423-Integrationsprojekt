package de.fhdw.vendix.store.old_ui.admin;

//@Route("/admin")
//@PageTitle("Admin View")
//@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class AdminView
//        extends AppLayout implements BeforeEnterObserver
{
//
//
//    private Grid<Account> grid;
//
//    public AdminView() {
//        createHeader();
//        addToDrawer(createSidebar());
//    }
//
//    private void createHeader() {
//        H1 viewTitle = new H1(getPageTitle());
//        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
//        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
//
//        Span liveClockLabel = new Span();
//        liveClockLabel.setId("live-clock-label");
//        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
//        liveClockLabel.getStyle().set("font-weight", "bold");
//
//        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
//            UI.getCurrent().getPage().executeJs("return document.documentElement.getAttribute('theme');")
//                    .then(String.class, currentClientTheme -> {
//                        var themeList = UI.getCurrent().getElement().getThemeList();
//                        boolean isClientDark = "dark".equals(currentClientTheme);
//
//                        if (isClientDark) {
//                            themeList.remove(Lumo.DARK);
//                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'light');");
//                            UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme');");
//                        } else {
//                            themeList.add(Lumo.DARK);
//                            UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'dark');");
//                            UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
//                        }
//                    });
//        });
//        themeToggleButton.setTooltipText("Toggle dark mode");
//
//        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));
//
//        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
//        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
//        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
//        rightSection.setSpacing(true);
//
//        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
//        header.setWidthFull();
//        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
//        header.setAlignItems(FlexComponent.Alignment.CENTER);
//
//        addToNavbar(header);
//
//        UI.getCurrent().getPage().executeJs("""
//            const label = document.getElementById('live-clock-label');
//            if (label) {
//                setInterval(() => {
//                    const now = new Date();
//                    label.textContent = now.toLocaleString('de-DE', {
//                        year: 'numeric', month: '2-digit', day: '2-digit',
//                        hour: '2-digit', minute: '2-digit', second: '2-digit'
//                    });
//                }, 1000);
//            }
//        """);
//    }
//
//    private Component createSidebar() {
//        VerticalLayout sidebar = new VerticalLayout();
//        sidebar.setPadding(false);
//        sidebar.setSpacing(false);
//        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);
//
//        sidebar.add(
//                createSidebarLink("Home", VaadinIcon.HOME, MainView.class),
//                createSidebarLink("Admin View", VaadinIcon.USER, AdminView.class),
//                createSidebarLink("Role View", VaadinIcon.GROUP, RoleView.class),
//                createSidebarLink("Register View", VaadinIcon.CASH, RegisterAddView.class),
//                createSidebarLink("Stock View", VaadinIcon.PACKAGE, StockView.class),
//                createSidebarLink("Daily Receipt Reporting", VaadinIcon.RECORDS, DailyReceiptReportingView.class)
//        );
//        return sidebar;
//    }
//
//    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
//        com.vaadin.flow.component.icon.Icon i = new com.vaadin.flow.component.icon.Icon(icon);
//        Span textSpan = new Span(text);
//        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");
//
//        RouterLink link = new RouterLink(navigationTarget);
//        link.add(i, textSpan);
//        link.getStyle().set("display", "flex");
//        link.getStyle().set("align-items", "center");
//        link.getStyle().set("padding", "var(--lumo-space-s)");
//        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
//        link.getStyle().set("transition", "background-color 0.2s");
//        link.getStyle().set("text-decoration", "none");
//        link.getStyle().set("color", "var(--lumo-body-text-color)");
//
//        return link;
//    }
//
//    @PostConstruct
//    public void initUI() {
//        VerticalLayout content = new VerticalLayout();
//        content.setSizeFull();
//        content.setPadding(true);
//        content.setSpacing(true);
//        content.setAlignItems(FlexComponent.Alignment.STRETCH);
//
//        grid = new Grid<>(Account.class, false);
//        grid.setSizeFull();
//        grid.getStyle().set("margin-top", "5em");
//
//
//        grid.addColumn(account -> DateTimeFormat.UI_DATE_TIME.format(account.getCreatedAt()))
//                .setHeader("Erstellt am")
//                .setSortable(true);
//        grid.addColumn(Account::getCreatedBy)
//                .setHeader("Erstellt von")
//                .setSortable(true);
//        grid.addColumn(account -> DateTimeFormat.UI_DATE_TIME.format(account.getChangedAt()))
//                .setHeader("Geändert am")
//                .setSortable(true);
//        grid.addColumn(Account::getChangedBy)
//                .setHeader("Geändert von")
//                .setSortable(true);
//        grid.addColumn(Account::getUuid)
//                .setHeader("Account ID")
//                .setSortable(true);
//        grid.addColumn(Account::getUsername)
//                .setHeader("Username")
//                .setSortable(true);
//        grid.addColumn(account -> account.getRole().getRole().name())
//                .setHeader("Rolle")
//                .setSortable(true);
//
//        updateGrid();
//        content.add(grid);
//        content.setFlexGrow(1, grid);
//        setContent(content);
//    }
//
//    private void updateGrid() {
//        grid.setItems(accountService.findAll());
//    }
//
//    private String getPageTitle() {
//        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
//        return titleAnnotation != null ? titleAnnotation.value() : "Admin View";
//    }
//
//    @Override
//    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
//        Class<?> targetView = beforeEnterEvent.getNavigationTarget();
//
//        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
//        if (rolesAllowed == null) {
//            return;
//        }
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
//            beforeEnterEvent.rerouteTo("login");
//            return;
//        }
//
//        Set<String> userAuthorities = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toSet());
//
//        boolean authorized = Arrays.stream(rolesAllowed.value())
//                .anyMatch(userAuthorities::contains);
//
//        if (!authorized) {
//            beforeEnterEvent.rerouteTo("login");
//        }
//    }
}