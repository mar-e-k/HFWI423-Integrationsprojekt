package de.fhdw.vendix.store.ui.business;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.core.printer.api.ReceiptPrinter;
import de.fhdw.vendix.commons.core.printer.api.ReceiptType;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "receipt", layout = StoreAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class ReceiptView extends VerticalLayout {

    private final ReceiptMapper receiptMapper;

    public ReceiptView(ReceiptMapper receiptMapper) {
        this.receiptMapper = receiptMapper;
        add(initGrid());
    }

    private Grid<Receipt> initGrid() {
        Grid<Receipt> receiptGrid = new Grid<>(Receipt.class, false);
        receiptGrid.setHeightFull();
        receiptGrid.setWidthFull();
        receiptGrid.addColumn(Receipt::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(Receipt::getStore)
                .setHeader("Store")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(Receipt::getRegister)
                .setHeader("Register")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(Receipt::getCashierId)
                .setHeader("Cashier")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(Receipt::getCreatedAt)
                .setHeader("Created")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addColumn(Receipt::getTotalPrice)
                .setHeader("Total")
                .setAutoWidth(true)
                .setSortable(true);
        receiptGrid.addComponentColumn(this::createPrintReceiptButton)
                .setHeader("Print")
                .setAutoWidth(true);
        return receiptGrid;
    }

    private Button createPrintReceiptButton(Receipt receipt) {
        Button printReceiptButton = new Button("Print");
        printReceiptButton.addClickListener(_ -> {
            ReceiptPrinter.print(receiptMapper.toDTO(receipt), ReceiptType.STANDARD);
        });
        printReceiptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return printReceiptButton;
    }
}