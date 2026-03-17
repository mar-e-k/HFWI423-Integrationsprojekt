package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractHomeView extends VerticalLayout {

    protected AbstractHomeView() {
       super.add(new H1("Vendix"));
    }
}