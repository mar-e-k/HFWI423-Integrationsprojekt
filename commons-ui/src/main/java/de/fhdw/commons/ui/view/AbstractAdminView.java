package de.fhdw.commons.ui.view;

import com.vaadin.flow.component.icon.VaadinIcon;
import de.fhdw.commons.ui.layout.Sidebar;
import de.fhdw.commons.ui.layout.SidebarEntry;
import de.fhdw.commons.ui.layout.Topbar;

import java.util.List;

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