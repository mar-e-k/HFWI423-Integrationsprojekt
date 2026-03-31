package de.fhdw.vendix.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PosApplication {
    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}