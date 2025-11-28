package de.fhdw.fillialensystem.utility.initializer;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class StoreInitializer implements ApplicationRunner {

    public StoreInitializer() {
    }

    @Override
    public void run(ApplicationArguments args) {

    }
}
