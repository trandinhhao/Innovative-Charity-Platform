package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.request.BidRequest;
import dev.lhs.charity_backend.dto.request.SkillAuctionCreationRequest;
import dev.lhs.charity_backend.dto.response.BidResponse;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.entity.*;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.repository.*;
import dev.lhs.charity_backend.service.AuctionStateCacheService;
import dev.lhs.charity_backend.service.BidProducer;
import dev.lhs.charity_backend.service.FinalizationProducer;
import dev.lhs.charity_backend.service.SkillAuctionService;
import dev.lhs.charity_backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của SkillAuctionService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillAuctionServiceImpl implements SkillAuctionService {

    private final SkillAuctionRepository skillAuctionRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final FinalizationProducer finalizationProducer;
    private final AuctionStateCacheService auctionStateCacheService;

    private final BidProducer bidProducer;
    private final BidRepository bidRepository;

    @Override
    @Transactional
    public SkillAuctionResponse createSkillWithAuction(SkillAuctionCreationRequest request) {
        log.info("Creating new auction: skillId={}, skillOwnerId={}, campaignId={}", 
                request.getSkillId(), request.getSkillOwnerId(), request.getCampaignId());
        
        // Validate entities
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_EXISTED));
        
        User skillOwner = userRepository.findById(request.getSkillOwnerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOT_EXISTED));
        
        // Validate time
        LocalDateTime now = LocalDateTime.now();
        // Cho phép startTime trong quá khứ (sẽ set status = ACTIVE)
        if (request.getEndTime().isBefore(request.getStartTime())) {
            log.error("EndTime is before StartTime: startTime={}, endTime={}", request.getStartTime(), request.getEndTime());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // TODO: Add specific error
        }
        
        // Determine initial status: nếu startTime <= now thì ACTIVE, ngược lại PENDING
        AuctionStatus initialStatus = request.getStartTime().isAfter(now) 
                ? AuctionStatus.PENDING 
                : AuctionStatus.ACTIVE;
        
        // Create SkillAuction
        // currentBid = 0 khi chưa có ai đấu giá (sẽ được cập nhật khi có bid đầu tiên >= startingBid)
        SkillAuction auction = SkillAuction.builder()
                .skill(skill)
                .skillOwner(skillOwner)
                .campaign(campaign)
                .startingBid(request.getStartingBid())
                .currentBid(BigDecimal.ZERO) // Bắt đầu từ 0, chưa có ai đấu giá
                .targetAmount(request.getTargetAmount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(initialStatus)
                .bids(List.of())
                .transactions(List.of())
                .build();
        
        auction = skillAuctionRepository.save(auction);
        
        // Initialize cache với endTime
        // Cache currentBid = 0 vì chưa có bid nào
        auctionStateCacheService.initializeAuctionState(
                auction.getId(),
                BigDecimal.ZERO, // currentBid = 0 khi mới tạo
                auction.getEndTime(),
                auction.getStatus()
        );
        
        // Schedule finalization
        finalizationProducer.scheduleFinalization(auction.getId(), auction.getEndTime());
        
        log.info("Auction created successfully: auctionId={}, status={}", auction.getId(), auction.getStatus());
        
        return toResponse(auction);
    }

    @Override
    public SkillAuctionResponse getAuction(Long auctionId) {
        SkillAuction auction = skillAuctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_EXISTED));
        return toResponse(auction);
    }

    @Override
    public List<SkillAuctionResponse> listAuctions() {
        return skillAuctionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SkillAuctionResponse> listAuctionsByCampaign(Long campaignId) {
        return skillAuctionRepository.findByCampaignId(campaignId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String placeBidForSkill(Long auctionId, BigDecimal bidAmount) {

        if (bidAmount == null || bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.BID_MUST_BE_HIGHER);
        }

        // BidRequest
        BidRequest bidRequest = BidRequest.builder()
                .auctionId(auctionId)
                .bidderId(SecurityUtils.getUserId())
                .bidAmount(bidAmount)
                .clientTimestamp(System.currentTimeMillis())
                .build();

        // Đẩy vào queue
        bidProducer.sendBidRequest(bidRequest);

        log.info("Bid request queued: auctionId={}, bidderId={}, amount={}",
                auctionId, SecurityUtils.getUserId(), bidAmount);

        return "Bid đã được ghi nhận, đang xử lý";
    }

    @Override
    public BidResponse checkBidStatus(Long auctionId) {

        SkillAuction auction = skillAuctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_EXISTED));

        Long bidderId = SecurityUtils.getUserId();

        // Tìm bid gần nhất của user trong auction này
        List<Bid> userBids = bidRepository.findLatestBidByAuctionAndBidder(auctionId, bidderId);

        if (userBids.isEmpty()) {
            return BidResponse.builder()
                    .bidderId(bidderId)
                    .auctionId(auctionId)
                    .isHighestBid(false)
                    .build();
        }

        Bid latestBid = userBids.get(0);
        boolean isHighestBid = auction.getHighestBidderId() != null
                && auction.getHighestBidderId().equals(bidderId);

        return BidResponse.builder()
                .id(latestBid.getId())
                .bidAmount(latestBid.getBidAmount())
                .bidTime(latestBid.getBidTime())
                .bidderId(bidderId)
                .auctionId(auctionId)
                .isHighestBid(isHighestBid)
                .build();
    }

    @Override
    public List<SkillAuctionResponse> listActiveAuctions() {
        return skillAuctionRepository.findByStatus(AuctionStatus.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BidResponse> getAuctionBids(Long auctionId) {
        List<Bid> bids = bidRepository.findAllBidsByAuctionId(auctionId);

        SkillAuction auction = skillAuctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_EXISTED));

        Long highestBidderId = auction.getHighestBidderId();

        return bids.stream()
                .map(bid -> BidResponse.builder()
                        .id(bid.getId())
                        .bidAmount(bid.getBidAmount())
                        .bidTime(bid.getBidTime())
                        .bidderId(bid.getBidder().getId())
                        .auctionId(auctionId)
                        .isHighestBid(highestBidderId != null && highestBidderId.equals(bid.getBidder().getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BidResponse> getMyBidHistory() {

        Long userId = SecurityUtils.getUserId();

        List<Bid> userBids = bidRepository.findAllBidsByUserIdOrderByBidTimeDesc(userId);

        return userBids.stream()
                .map(bid -> {
                    SkillAuction auction = bid.getSkillAuction();
                    Long highestBidderId = auction != null ? auction.getHighestBidderId() : null;

                    return BidResponse.builder()
                            .id(bid.getId())
                            .bidAmount(bid.getBidAmount())
                            .bidTime(bid.getBidTime())
                            .bidderId(bid.getBidder().getId())
                            .bidderUsername(bid.getBidder().getUsername())
                            .auctionId(auction != null ? auction.getId() : null)
                            .isHighestBid(highestBidderId != null && highestBidderId.equals(userId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void activatePendingAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<SkillAuction> pendingAuctions = skillAuctionRepository.findPendingAuctionsReadyToActivate(
                AuctionStatus.PENDING, now);
        
        if (pendingAuctions.isEmpty()) {
            log.debug("No pending auctions ready to activate");
            return;
        }
        
        log.info("Activating {} pending auctions", pendingAuctions.size());
        
        for (SkillAuction auction : pendingAuctions) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auction.setUpdatedAt(now);
            skillAuctionRepository.save(auction);
            
            // Update cache status
            auctionStateCacheService.updateAuctionStatus(auction.getId(), AuctionStatus.ACTIVE);
            
            log.info("Activated auction: auctionId={}, startTime={}", auction.getId(), auction.getStartTime());
        }
    }

    @Override
    @Transactional
    public void disableExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<SkillAuction> expiredAuctions = skillAuctionRepository.findActiveAuctionsExpired(
                AuctionStatus.ACTIVE, now);
        
        if (expiredAuctions.isEmpty()) {
            log.debug("No expired auctions to disable");
            return;
        }
        
        log.info("Disabling {} expired auctions", expiredAuctions.size());
        
        for (SkillAuction auction : expiredAuctions) {
            // Chuyển sang COMPLETED khi đã hết thời gian
            auction.setStatus(AuctionStatus.COMPLETED);
            auction.setUpdatedAt(now);
            skillAuctionRepository.save(auction);
            
            // Update cache status
            Long winnerId = auction.getHighestBidderId();
            BigDecimal finalBid = auction.getCurrentBid();
            auctionStateCacheService.finalizeAuctionState(auction.getId(), winnerId, finalBid);
            
            log.info("Disabled expired auction: auctionId={}, endTime={}, winnerId={}, finalBid={}", 
                    auction.getId(), auction.getEndTime(), winnerId, finalBid);
        }
    }

    private SkillAuctionResponse toResponse(SkillAuction auction) {
        return SkillAuctionResponse.builder()
                .id(auction.getId())
                .startingBid(auction.getStartingBid())
                .currentBid(auction.getCurrentBid())
                .targetAmount(auction.getTargetAmount())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .statusCode(auction.getStatusCode())
                .highestBidderId(auction.getHighestBidderId())
                .skillId(auction.getSkill().getId())
                .skillOwnerId(auction.getSkillOwner().getId())
                .campaignId(auction.getCampaign().getId())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .build();
    }
}

