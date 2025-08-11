package dev.lhs.charity_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // bat auditing
@SpringBootApplication
public class CharityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CharityBackendApplication.class, args);
    }

}
