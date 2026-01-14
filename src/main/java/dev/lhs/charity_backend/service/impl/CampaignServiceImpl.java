package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.*;
import dev.lhs.charity_backend.dto.response.CampaignCommentResponse;
import dev.lhs.charity_backend.dto.response.CampaignResponse;
import dev.lhs.charity_backend.entity.*;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.enumeration.VerificationStatus;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.CampaignCommentMapper;
import dev.lhs.charity_backend.mapper.CampaignContentBlockMapper;
import dev.lhs.charity_backend.mapper.CampaignMapper;
import dev.lhs.charity_backend.repository.*;
import dev.lhs.charity_backend.service.CampaignService;
import dev.lhs.charity_backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                .map(campaign -> {
                    CampaignResponse response = campaignMapper.toCampaignResponse(campaign);
                    calculateRaisedAmount(response, campaign);
                    return response;
                })
                .toList();
    }

    @Override
    public CampaignResponse getCampaign(Long campId) {
        if (!campaignRepository.existsById(campId)) throw new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED);
        Campaign campaign = campaignRepository.findCampaignById(campId);
        CampaignResponse response = campaignMapper.toCampaignResponse(campaign);
        calculateRaisedAmount(response, campaign);
        return response;
    }

    /**
     * Tính raisedAmount của campaign dựa trên:
     * - Tổng currentAmount của các challenges (đã được APPROVED)
     * - Tổng curentBid của các skills
     */
    private void calculateRaisedAmount(CampaignResponse response, Campaign campaign) {
        BigDecimal totalRaised = BigDecimal.ZERO;

        // Tính tổng từ challenges: currentAmount của mỗi challenge
        if (campaign.getChallenges() != null) {
            for (Challenge challenge : campaign.getChallenges()) {
                if (challenge.getCurrentAmount() != null) {
                    // Tính lại currentAmount từ userChallenges APPROVED
                    BigDecimal challengeCurrentAmount = BigDecimal.ZERO;
                    if (challenge.getUserChallenges() != null && challenge.getUnitAmount() != null) {
                        long approvedCount = challenge.getUserChallenges().stream()
                                .filter(uc -> uc.getVerificationStatus() == VerificationStatus.APPROVED)
                                .count();
                        challengeCurrentAmount = challenge.getUnitAmount().multiply(BigDecimal.valueOf(approvedCount));
                    }
                    totalRaised = totalRaised.add(challengeCurrentAmount);
                }
            }
        }

        // Tính tổng từ skills: curentBid của mỗi skill
        // Lưu ý: skill.curentBid được lưu theo đơn vị nghìn (ví dụ: 500 = 500.000 VND)
        // Nên cần nhân với 1000 để đúng với đơn vị VND thực tế
        if (campaign.getSkills() != null) {
            for (Skill skill : campaign.getSkills()) {
                if (skill.getCurentBid() != null) {
                    BigDecimal skillAmount = skill.getCurentBid().multiply(BigDecimal.valueOf(1000));
                    totalRaised = totalRaised.add(skillAmount);
                }
            }
        }

        response.setRaisedAmount(totalRaised);
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

        Long userId = SecurityUtils.getUserId();
        if (userId == null) throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);

        User user = userRepository.findById(userId)
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
