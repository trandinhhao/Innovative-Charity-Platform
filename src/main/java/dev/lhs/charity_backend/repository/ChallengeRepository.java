package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Challenge findChallengeById(Long id);
}
