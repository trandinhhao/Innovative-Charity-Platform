package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.dto.response.CampaignCommentResponse;
import dev.lhs.charity_backend.entity.CampaignComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CampaignCommentRepository extends JpaRepository<CampaignComment, Long> {
    List<CampaignComment> findAllByCampaign_Id(Long campaignId);
}
