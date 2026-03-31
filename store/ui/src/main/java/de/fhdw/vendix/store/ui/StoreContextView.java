package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.ClickEvent;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.aura.Aura;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.security.api.context.AppContext;
import de.fhdw.vendix.security.api.context.StoreContext;
import de.fhdw.vendix.store.core.persistance.lock.port.LockService;
import de.fhdw.vendix.store.core.persistance.store.port.StoreService;
import jakarta.annotation.security.RolesAllowed;
import org.hibernate.query.common.TemporalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RolesAllowed(AccountRole.ROLE_ADMIN)
@Route("context")
@StyleSheet(Aura.STYLESHEET)
public class StoreContextView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(StoreContextView.class);

    private final StoreService storeService;
    private final StoreContext storeContext;
    private final AppContext appContext;
    private final LockService lockService;

    private final FlexLayout storeLayout = new FlexLayout();
    private final List<StoreDTO> inactiveStores = new ArrayList<>();
    private final List<StoreDTO> activeStores = new ArrayList<>();

    public StoreContextView(StoreService storeService, StoreContext storeContext, AppContext appContext, LockService lockService) {
        this.storeService = storeService;
        this.storeContext = storeContext;
        this.appContext = appContext;
        this.lockService = lockService;

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
        log.atInfo().log("Inactive stores: {}", storeService.getAllInactiveActiveStores().toString());
        inactiveStores.addAll(storeService.getAllInactiveActiveStores());
        inactiveStores.forEach(store -> {
            Component storeCard = createStoreCard(store, false);
            storeLayout.add(storeCard);
        });
    }

    private void loadActiveStores() {
        log.atInfo().log("Active stores: {}", storeService.getAllActiveStores().toString());
        activeStores.addAll(storeService.getAllActiveStores());
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

            activate.addClickListener(e -> handleStoreClickEvent(store));

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
        log.atInfo().log("Setting Store Context: {}", store);

        storeContext.setStore(store);

        LockDTO lock = new LockDTO(
                null,
                TargetType.STORE,
                Objects.requireNonNull(store.id(), "Store id cannot be null when setting context"),
                appContext.getInstanceUUID(),
                Instant.now(),
                Instant.now().plusSeconds(60 * 60 * 24)
        );
        lockService.create(lock);

        UI.getCurrent().navigate(RootView.class);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (storeContext.getStore() != null) {
            log.atWarn().log("Store context is already set and cannot be set again. Reset the application if required");
            event.rerouteTo(RootView.class);
        }
    }
}