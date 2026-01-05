package de.fhdw.kassensystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "de.fhdw.kassensystem",
        "de.fhdw.commons"
})
public class Kassensystem {
    public static void main(String[] args) {
        SpringApplication.run(Kassensystem.class, args);
    }
}