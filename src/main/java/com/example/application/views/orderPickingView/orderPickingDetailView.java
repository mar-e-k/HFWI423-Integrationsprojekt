package com.example.application.views.orderPickingView;

import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionPosition;
import com.example.application.data.orderPicking.KommissionPositionRepository;
import com.example.application.data.orderPicking.KommissionRepository;
import com.example.application.services.KommissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Order Picking Detail")
@Route("order-picking-detail")
@Menu(order = 5, icon = LineAwesomeIconUrl.TRUCK_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)
public class orderPickingDetailView extends VerticalLayout{

    @Autowired
    private KommissionService service;
    @Autowired private KommissionRepository komRepo;
    @Autowired private KommissionPositionRepository posRepo;

    private Kommission kommission;
    private Grid<KommissionPosition> posGrid = new Grid<>(KommissionPosition.class, false);

    public void setParameter(BeforeEvent event, Long id) {
        kommission = komRepo.findById(id).orElse(null);
        if (kommission == null) { add(new Span("Nicht gefunden")); return; }
        buildLayout();
    }

    private void buildLayout() {
        add(new H2("Kommission " + kommission.getOrderPickingNumber()));
        posGrid.addColumn(p -> p.getArtikel().getName()).setHeader("Article Number");
        posGrid.addColumn(p -> p.getArtikel().getName()).setHeader("Article Name");
        posGrid.addColumn(KommissionPosition::getMenge).setHeader("Stückzahl");
        posGrid.addColumn(KommissionPosition::getLagerplatz).setHeader("Lagerplatz");

        // Button für Abweichung rechts
        posGrid.addComponentColumn(p -> {
            Button abw = new Button("Abweichung");
            abw.addClickListener(e -> openAbweichungsDialog(p));
            return abw;
        }).setHeader("");

        // Checkbox/Button um Position als bestätigt zu markieren
        posGrid.addComponentColumn(p -> {
            Button confirm = new Button("Bestätigen");
            confirm.addClickListener(e -> {
                //openBestätigungsDialog(p);
            });
            return confirm;
        });

        //List<KommissionPosition> positions = posRepo.findByKommissionIdOrderByPositionIndexAsc(kommission.getId());
       // posGrid.setItems(positions);

        // Erledigt-Button
        Button erledigtBtn = new Button("Kommission erledigen", e -> {
            //service.schließeKommission(kommission.getId());
            Notification.show("Kommission als erledigt markiert");
            // UI: graue Zeile / Hinweis
            getUI().ifPresent(ui -> ui.navigate(orderPickingMainView.class));
        });

        if (Boolean.TRUE.equals(kommission.getFinished())) {
            erledigtBtn.setEnabled(false);
            addClassName("erledigt"); // CSS: ausgegraut
        }

        add(posGrid, erledigtBtn);
    }

    private void openAbweichungsDialog(KommissionPosition pos) {
        Dialog d = new Dialog();
        NumberField geliefert = new NumberField("Gelieferte Menge");
        geliefert.setMin(0);
        TextArea grund = new TextArea("Grund");
        Button save = new Button("Speichern", e -> {
            int gemeldet = geliefert.getValue().intValue();
            // Ersteller: ggf. aktueller Benutzer
            //service.bestätigePosition(); // implementiere call to bestätigePosition
            d.close();
            Notification.show("Abweichung gespeichert");
            posGrid.getDataProvider().refreshAll();
        });
        d.add(new VerticalLayout(geliefert, grund, save));
        d.open();
    }
}