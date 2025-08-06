package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
