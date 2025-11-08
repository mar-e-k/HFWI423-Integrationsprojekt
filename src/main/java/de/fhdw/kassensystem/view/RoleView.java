package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.Account;
import de.fhdw.kassensystem.persistence.entity.AccountRole;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.service.AccountRoleService;
import de.fhdw.kassensystem.persistence.service.AccountService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.EntityNotFoundException;

@Route("/role")
@PageTitle("Roles View")
@RolesAllowed({AccountRoleEnum.ROLE_CASHIER, AccountRoleEnum.ROLE_ADMIN})
public class RoleView extends BaseView {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;

    public RoleView(AccountService accountService, AccountRoleService accountRoleService) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
    }

    @Override
    protected String setTopbarTitle() {
        return this.getClass().getSimpleName();
    }

    /**
     * Notiz an Rohid: hier sind zwei Test Buttons einfügt, die jeweils ein Account und eine AccountRole erstellen.
     * Dieses solltest du in der View verwalten. Es ist schon so eingestellt, dass wenne eine CRUD-Operation vorgenommen wird,
     * dies in der DB geloggt wird. Müllt bei mehreren ausführen die Tabelle zu, also vl. bereinigen am Ende.
     * Wir müssen ebenso die Passwörter und andere Account Info in der DB verschlüsseln.
     * Erstmal weggelassen, damit du das besser siehst.
     */
    @Override
    protected void init() {
        Button addAdmin = new Button("Add Test Admin");
        Button addAdminRole = new Button("Add Test Admin Role");
        addAdmin.addClickListener(e -> {
            AccountRole adminRole = accountRoleService.findByRole(AccountRoleEnum.ADMIN)
                    .orElseThrow(EntityNotFoundException::new);
            Account account = new  Account();
            account.setAccountRole(adminRole);
            account.setAccountId(1);
            account.setUsername("testAdmin");
            account.setPassword("testAdmin");
            accountService.create(account);
        });
        addAdminRole.addClickListener(e -> {
            accountRoleService.create(
                    new AccountRole(
                        null,
                        AccountRoleEnum.ADMIN
                    )
            );
        });
        add(addAdmin, addAdminRole);
    }
}