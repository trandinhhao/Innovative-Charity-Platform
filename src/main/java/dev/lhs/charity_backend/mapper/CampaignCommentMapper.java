package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.CampaignCommentRequest;
import dev.lhs.charity_backend.dto.response.CampaignCommentResponse;
import dev.lhs.charity_backend.entity.CampaignComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignCommentMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    CampaignComment toCampaignComment(CampaignCommentRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "campaignId", source = "campaign.id")
    CampaignCommentResponse toCampaignCommentResponse(CampaignComment campaignComment);
}
