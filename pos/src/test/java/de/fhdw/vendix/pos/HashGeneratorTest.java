package de.fhdw.vendix.pos;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGeneratorTest {

    @Test
    void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hash = encoder.encode("1234");
        System.out.println("==================================");
        System.out.println("BCrypt Hash für '1234': " + hash);
        System.out.println("==================================");
    }
}