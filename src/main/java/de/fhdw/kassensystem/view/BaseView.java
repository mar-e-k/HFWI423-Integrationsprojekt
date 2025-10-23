package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class BaseView extends VerticalLayout {

    protected H1 viewTitle; // To be set by subclasses

    public BaseView() {
        // Create a top bar
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        viewTitle = new H1(); // Initialize, actual title set by subclass
        topBar.add(viewTitle);

        Button logoutButton = new Button("Logout", e -> {
            UI.getCurrent().getPage().setLocation("/logout");
        });
        topBar.add(logoutButton);

        add(topBar); // Add the top bar to the main layout
        setAlignItems(FlexComponent.Alignment.CENTER); // Center content below top bar
    }

    protected void setViewTitle(String title) {
        this.viewTitle.setText(title);
    }
}