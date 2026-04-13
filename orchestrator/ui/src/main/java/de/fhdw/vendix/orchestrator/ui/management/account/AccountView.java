package de.fhdw.vendix.orchestrator.ui.management.account;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.account.AccountService;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "accounts", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
@PageTitle("Accounts")
public class AccountView extends VerticalLayout {

    private final AccountService accountService;

    public AccountView(AccountService accountService) {
        this.accountService = accountService;
    }


//    private final AccountService accountService;
//    private final PasswordEncoder passwordEncoder;
//
//    private final AccountGrid grid;
//    private final AuditingDetailsForm detailsForm;
//    private final MasterDetailLayout detailLayout;
//
//    public AccountView(AccountService accountService, PasswordEncoder passwordEncoder) {
//        this.accountService = accountService;
//        this.passwordEncoder = passwordEncoder;
//
//        grid = new AccountGrid(accountService);
//        detailsForm = new AuditingDetailsForm();
//        detailLayout = new MasterDetailLayout();
//
//        configureDetailLayout();
//
//        Crud<Account> crud = new Crud<>();
//
//        add(detailLayout);
//    }
//
//    private void configureDetailLayout() {
//        detailLayout.setSizeFull();
//        detailLayout.setDetailSize("30em");
//        detailLayout.setAnimationEnabled(true);
//        detailLayout.setMaster(grid);
//
//        grid.asSingleSelect().addValueChangeListener(event -> {
//            Account selectedAccount = event.getValue();
//            if (selectedAccount != null) {
//                detailsForm.setEntity(selectedAccount);
//                detailLayout.setDetail(detailsForm);
//            } else {
//                detailLayout.setDetail(null);
//            }
//        });
//
//        detailLayout.addBackdropClickListener(_ -> clearGridContext());
//        detailLayout.addDetailEscapePressListener(_ -> clearGridContext());
//        detailsForm.addCloseListener(_ -> clearGridContext());
//    }
//
//    private void clearGridContext() {
//        detailsForm.clear();
//        detailLayout.setDetail(null);
//        grid.deselectAll();
//    }
//
//    @Override
//    protected Dialog createEntityDialog() {
//        return new Dialog();
//    }
}