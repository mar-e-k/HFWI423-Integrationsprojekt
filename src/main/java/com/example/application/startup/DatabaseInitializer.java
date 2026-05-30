package com.example.application.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Legt Datenbank-Objekte an, die Hibernate nicht automatisch erstellt
 * (z.B. manuell genutzte Sequences per JDBC).
 * Läuft vor allen anderen ApplicationRunners (@Order(1)).
 */
@Component
@Order(1)
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        createSequenceIfNotExists("wareneingang", "goods_receipt_seq");
    }

    private void createSequenceIfNotExists(String schema, String sequenceName) {
        try {
            jdbc.execute("CREATE SEQUENCE IF NOT EXISTS " + schema + "." + sequenceName
                    + " START WITH 1 INCREMENT BY 1");
            logger.info("Sequence {}.{} sichergestellt.", schema, sequenceName);
        } catch (Exception e) {
            logger.warn("Sequence {}.{} konnte nicht angelegt werden: {}", schema, sequenceName, e.getMessage());
        }
    }
}
