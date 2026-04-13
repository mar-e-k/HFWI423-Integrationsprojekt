package de.fhdw.vendix.store.ui.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.StoreProxyService;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Route(value = "context", layout = StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
@StyleSheet(Aura.STYLESHEET)
public class StoreContextView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(StoreContextView.class);

    private final StoreProxyService storeProxyService;
    private final StoreContext storeContext;

    private final FlexLayout storeLayout = new FlexLayout();
    private final List<StoreDTO> inactiveStores = new ArrayList<>();
    private final List<StoreDTO> activeStores = new ArrayList<>();

    public StoreContextView(StoreProxyService storeProxyService, StoreContext storeContext) {
        this.storeProxyService = storeProxyService;
        this.storeContext = storeContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createFilterMethods();
        createStoreLayout();

        loadInactiveStores();
        loadActiveStores();
    }

    private void createFilterMethods() {
        HorizontalLayout topbar = new HorizontalLayout();
        topbar.setWidthFull();

        add(topbar);
    }

    private void createStoreLayout() {
        storeLayout.setWidthFull();
        storeLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        storeLayout.setJustifyContentMode(JustifyContentMode.START);

        add(storeLayout);
    }

    private void loadInactiveStores() {
        ResponseEntity<List<StoreDTO>> stores = storeProxyService.getUnlockedStores();
        if (stores.getStatusCode() == HttpStatus.OK && stores.getBody() != null) {
            inactiveStores.addAll(stores.getBody());
        }
        inactiveStores.forEach(store -> {
            Component storeCard = createStoreCard(store, false);
            storeLayout.add(storeCard);
        });
    }

    private void loadActiveStores() {
        ResponseEntity<List<StoreDTO>> stores = storeProxyService.getLockedStores();
        if (stores.getStatusCode() == HttpStatus.OK && stores.getBody() != null) {
            activeStores.addAll(stores.getBody());
        }
        activeStores.forEach(store -> {
            Component storeCard = createStoreCard(store, true);
            storeLayout.add(storeCard);
        });
    }

    private Component createStoreCard(StoreDTO store, boolean isActive) {
        Div card = new Div();

        card.setWidth("220px");
        card.setHeight("150px");
        card.getStyle()
                .set("border", "1px solid #ccc")
                .set("border-radius", "10px")
                .set("padding", "10px")
                .set("margin", "10px")
                .set("box-shadow", "2px 2px 6px rgba(0,0,0,0.1)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("justify-content", "space-between");

        if (isActive) {
            card.getStyle().set("background-color", "#f5f5f5");
            card.getStyle().set("color", "#999");
        } else {
            card.getStyle().set("background-color", "white");
        }

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        Span title = new Span(store.city() + ", " + store.street() + " " + store.streetNumber());
        Span country = new Span(store.country());

        content.add(title, country);

        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setWidthFull();
        buttonBar.setPadding(false);
        buttonBar.setSpacing(true);

        if (isActive) {
            Button locked = new Button("Active");
            locked.setEnabled(false);

            locked.getStyle()
                    .set("background-color", "#999")
                    .set("color", "white");

            buttonBar.add(locked);
            buttonBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        } else {
            Button activate = new Button("Activate");

            activate.addClickListener(_ -> handleStoreClickEvent(store));

            activate.getStyle()
                    .set("background-color", "#2e7d32")
                    .set("color", "white");

            buttonBar.add(activate);
            buttonBar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        }

        card.add(content, buttonBar);

        return card;
    }

    private void handleStoreClickEvent(StoreDTO store) {
        storeContext.setStore(store);
        UI.getCurrent().navigate(RootView.class);
    }
}