package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.CampaignRequest;
import dev.lhs.charity_backend.dto.request.OrganizationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.CampaignResponse;
import dev.lhs.charity_backend.entity.Campaign;
import dev.lhs.charity_backend.entity.Organization;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.CampaignMapper;
import dev.lhs.charity_backend.repository.CampaignRepository;
import dev.lhs.charity_backend.repository.OrganizationRepository;
import dev.lhs.charity_backend.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final OrganizationRepository organizationRepository;

    @Override
    public CampaignResponse create(CampaignRequest request) {
        Campaign campaign = campaignMapper.toCampaign(request);
        Organization organization = organizationRepository.findOrganizationById(request.getOrgId());
        if (organization == null) throw new AppException(ErrorCode.ORGANIZATION_NOT_EXISTED);

        campaign.setOrganization(organization);
        campaign.setCampaignContentBlocks(request.getCampaignContentBlocks());
        return campaignMapper.toCampaignResponse(campaignRepository.save(campaign));
    }

    @Override
    public List<CampaignResponse> getCampaigns() {
        return campaignRepository.findAll().stream()
                .map(campaignMapper::toCampaignResponse).toList();
    }

    @Override
    public CampaignResponse getCampaign(Long campId) {
        if (!campaignRepository.existsById(campId)) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);
        return campaignMapper.toCampaignResponse(campaignRepository.findCampaignById(campId));
    }

    @Override
    public String delete(Long campId) {
        if (!campaignRepository.existsById(campId)) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);
        campaignRepository.deleteById(campId);
        return "Campaign has been deleted";
    }

    @Override
    public CampaignResponse update(Long campId, CampaignRequest request) {
        Campaign campaign = campaignRepository.findCampaignById(campId);
        if (campaign == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        campaignMapper.updateCampaign(campaign, request);
        return campaignMapper.toCampaignResponse(campaignRepository.save(campaign));
    }

}
