package de.fhdw.vendix.store.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    private final String writeUrl;
    private final String writeUsername;
    private final String writePassword;
    private final int    writeMaxPoolSize;
    private final int    writeMinIdle;
    private final long   writeConnectionTimeout;

    private final String readUrl;
    private final String readUsername;
    private final String readPassword;
    private final int    readMaxPoolSize;
    private final int    readMinIdle;

    public DataSourceConfig(
            @Value("${spring.datasource.url}")                              String writeUrl,
            @Value("${spring.datasource.username}")                         String writeUsername,
            @Value("${spring.datasource.password}")                         String writePassword,
            @Value("${spring.datasource.hikari.maximum-pool-size:30}")      int    writeMaxPoolSize,
            @Value("${spring.datasource.hikari.minimum-idle:10}")           int    writeMinIdle,
            @Value("${spring.datasource.hikari.connection-timeout:20000}")  long   writeConnectionTimeout,
            @Value("${spring.datasource.read.url:${spring.datasource.url}}")                  String readUrl,
            @Value("${spring.datasource.read.username:${spring.datasource.username}}")        String readUsername,
            @Value("${spring.datasource.read.password:${spring.datasource.password}}")        String readPassword,
            @Value("${spring.datasource.read.hikari.maximum-pool-size:15}") int    readMaxPoolSize,
            @Value("${spring.datasource.read.hikari.minimum-idle:5}")       int    readMinIdle
    ) {
        this.writeUrl               = writeUrl;
        this.writeUsername          = writeUsername;
        this.writePassword          = writePassword;
        this.writeMaxPoolSize       = writeMaxPoolSize;
        this.writeMinIdle           = writeMinIdle;
        this.writeConnectionTimeout = writeConnectionTimeout;
        this.readUrl                = readUrl;
        this.readUsername           = readUsername;
        this.readPassword           = readPassword;
        this.readMaxPoolSize        = readMaxPoolSize;
        this.readMinIdle            = readMinIdle;
    }

    @Bean("writeDataSource")
    public HikariDataSource writeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("vendix-write-pool");
        config.setJdbcUrl(writeUrl);
        config.setUsername(writeUsername);
        config.setPassword(writePassword);
        config.setMaximumPoolSize(writeMaxPoolSize);
        config.setMinimumIdle(writeMinIdle);
        config.setConnectionTimeout(writeConnectionTimeout);
        config.setAutoCommit(false);
        return new HikariDataSource(config);
    }

    @Bean("readDataSource")
    public HikariDataSource readDataSource() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("vendix-read-pool");
        config.setJdbcUrl(readUrl);
        config.setUsername(readUsername);
        config.setPassword(readPassword);
        config.setMaximumPoolSize(readMaxPoolSize);
        config.setMinimumIdle(readMinIdle);
        config.setAutoCommit(true);
        config.setReadOnly(true);
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        TransactionRoutingDataSource routing = new TransactionRoutingDataSource();
        routing.setTargetDataSources(Map.of(
                TransactionRoutingDataSource.WRITE, writeDataSource(),
                TransactionRoutingDataSource.READ,  readDataSource()
        ));
        routing.setDefaultTargetDataSource(writeDataSource());
        routing.afterPropertiesSet();
        return routing;
    }
}