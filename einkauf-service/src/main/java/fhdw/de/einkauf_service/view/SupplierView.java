package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;


@Route(value = "suppliers", layout =  MainLayout.class)
public class SupplierView extends VerticalLayout  {
    public SupplierView() {
        add(new H2("Lieferanten"));
    }
}
