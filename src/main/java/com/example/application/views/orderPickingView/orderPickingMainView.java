package com.example.application.views.orderPickingView;

import com.example.application.data.orderPicking.Kommission;
import com.example.application.services.KommissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Route("order-picking")
@PageTitle("Order Picking")
@Menu(order = 6, icon = LineAwesomeIconUrl.TRUCK_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)
public class orderPickingMainView extends VerticalLayout {

    private final KommissionService service;
    private final Grid<Kommission> grid = new Grid<>(Kommission.class, false);

    public orderPickingMainView(KommissionService service) {
        this.service = service;
        setSizeFull();

        grid.addColumn(Kommission::getOrderPickingNumber).setHeader("Nr");
        grid.addColumn(Kommission::getDate).setHeader("Erstellt");
        grid.addColumn(k -> k.getStore() != null ? k.getStore() : "").setHeader("Lieferung/Store");
        grid.addComponentColumn(k -> {
            Button open = new Button("Öffnen");
            open.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(orderPickingDetailView.class)));
            return open;
        }).setHeader("Aktion");

        grid.setItems(service.getOffeneKommissionen());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);


        add(new H2("Offene Kommissionen"), grid);
    }
}