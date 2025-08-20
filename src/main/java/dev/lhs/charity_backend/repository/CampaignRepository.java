package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Campaign findCampaignById(Long id);
}
