package de.fhdw.vendix.store.core.specification;

import de.fhdw.vendix.commons.api.domain.account.port.AccountQueryPort;
import de.fhdw.vendix.commons.api.domain.account_role.dto.AccountRoleEnum;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.api.specification.authentication.AuthenticationCommandApi;
import de.fhdw.vendix.commons.api.specification.authorization.AuthorizationQueryApi;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
class AuthorizationAdapter implements AuthorizationQueryApi, AuthenticationCommandApi {

    private final AccountQueryPort accountQueryPort;
    private final LockQueryPort lockQueryPort;

    AuthorizationAdapter(AccountQueryPort accountQueryPort, LockQueryPort lockQueryPort) {
        this.accountQueryPort = accountQueryPort;
        this.lockQueryPort = lockQueryPort;
    }

    @Override
    public Set<AccountRoleEnum> findRolesByAccountId(Long accountId) {
        return accountQueryPort.findAllRolesByAccount_Id(accountId);
    }

    @Override
    public boolean isAccountLocked(Long accountId) {
        return lockQueryPort.existsByTargetTypeAndTargetID(TargetTypeEnum.ACCOUNT, accountId);
    }
}