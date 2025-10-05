package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.CampaignRequest;
import dev.lhs.charity_backend.dto.request.CampaignUpdateRequest;
import dev.lhs.charity_backend.dto.response.CampaignResponse;

import java.util.List;

public interface CampaignService {
    CampaignResponse create(CampaignRequest request);
    List<CampaignResponse> getCampaigns();
    CampaignResponse getCampaign(Long campId);
    String delete (Long campId);
    CampaignResponse update (Long campId, CampaignUpdateRequest request);
}
