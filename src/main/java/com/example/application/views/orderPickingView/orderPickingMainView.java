package com.example.application.views.orderPickingView;

import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionPosition;
import com.example.application.services.KommissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
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
            Checkbox cb = new Checkbox(k.getFinished());
            cb.addValueChangeListener(e -> {
                k.setFinished(e.getValue());
                service.save(k);   // musst du anlegen
                grid.getDataProvider().refreshAll();
            });
            return cb;
        }).setHeader("✔");
        grid.addComponentColumn(k -> {
            Button open = new Button("Öffnen");

            open.addClickListener(e -> {

                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Kommission " + k.getOrderPickingNumber());
                dialog.setWidth("900px");

                // Layout als Container für das Grid
                VerticalLayout layout = new VerticalLayout();
                layout.setWidthFull();      // ⬅ wichtig
                layout.setPadding(false);
                layout.setSpacing(false);

                Grid<KommissionPosition> posGrid = new Grid<>(KommissionPosition.class, false);
                posGrid.setWidthFull();     // ⬅ wichtig
                posGrid.setHeight("400px"); // optional

                posGrid.addColumn(KommissionPosition::getArticle_id)
                        .setHeader("Article-Nr.")
                        .setAutoWidth(true);

                posGrid.addColumn(pos -> service.getArticleName(pos.getArticle_id()))
                        .setHeader("Article Name")
                        .setFlexGrow(1);     // füllt Platz

                posGrid.addColumn(KommissionPosition::getAmount)
                        .setHeader("Menge")
                        .setAutoWidth(true);

                posGrid.setItems(service.getPositionenFürKommission(k));

                layout.add(posGrid);
                dialog.add(layout);

                dialog.getFooter().add(new Button("Schließen", ev -> dialog.close()));
                dialog.open();
            });

            return open;
        }).setHeader("Aktion");
        grid.setItems(service.getOffeneKommissionen());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);


        add(new H2("Offene Kommissionen"), grid);
    }
}