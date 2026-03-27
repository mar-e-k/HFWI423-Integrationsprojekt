package de.fhdw.vendix.store.ui;

import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
@Route("")
public class StoreSelectorView {

}