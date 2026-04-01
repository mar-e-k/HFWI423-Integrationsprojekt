package de.fhdw.vendix.store.core.persistance.account_role.port;

import de.fhdw.vendix.commons.api.domain.account_role.AccountRoleDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

interface AccountRoleQueryService extends CrudQueryService<AccountRoleDTO, Long> {}