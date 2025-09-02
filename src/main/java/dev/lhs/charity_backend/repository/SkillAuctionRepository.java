package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.SkillAuction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillAuctionRepository extends JpaRepository<SkillAuction, Long> {
}
