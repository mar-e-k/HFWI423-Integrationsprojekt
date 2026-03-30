package de.fhdw.vendix.store.utility.initializer;

import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.entity.AccountRole;
import de.fhdw.vendix.store.persistence.service.AccountRoleService;
import de.fhdw.vendix.store.persistence.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(3)
@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    private static final String DEFAULT_ADMIN_USERNAME = "A";
    private static final String DEFAULT_ADMIN_PASSWORD = "1234";

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;
    private final BCryptPasswordEncoder passwordEncoder;

    public DefaultAdminInitializer(AccountService accountService,
                                   AccountRoleService accountRoleService) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
        this.passwordEncoder = new BCryptPasswordEncoder(10);
    }

    @Override
    public void run(ApplicationArguments args) {

        // ADMIN-Rolle sicherstellen (falls noch nicht in der DB)
        AccountRole adminRole = accountRoleService
                .findByRole(AccountRoleEnum.ADMIN)
                .orElseGet(() -> {
                    log.atWarn().log("ADMIN-Rolle nicht gefunden — wird jetzt angelegt.");
                    return accountRoleService.create(new AccountRole(AccountRoleEnum.ADMIN));
                });

        // Prüfen ob bereits ein Admin-Account mit Username "A" existiert
        boolean adminExists = accountService
                .findByUsername(DEFAULT_ADMIN_USERNAME)
                .isPresent();

        if (adminExists) {
            log.atInfo().log("Default-Admin '{}' ist bereits vorhanden — kein Setup nötig.",
                    DEFAULT_ADMIN_USERNAME);
            return;
        }

        // Keinen Admin gefunden → Default-Admin anlegen
        Account defaultAdmin = new Account(
                adminRole,
                null,
                UUID.randomUUID().toString(),
                DEFAULT_ADMIN_USERNAME,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD)
        );

        accountService.create(defaultAdmin);

        log.atWarn().log("========================================================");
        log.atWarn().log("Default-Admin angelegt: Username='{}', Passwort='{}'",
                DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        log.atWarn().log("Bitte das Passwort nach dem ersten Login ändern!");
        log.atWarn().log("========================================================");
    }
}