package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.ContingentResponseDTO;
import fhdw.de.einkauf_service.service.ContingentService;
import fhdw.de.einkauf_service.service.ShoppingCartService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Route(value = "contingents", layout = MainLayout.class)
public class ContingentView extends VerticalLayout {

    private final ContingentService contingentService;
    private final ShoppingCartService shoppingCartService;

    private final Grid<ContingentResponseDTO> grid = new Grid<>(ContingentResponseDTO.class, false);

    public ContingentView(ContingentService contingentService,
                          ShoppingCartService shoppingCartService) {
        this.contingentService = contingentService;
        this.shoppingCartService = shoppingCartService;

        // Layout-Einstellungen
        setSizeFull();
        addClassName("page-view");

        H2 title = new H2("Aktive Kontingente");
        add(title);

        configureGrid();
        add(createToolbar());
        add(grid);
        setFlexGrow(1, grid);

        updateGrid();
    }

    // ========== GRID KONFIGURATION ==========
    private void configureGrid() {

        // 1. Spalten für die Anzeige
        grid.addColumn(ContingentResponseDTO::getArticleName)
                .setHeader("Artikel")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(ContingentResponseDTO::getSupplierName)
                .setHeader("Lieferant")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(ContingentResponseDTO::getAvailableQuantity)
                .setHeader("Verfügbar (Stk.)")
                .setSortable(true)
                .setKey("availableQuantity"); // Key für Sortierung

        grid.addColumn(ContingentResponseDTO::getOriginalOrderQuantity)
                .setHeader("Ursprüngliche Menge")
                .setSortable(true)
                .setKey("originalQuantity"); // Key für Sortierung

        grid.addColumn(ContingentResponseDTO::getOrderId)
                .setHeader("Bestell-ID")
                .setAutoWidth(true)
                .setSortable(true);

        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        // 2. Anzeige des Kontingent-Status als ProgressBar (Balken)
        grid.addColumn(new ComponentRenderer<>(this::createAvailabilityIndicator))
                .setHeader("Verfügbarkeit (%)")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setSizeFull();
    }

    // ========== VISUELLE HELFERMETHODE ==========
    private VerticalLayout createAvailabilityIndicator(ContingentResponseDTO dto) {
        double percentage = dto.getAvailabilityPercentage();
        double value = percentage / 100.0;

        ProgressBar bar = new ProgressBar(0, 1, value);
        bar.setWidth("120px");

        // Visuelle Priorisierung
        if (percentage < 25) {
            bar.getElement().getThemeList().add("error"); // Rot bei niedrigem Bestand
        } else if (percentage < 50) {
            bar.getElement().getThemeList().add("warning"); // Gelb
        } else {
            bar.getElement().getThemeList().add("success"); // Grün
        }

        String label = String.format("%.0f%%", percentage);

        VerticalLayout layout = new VerticalLayout(bar, new Span(label));
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(Alignment.START);
        return layout;
    }

    // ========== DATEN LADEN & ANZEIGEN ==========
    private void updateGrid() {

        // 1. Daten vom Service abrufen
        List<ContingentResponseDTO> immutableContingents = contingentService.getAllAvailableContingents();

        // 2. Eine neue, modifizierbare Kopie erstellen
        List<ContingentResponseDTO> contingents = new ArrayList<>(immutableContingents);

        // 3. Sortierung auf der modifizierbaren Kopie durchführen
        // (Sortierung aufsteigend nach dem Prozentsatz: niedrigster Bestand zuerst)
        contingents.sort(Comparator.comparing(ContingentResponseDTO::getAvailabilityPercentage));

        // 4. Grid mit der sortierten Liste befüllen
        grid.setItems(contingents);
    }

    private void processReorder(Set<ContingentResponseDTO> selectedContingents) {
        if (selectedContingents.isEmpty()) {
            return;
        }

        // Die ausgewählten Artikel mit der vollen ursprünglichen Menge in den Warenkorb legen
        selectedContingents.forEach(contingent -> {

            // Wir nehmen die ursprüngliche Menge als Basis für die Nachbestellung
            int quantityToReorder = contingent.getOriginalOrderQuantity();

            // Service-Methode aufrufen
            // Die Logik im Warenkorb-Service prüft, ob der Artikel bereits existiert und addiert ggf. die Menge.
            shoppingCartService.validateAndAddToCart(
                    contingent.getArticleId(),
                    quantityToReorder
            );
        });

        int articleCount = selectedContingents.size();

        // Feedback und Aufforderung zum Checkout-Screen
        Notification.show(articleCount + " Artikel dem Warenkorb hinzugefügt. Bitte zur Warenkorb-Ansicht wechseln, um die Bestellung abzuschließen.",
                        5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private HorizontalLayout createToolbar() {

        Button addToCartButton = new Button("Zum Warenkorb hinzufügen", VaadinIcon.CART_O.create());
        addToCartButton.setThemeName("primary");
        addToCartButton.setEnabled(false);

        Button deleteButton = new Button("Kontingent löschen", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setEnabled(false);

        grid.asMultiSelect().addValueChangeListener(event -> {
            boolean hasSelection = !event.getValue().isEmpty();
            addToCartButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        addToCartButton.addClickListener(event -> {
            Set<ContingentResponseDTO> selectedContingents = grid.asMultiSelect().getValue();
            processReorder(selectedContingents);
            grid.asMultiSelect().clear();
            updateGrid();
        });

        deleteButton.addClickListener(event -> {
            Set<ContingentResponseDTO> selected = grid.asMultiSelect().getValue();
            selected.forEach(contingent -> contingentService.deleteContingent(contingent.getId()));
            grid.asMultiSelect().clear();
            updateGrid();
            Notification n = Notification.show(
                    selected.size() + " Kontingent(e) gelöscht. Logistik wurde benachrichtigt.",
                    4000, Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        HorizontalLayout toolbar = new HorizontalLayout(addToCartButton, deleteButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        toolbar.addClassName("action-toolbar");
        return toolbar;
    }
}
