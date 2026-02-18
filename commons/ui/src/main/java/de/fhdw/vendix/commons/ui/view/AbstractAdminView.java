package de.fhdw.vendix.commons.ui.view;

import de.fhdw.vendix.commons.ui.layout.Sidebar;
import de.fhdw.vendix.commons.ui.layout.Topbar;

public abstract class AbstractAdminView extends AbstractView {

    public AbstractAdminView() {
        add(new Topbar());
        add(new Sidebar());
//        add(new Sidebar((List.of(
//                new SidebarEntry("Home", VaadinIcon.HOME, MainView.class),
//                new SidebarEntry("Test", VaadinIcon.HOME, TestView.class),
//                new SidebarEntry("Admin", VaadinIcon.USER, AdminView.class),
//                new SidebarEntry("Accounts ", VaadinIcon.GROUP, RoleView.class),
//                new SidebarEntry("Kassen", VaadinIcon.CASH, RegisterAddView.class),
//                new SidebarEntry("Bestand", VaadinIcon.PACKAGE, StockView.class),
//                new SidebarEntry("Belege", VaadinIcon.RECORDS, DailyReceiptReportingView.class)))));
    }
}