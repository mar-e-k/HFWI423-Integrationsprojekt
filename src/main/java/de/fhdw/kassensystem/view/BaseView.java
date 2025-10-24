package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

//BasisView ist die abstrakte Klasse, die das Basis-Layout der Seiten implementiert und von der die anderen Views erben
public abstract class BaseView extends VerticalLayout {

    protected H1 viewTitle;

    public BaseView() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        viewTitle = new H1();
        topBar.add(viewTitle);

        Button logoutButton = new Button("Logout", e -> {
            UI.getCurrent().getPage().setLocation("/logout");
        });
        topBar.add(logoutButton);

        add(topBar);
        setAlignItems(FlexComponent.Alignment.CENTER);
    }

    protected void setViewTitle(String title) {
        this.viewTitle.setText(title);
    }
}