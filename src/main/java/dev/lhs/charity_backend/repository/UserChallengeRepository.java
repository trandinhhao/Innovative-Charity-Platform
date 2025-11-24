package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
}
