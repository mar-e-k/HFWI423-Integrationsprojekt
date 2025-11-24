package de.fhdw.fillialensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("")
@PermitAll
public class MainView extends VerticalLayout {

    public MainView() {
        add(new Button("Filialensystem"));
    }
}