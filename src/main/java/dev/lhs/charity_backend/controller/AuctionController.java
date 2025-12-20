package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.BidRequest;
import dev.lhs.charity_backend.dto.request.SkillAuctionCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.BidResponse;
import dev.lhs.charity_backend.dto.response.SkillAuctionResponse;
import dev.lhs.charity_backend.entity.Bid;
import dev.lhs.charity_backend.entity.SkillAuction;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.repository.BidRepository;
import dev.lhs.charity_backend.repository.SkillAuctionRepository;
import dev.lhs.charity_backend.service.BidProducer;
import dev.lhs.charity_backend.service.SkillAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý auction và bid
 */
@Slf4j
@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final SkillAuctionService skillAuctionService;
    private final BidProducer bidProducer;
    private final BidRepository bidRepository;
    private final SkillAuctionRepository skillAuctionRepository;

    /**
     * Tạo phiên đấu giá mới
     */
    @PostMapping
    ApiResponse<SkillAuctionResponse> createAuction(@RequestBody SkillAuctionCreationRequest request) {
        return ApiResponse.<SkillAuctionResponse>builder()
                .result(skillAuctionService.createAuction(request))
                .build();
    }

    /**
     * Lấy thông tin phiên đấu giá
     */
    @GetMapping("/{auctionId}")
    ApiResponse<SkillAuctionResponse> getAuction(@PathVariable Long auctionId) {
        return ApiResponse.<SkillAuctionResponse>builder()
                .result(skillAuctionService.getAuction(auctionId))
                .build();
    }

    /**
     * List tất cả phiên đấu giá
     */
    @GetMapping
    ApiResponse<List<SkillAuctionResponse>> listAuctions() {
        return ApiResponse.<List<SkillAuctionResponse>>builder()
                .result(skillAuctionService.listAuctions())
                .build();
    }

    /**
     * List phiên đấu giá đang ACTIVE
     */
    @GetMapping("/active")
    ApiResponse<List<SkillAuctionResponse>> listActiveAuctions() {
        return ApiResponse.<List<SkillAuctionResponse>>builder()
                .result(skillAuctionService.listActiveAuctions())
                .build();
    }

    /**
     * List phiên đấu giá theo campaign
     */
    @GetMapping("/campaign/{campaignId}")
    ApiResponse<List<SkillAuctionResponse>> listAuctionsByCampaign(@PathVariable Long campaignId) {
        return ApiResponse.<List<SkillAuctionResponse>>builder()
                .result(skillAuctionService.listAuctionsByCampaign(campaignId))
                .build();
    }

    /**
     * Đặt giá cho phiên đấu giá
     * API trả về ngay với message "bid đã được ghi nhận, đang xử lý"
     */
    @PostMapping("/{auctionId}/bids")
    ApiResponse<String> placeBid(
            @PathVariable Long auctionId,
            @RequestParam Long bidderId,
            @RequestParam java.math.BigDecimal bidAmount) {
        
        // Validate cơ bản
        if (bidAmount == null || bidAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ApiResponse.<String>builder()
                    .result("Mức giá không hợp lệ")
                    .build();
        }
        
        // Tạo BidRequest
        BidRequest bidRequest = BidRequest.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .bidAmount(bidAmount)
                .clientTimestamp(System.currentTimeMillis())
                .build();
        
        // Đẩy vào queue
        bidProducer.sendBidRequest(bidRequest);
        
        log.info("Bid request queued: auctionId={}, bidderId={}, amount={}", 
                auctionId, bidderId, bidAmount);
        
        return ApiResponse.<String>builder()
                .result("Bid đã được ghi nhận, đang xử lý")
                .build();
    }

    /**
     * Kiểm tra xem user có phải là người đặt giá cao nhất trong auction không
     * Dùng để check kết quả bid sau khi đặt giá
     */
    @GetMapping("/{auctionId}/my-bid")
    ApiResponse<BidResponse> checkBidStatus(
            @PathVariable Long auctionId,
            @RequestParam Long bidderId) {
        
        SkillAuction auction = skillAuctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_EXISTED));
        
        // Tìm bid gần nhất của user trong auction này
        List<Bid> userBids = bidRepository.findLatestBidByAuctionAndBidder(auctionId, bidderId);
        
        if (userBids.isEmpty()) {
            return ApiResponse.<BidResponse>builder()
                    .result(BidResponse.builder()
                            .bidderId(bidderId)
                            .auctionId(auctionId)
                            .isHighestBid(false)
                            .build())
                    .build();
        }
        
        Bid latestBid = userBids.get(0);
        boolean isHighestBid = auction.getHighestBidderId() != null 
                && auction.getHighestBidderId().equals(bidderId);
        
        BidResponse response = BidResponse.builder()
                .id(latestBid.getId())
                .bidAmount(latestBid.getBidAmount())
                .bidTime(latestBid.getBidTime())
                .bidderId(bidderId)
                .auctionId(auctionId)
                .isHighestBid(isHighestBid)
                .build();
        
        return ApiResponse.<BidResponse>builder()
                .result(response)
                .build();
    }

    /**
     * Lấy danh sách tất cả bids của auction (sắp xếp theo giá cao nhất trước)
     */
    @GetMapping("/{auctionId}/bids")
    ApiResponse<List<BidResponse>> getAuctionBids(@PathVariable Long auctionId) {
        List<Bid> bids = bidRepository.findAllBidsByAuctionId(auctionId);
        
        SkillAuction auction = skillAuctionRepository.findById(auctionId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_EXISTED));
        
        Long highestBidderId = auction.getHighestBidderId();
        
        List<BidResponse> bidResponses = bids.stream()
                .map(bid -> BidResponse.builder()
                        .id(bid.getId())
                        .bidAmount(bid.getBidAmount())
                        .bidTime(bid.getBidTime())
                        .bidderId(bid.getBidder().getId())
                        .auctionId(auctionId)
                        .isHighestBid(highestBidderId != null && highestBidderId.equals(bid.getBidder().getId()))
                        .build())
                .collect(Collectors.toList());
        
        return ApiResponse.<List<BidResponse>>builder()
                .result(bidResponses)
                .build();
    }
}

