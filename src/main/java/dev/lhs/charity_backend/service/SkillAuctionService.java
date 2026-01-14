package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.SkillAuctionCreationRequest;
import dev.lhs.charity_backend.dto.response.BidResponse;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SkillAuctionService {

    SkillAuctionResponse createSkillWithAuction(SkillAuctionCreationRequest request);
    SkillAuctionResponse getAuction(Long auctionId);
    List<SkillAuctionResponse> listAuctions();
    List<SkillAuctionResponse> listAuctionsByCampaign(Long campaignId);
    String placeBidForSkill(Long auctionId, BigDecimal bidAmount);
    BidResponse checkBidStatus(Long auctionId);
    List<SkillAuctionResponse> listActiveAuctions();
    List<BidResponse> getAuctionBids(Long auctionId);
    List<BidResponse> getMyBidHistory();

    /**
     * Tự động activate các auction PENDING đã đến thời gian bắt đầu
     * Được gọi bởi scheduler định kỳ
     */
    void activatePendingAuctions();

    /**
     * Tự động disable các auction ACTIVE đã quá thời gian kết thúc (endTime)
     * Được gọi bởi scheduler định kỳ
     */
    void disableExpiredAuctions();
}

