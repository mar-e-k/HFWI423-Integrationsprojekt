package de.fhdw.vendix.commons.spring.vaadin.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.popover.Popover;

public class NotificationComponent extends Composite<Div> {

    private final Button button = new Button(VaadinIcon.BELL.create());
    private final Popover popover = new  Popover();

    public NotificationComponent() {
        configureButton();
        configurePopover();

        getContent().add(button);
    }

    private void configureButton() {}

    private void configurePopover() {
        popover.setTarget(button);
        popover.setWidth("20em");
        popover.setModal(true);
    }
}