package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

//BasisView ist die abstrakte Klasse, die das Basis-Layout der Seiten implementiert und von der die anderen Views erben
public abstract class BaseView extends VerticalLayout {

    public BaseView() {
        // Top Bar
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        // Titel wird von der Subklasse geholt
        H1 viewTitle = new H1(getViewTitle());
        topBar.add(viewTitle);

        // Logout Button
        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));
        topBar.add(logoutButton);

        add(topBar);

        // Subklasse fügt ihren spezifischen Inhalt hinzu
        initView();

        setAlignItems(FlexComponent.Alignment.CENTER);
    }

     // Subklassen müssen diese Methode implementieren, um den Titel für die Ansicht zu erstellen.

    protected abstract String getViewTitle();


    // Subklassen müssen diese Methode implementieren, um ihre spezifische UI hinzuzufügen.

    protected abstract void initView();

}