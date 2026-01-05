package de.fhdw.fillialensystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "de.fhdw.fillialensystem",
        "de.fhdw.commons"
})
public class Filialsystem {
    public static void main(String[] args) {
        SpringApplication.run(Filialsystem.class, args);
    }
}