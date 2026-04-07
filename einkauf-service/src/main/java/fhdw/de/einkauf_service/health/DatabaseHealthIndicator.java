package fhdw.de.einkauf_service.health;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * Health Check für die Datenbankkonnektivität.
 * Wird vom Actuator exponiert unter /actuator/health.
 */
@Component
public class DatabaseHealthIndicator {

    private static final Logger logger = Logger.getLogger(DatabaseHealthIndicator.class.getName());
    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Überprüft die Datenbankkonnektivität.
     */
    public boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            logger.warning("Database connection check failed: " + e.getMessage());
            return false;
        }
    }
}

