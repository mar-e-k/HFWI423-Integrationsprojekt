package de.fhdw.vendix.store.ui.business;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.Role;
import de.fhdw.vendix.store.core.domain.voucher.Voucher;
import de.fhdw.vendix.store.core.domain.voucher.VoucherService;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "voucher", layout = StoreAppLayout.class)
@RolesAllowed(Role.Constants.ADMIN)
public class VoucherView extends VerticalLayout {

    private final VoucherService voucherService;

    public VoucherView(VoucherService voucherService) {
        this.voucherService = voucherService;
        add(initGrid());
    }

    private Grid<Voucher> initGrid() {
        Grid<Voucher> voucherGrid = new Grid<>(Voucher.class, false);
        voucherGrid.setHeightFull();
        voucherGrid.setWidthFull();
        voucherGrid.addColumn(Voucher::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        voucherGrid.addColumn(Voucher::getCode)
                .setHeader("Code")
                .setAutoWidth(true)
                .setSortable(true);
        voucherGrid.addColumn(Voucher::getExpiresAt)
                .setHeader("Expires at")
                .setAutoWidth(true)
                .setSortable(true);
        voucherGrid.addColumn(Voucher::getRedeemedAt)
                .setHeader("Redeemed at")
                .setAutoWidth(true)
                .setSortable(true);
        voucherGrid.setItems(voucherService.findAll());
        return voucherGrid;
    }
}