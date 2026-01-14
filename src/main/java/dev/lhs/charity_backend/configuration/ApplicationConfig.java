package dev.lhs.charity_backend.configuration;

import dev.lhs.charity_backend.constant.PredefinedRole;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.entity.auth.Role;
import dev.lhs.charity_backend.repository.RoleRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {

    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver")

    ApplicationRunner applicationRunner (UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {

            // roles
            HashSet<Role> roles = new HashSet<>();

            if (!roleRepository.existsById(PredefinedRole.ADMIN_ROLE)) {

                // create admin role
                Role adminRole = Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build();
                roleRepository.save(adminRole);
                roles.add(adminRole);
            }

            if (!roleRepository.existsById(PredefinedRole.USER_ROLE)) {

                // create user role
                Role userRole = Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build();
                roleRepository.save(userRole);
                roles.add(userRole);
            }

            if (!userRepository.existsByUsername(ADMIN_USERNAME)) {

                // create Admin account
                User user = User.builder()
                        .username(ADMIN_USERNAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .dob(LocalDate.parse("1999-09-19"))
                        .email("admin@gmail.com")
                        .phoneNumber("0123456789")
                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
