package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.SkillAuctionCreationRequest;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;

import java.util.List;

/**
 * Service quản lý phiên đấu giá kỹ năng
 */
public interface SkillAuctionService {
    
    /**
     * Tạo phiên đấu giá mới
     * Tự động schedule finalization khi tạo
     */
    SkillAuctionResponse createAuction(SkillAuctionCreationRequest request);
    
    /**
     * Lấy thông tin phiên đấu giá
     */
    SkillAuctionResponse getAuction(Long auctionId);
    
    /**
     * List tất cả phiên đấu giá
     */
    List<SkillAuctionResponse> listAuctions();
    
    /**
     * List phiên đấu giá theo campaign
     */
    List<SkillAuctionResponse> listAuctionsByCampaign(Long campaignId);
    
    /**
     * List phiên đấu giá đang ACTIVE
     */
    List<SkillAuctionResponse> listActiveAuctions();
    
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
    
    /**
     * Kiểm tra xem user có phải là người đặt giá cao nhất trong auction không
     * 
     * @param auctionId ID của auction
     * @param bidderId ID của user
     * @return true nếu user là highest bidder, false nếu không
     */
    boolean isHighestBidder(Long auctionId, Long bidderId);
}

