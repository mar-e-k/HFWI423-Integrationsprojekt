package de.fhdw.fillialensystem.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.service.RegisterService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route("register")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class RegisterAddView extends AbstractMainView {

    private final RegisterService registerService;
    private final StoreService storeService;

    public RegisterAddView(RegisterService registerService, StoreService storeService) {
        this.registerService = registerService;
        this.storeService = storeService;
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button homeButton = new Button("Zum Home-Screen");
        homeButton.addClickListener(e -> UI.getCurrent().navigate(""));
        return new HorizontalLayout(homeButton);
    }

    @Override
    protected void init() {
        H2 title = new H2("Manage Registers");
        add(title);

        List<Store> allStores = storeService.findAll();

        ComboBox<Store> storeComboBox = new ComboBox<>("Select Store");
        storeComboBox.setItems(allStores);
        storeComboBox.setItemLabelGenerator(store -> store.getCity() + ", " + store.getStreet() + " " + store.getStreetNumber());

        Grid<Register> registerGrid = new Grid<>(Register.class, false);
        registerGrid.addColumn(reg -> reg.getId()).setHeader("ID").setAutoWidth(true);
        registerGrid.addColumn(reg -> reg.getStore().getCity() + ", " + reg.getStore().getStreet() + " " + reg.getStore().getStreetNumber())
                .setHeader("Store")
                .setAutoWidth(true);
        registerGrid.setWidthFull();

        List<Register> allRegisters = registerService.findAll();
        registerGrid.setItems(allRegisters);

        Button addRegisterButton = new Button("Add Register", click -> {
            Store selectedStore = storeComboBox.getValue();
            if (selectedStore == null) {
                Notification.show("Please select a store first", 3000, Notification.Position.MIDDLE);
                return;
            }

            Register newRegister = new Register();
            newRegister.setStore(selectedStore);

            registerService.save(newRegister);
            Notification.show("Register added for store " + selectedStore.getCity(), 3000, Notification.Position.MIDDLE);

            registerGrid.setItems(registerService.findAll());
        });

        HorizontalLayout addLayout = new HorizontalLayout(storeComboBox, addRegisterButton);
        addLayout.setAlignItems(Alignment.BASELINE);
        add(addLayout);

        add(registerGrid);
    }
}