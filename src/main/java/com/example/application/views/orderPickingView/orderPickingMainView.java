package com.example.application.views.orderPickingView;

import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.KommissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
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


import java.util.Comparator;
import java.util.List;

@Route("order-picking")
@PageTitle("Order Picking")
@Menu(order = 6, icon = LineAwesomeIconUrl.TRUCK_SOLID)
@Uses(Icon.class)
public class orderPickingMainView extends VerticalLayout {

    private final KommissionService service;
    private final MessageLogisticRepository msgRepo;
    private final Grid<Kommission> grid = new Grid<>(Kommission.class, false);

    public orderPickingMainView(KommissionService service, MessageLogisticRepository msgRepo) {
        this.service = service;
        this.msgRepo = msgRepo;
        setSizeFull();

        // Grid-Spalten
        grid.addColumn(Kommission::getOrderPickingNumber).setHeader("Order Picking Nr.");
        grid.addColumn(Kommission::getDate).setHeader("Created");
        grid.addColumn(k -> k.getStoreId() != null ? k.getStoreId() : "").setHeader("Store");

        // Finished-Checkbox-Spalte
        grid.addComponentColumn(k -> {
            Checkbox cb = new Checkbox(k.getFinished());
            cb.addValueChangeListener(e -> {
                k.setFinished(e.getValue());
                service.save(k);

                // Liste neu laden, sortieren und ins Grid setzen
                List<Kommission> items = service.getAlleKommissionen();
                items.sort(Comparator.comparing(Kommission::getFinished));
                grid.setItems(items);
            });
            return cb;
        }).setHeader("Finished");
        grid.setClassNameGenerator(k -> {
            if (k.getFinished()) {
                // Inline CSS über setAttribute auf das Grid-Element
                return "finished-row";
            }
            return "";
        });
        getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.innerHTML = '.finished-row { opacity: 0.5; pointer-events: none; background-color: #e0e0e0; }';" +
                        "document.head.appendChild(style);"
        );
        List<Kommission> items = service.getAlleKommissionen();
        items.sort(Comparator.comparing(Kommission::getFinished));
        grid.setItems(items);

        // Details-Button
        grid.addComponentColumn(k -> {
            Button open = new Button("Open Details");

            open.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Order Picking " + k.getOrderPickingNumber());
                dialog.setWidth("900px");

                List<MessageLogistic> artikel = msgRepo.findUnderstocked(k.getStoreId());

                Grid<MessageLogistic> posGrid = new Grid<>(MessageLogistic.class, false);
                posGrid.setWidthFull();
                posGrid.setHeight("400px");

                posGrid.addColumn(MessageLogistic::getArticleNumber).setHeader("Artikel-Nr.").setAutoWidth(true);
                posGrid.addColumn(pos -> service.getArticleNameByNumber(pos.getArticleNumber()))
                        .setHeader("Artikel-Name")
                        .setFlexGrow(1);
                posGrid.addColumn(m -> m.getTargetStockLevel() - m.getStockLevel())
                        .setHeader("Nachzubestellende Menge").setAutoWidth(true);

                posGrid.setItems(artikel);

                VerticalLayout layout = new VerticalLayout(posGrid);
                layout.setWidthFull();
                layout.setPadding(false);
                layout.setSpacing(false);

                dialog.add(layout);
                dialog.getFooter().add(new Button("Schließen", ev -> dialog.close()));
                dialog.open();
            });

            return open;
        }).setHeader("Aktion");

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        add(new H2("Offene Kommissionen"), grid);
    }
}
