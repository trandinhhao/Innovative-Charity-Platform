package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.auth.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
