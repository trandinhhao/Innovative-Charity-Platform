package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.*;
import dev.lhs.charity_backend.dto.response.CampaignCommentResponse;
import dev.lhs.charity_backend.dto.response.CampaignResponse;
import dev.lhs.charity_backend.entity.*;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.CampaignCommentMapper;
import dev.lhs.charity_backend.mapper.CampaignContentBlockMapper;
import dev.lhs.charity_backend.mapper.CampaignMapper;
import dev.lhs.charity_backend.repository.*;
import dev.lhs.charity_backend.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final OrganizationRepository organizationRepository;
    private final CampaignContentBlockMapper campaignContentBlockMapper;
    private final CampaignContentBlockRepository campaignContentBlockRepository;
    private final CampaignCommentMapper campaignCommentMapper;
    private final UserRepository userRepository;
    private final CampaignCommentRepository campaignCommentRepository;

    @Override
    public CampaignResponse create(CampaignRequest request) {
        Campaign campaign = campaignMapper.toCampaign(request);
        Organization organization = organizationRepository.findOrganizationById(request.getOrgId());
        if (organization == null) throw new AppException(ErrorCode.ORGANIZATION_NOT_EXISTED);

        campaign.setOrganization(organization);
        campaign = campaignRepository.save(campaign);

        ArrayList<CampaignContentBlock> array = new ArrayList<>();
        if (request.getCampaignContentBlocks() != null) {
            for (CampaignContentBlockRequest block : request.getCampaignContentBlocks()) {
                CampaignContentBlock ccb = new CampaignContentBlock();
                ccb.setContentType(block.getContentType());
                ccb.setContent(block.getContent());
                ccb.setPosition(block.getPosition());
                ccb.setCampaign(campaign);
                ccb = campaignContentBlockRepository.save(ccb);
                array.add(ccb);
            }
        }

        campaign.setCampaignContentBlocks(array);
        return campaignMapper.toCampaignResponse(campaign);
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
    public CampaignResponse update(Long campId, CampaignUpdateRequest request) {
        Campaign campaign = campaignRepository.findCampaignById(campId);
        if (campaign == null) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);

        campaignMapper.updateCampaign(campaign, request);
        ArrayList<CampaignContentBlock> array = new ArrayList<>();
        for (CampaignContentBlockUpdateRequest block : request.getCampaignContentBlocks()) {
            CampaignContentBlock ccb = campaignContentBlockMapper.toCampaignContentBlock(block);
            ccb.setCampaign(campaign);
            array.add(ccb);
        }
        campaign.setCampaignContentBlocks(array);
        return campaignMapper.toCampaignResponse(campaignRepository.save(campaign));
    }

    @Override
    public CampaignCommentResponse createComment(Long campId, CampaignCommentRequest request) {
        Campaign campaign = campaignRepository.findById(campId)
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        CampaignComment campaignComment = campaignCommentMapper.toCampaignComment(request);
        campaignComment.setUser(user);
        campaignComment.setCampaign(campaign);

        return campaignCommentMapper
                .toCampaignCommentResponse(campaignCommentRepository.save(campaignComment));
    }

    @Override
    public List<CampaignCommentResponse> getCampaignComments(Long campId) {
        return campaignCommentRepository.findAllByCampaign_Id(campId).stream()
                .map(campaignCommentMapper::toCampaignCommentResponse).toList();
    }

    @Override
    public String deleteComment(Long commentId) {
        CampaignComment campaignComment = campaignCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        campaignCommentRepository.delete(campaignComment);
        return "Comment has been deleted";
    }

}
