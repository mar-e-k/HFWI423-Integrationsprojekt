package de.fhdw.vendix.commons.spring.vaadin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractEntityView extends VerticalLayout {

    private final H2 title = new H2();
    private final Button createButton = new  Button();

    protected AbstractEntityView() {
        setSizeFull();

        configureTitle();
        configureButton();

        Div spacer = new Div();
        HorizontalLayout header = new HorizontalLayout(title, spacer, createButton);
        header.setWidthFull();
        header.expand(spacer);

        add(header);
    }

    private void configureTitle() {
        title.setTitle("UI.getCurrent().getInternals().getTitle()");
    }

    private void configureButton() {
        createButton.addClickListener(event -> {
            Dialog dialog = createEntityDialog();
            if (dialog != null) {
                dialog.open();
            }
        });
    }

    protected abstract Dialog createEntityDialog();
}