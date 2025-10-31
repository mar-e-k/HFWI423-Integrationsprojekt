package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;


@Route(value = "", layout =  MainLayout.class)

public class HomeView extends VerticalLayout  {
    public HomeView() {
        add(new H2("Willkommen im Einkauf!"));
    }
}
