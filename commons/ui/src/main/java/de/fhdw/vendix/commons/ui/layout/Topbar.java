package de.fhdw.vendix.commons.ui.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.fhdw.vendix.commons.ui.components.LiveClockComponent;
import de.fhdw.vendix.commons.ui.components.ThemeToggleComponent;
import org.springframework.aop.support.AopUtils;

public final class Topbar extends HorizontalLayout {

    private final H1 viewTitle = new H1(AopUtils.getTargetClass(this).getSimpleName());

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
        left.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        left.setFlexGrow(1, viewTitle);
        return left;
    }

    HorizontalLayout createCenterSection() {
        HorizontalLayout center = new HorizontalLayout();
        center.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        center.setFlexGrow(1, center);
        return center;
    }

    HorizontalLayout createRightSection() {
        HorizontalLayout right = new HorizontalLayout();
        right.setAlignItems(FlexComponent.Alignment.CENTER);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
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