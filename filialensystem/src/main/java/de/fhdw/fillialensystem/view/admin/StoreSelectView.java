package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkLock;
import de.fhdw.fillialensystem.persistence.service.StoreLinkLockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import de.fhdw.fillialensystem.utility.StoreClient;
import de.fhdw.fillialensystem.view.MainView;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route("select-store")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class StoreSelectView extends AbstractMainView implements BeforeEnterObserver {

    private final StoreService storeService;
    private final StoreLinkLockService storeLinkLockService;
    private final StoreClient storeClient;

    public StoreSelectView(StoreService storeService, StoreLinkLockService storeLinkLockService, StoreClient storeClient) {
        this.storeService = storeService;
        this.storeLinkLockService = storeLinkLockService;
        this.storeClient = storeClient;
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button homeButton = new Button("Zum Home-Screen");
        homeButton.addClickListener(e -> UI.getCurrent().navigate(""));
        return new HorizontalLayout(homeButton);
    }

    @Override
    protected void init() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Select Available Store");
        add(title);

        List<Store> allStores = storeService.findAll();

        List<Store> lockedStores = storeLinkLockService.findAll().stream()
                .map(StoreLinkLock::getStore)
                .toList();

        ComboBox<Store> storeComboBox = new ComboBox<>("Available Stores");
        storeComboBox.setItems(allStores);
        storeComboBox.setItemLabelGenerator(s -> s.getId().toString());

        storeComboBox.setRenderer(new ComponentRenderer<>(store -> {
            Span label = new Span(store.getId() + " (" + store.getCountry() + ")");
            if (lockedStores.contains(store)) {
                label.getStyle().set("color", "var(--lumo-disabled-text-color)");
                label.getElement().setAttribute("title", "Currently locked");
            }
            return label;
        }));

        storeComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null && lockedStores.contains(event.getValue())) {
                storeComboBox.clear();
            }
        });

        Button confirmButton = new Button("Confirm Selection", click -> {
            Store selected = storeComboBox.getValue();
            if (selected == null) {
                Notification.show("Please select an available store.", 3000, Notification.Position.MIDDLE);
                return;
            }
            storeClient.setStore(selected);
            UI.getCurrent().navigate(MainView.class);
        });

        HorizontalLayout layout = new HorizontalLayout(storeComboBox, confirmButton);
        layout.setAlignItems(Alignment.BASELINE);
        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        super.beforeEnter(beforeEnterEvent);
        if (storeClient.getStore() != null || storeClient.getStore().getId() != null || storeService.findById(storeClient.getStore().getId()).isPresent()) {
            beforeEnterEvent.rerouteTo(MainView.class);
        } else {
            Notification.show("Please select an available store.", 5000, Notification.Position.MIDDLE);
        }
    }
}