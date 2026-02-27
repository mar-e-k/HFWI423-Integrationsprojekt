package de.fhdw.vendix.commons.ui.vaadin.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhdw.vendix.commons.ui.vaadin.components.LiveClockComponent;
import de.fhdw.vendix.commons.ui.vaadin.components.ThemeToggleComponent;

public final class Topbar extends HorizontalLayout {

    private final H1 viewTitle = new H1("placeholder");

    public Topbar() {
        setWidthFull();
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        setAlignItems(Alignment.CENTER);

        add(createLeftSection(), createCenterSection(), createRightSection());
    }

    public Topbar(String title) {
        setViewTitle(title);
        setWidthFull();
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        setAlignItems(Alignment.CENTER);

        add(createLeftSection(), createCenterSection(), createRightSection());
    }

    HorizontalLayout createLeftSection() {
        HorizontalLayout left = new HorizontalLayout(viewTitle);
        left.setJustifyContentMode(JustifyContentMode.START);
        left.setFlexGrow(1, viewTitle);
        return left;
    }

    HorizontalLayout createCenterSection() {
        HorizontalLayout center = new HorizontalLayout();
        center.setJustifyContentMode(JustifyContentMode.CENTER);
        center.setFlexGrow(1, center);
        return center;
    }

    HorizontalLayout createRightSection() {
        HorizontalLayout right = new HorizontalLayout();
        right.setAlignItems(Alignment.CENTER);
        right.setJustifyContentMode(JustifyContentMode.END);
        right.setSpacing(true);
        right.setFlexGrow(1, right);

        LiveClockComponent clock = new LiveClockComponent();
        ThemeToggleComponent themeToggle = new ThemeToggleComponent();
        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        right.add(clock, themeToggle, logoutButton);
        return right;
    }

    public void setViewTitle(String title) {
        viewTitle.setText(title);
    }
}