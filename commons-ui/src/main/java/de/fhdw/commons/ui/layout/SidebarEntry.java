package de.fhdw.commons.ui.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

public record SidebarEntry(
        String label,
        VaadinIcon icon,
        Class<? extends Component> target
) {
    public SidebarEntry {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
        if (icon == null) {
            throw new IllegalArgumentException("Icon cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target cannot be null");
        }
    }
}