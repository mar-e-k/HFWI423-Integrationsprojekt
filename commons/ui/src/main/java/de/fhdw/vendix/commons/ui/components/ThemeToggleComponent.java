package de.fhdw.vendix.commons.ui.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ThemeToggleComponent extends Button {

    public ThemeToggleComponent() {
        setIcon(new Icon(VaadinIcon.ADJUST));
        setTooltipText("Toggle dark mode");
        addClickListener(e -> UI.getCurrent().getPage().executeJs("import('./frontend/theme-toggle.js').then(m => m.toggleTheme());"));
    }
}