package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.dto.response.BidResult;
import dev.lhs.charity_backend.entity.Bid;
import dev.lhs.charity_backend.entity.SkillAuction;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.repository.BidRepository;
import dev.lhs.charity_backend.repository.SkillAuctionRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementation của BidService với Pessimistic Locking
 * Quy trình:
 * 1. Bắt đầu transaction với isolation level SERIALIZABLE hoặc REPEATABLE_READ
 * 2. Lock row SkillAuction với PESSIMISTIC_WRITE
 * 3. Validate các điều kiện
 * 4. Cập nhật SkillAuction và tạo Bid record
 * 5. Commit transaction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final SkillAuctionRepository skillAuctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BidResult placeBid(Long auctionId, Long bidderId, BigDecimal bidAmount, Long clientTimestamp) {
        log.info("Processing bid: auctionId={}, bidderId={}, amount={}", auctionId, bidderId, bidAmount);
        
        // Bước 1: Lock SkillAuction với PESSIMISTIC_WRITE
        SkillAuction auction = skillAuctionRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> {
                    log.warn("Auction not found: {}", auctionId);
                    return new AppException(ErrorCode.AUCTION_NOT_EXISTED);
                });

        // Bước 2: Validate các điều kiện
        LocalDateTime now = LocalDateTime.now();
        
        // 2.1: Kiểm tra status == ACTIVE
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            log.warn("Auction is not ACTIVE: auctionId={}, status={}", auctionId, auction.getStatus());
            return BidResult.builder()
                    .success(false)
                    .message("Phiên đấu giá không đang diễn ra. Trạng thái: " + auction.getStatus())
                    .currentBid(auction.getCurrentBid())
                    .highestBidderId(auction.getHighestBidderId())
                    .build();
        }
        
        // 2.2: Kiểm tra chưa hết hạn
        if (now.isAfter(auction.getEndTime())) {
            log.warn("Auction has expired: auctionId={}, endTime={}, now={}", auctionId, auction.getEndTime(), now);
            return BidResult.builder()
                    .success(false)
                    .message("Phiên đấu giá đã kết thúc")
                    .currentBid(auction.getCurrentBid())
                    .highestBidderId(auction.getHighestBidderId())
                    .build();
        }
        
        // 2.3: Kiểm tra bidAmount hợp lệ
        // Nếu currentBid = 0 (chưa có ai đấu giá): bidAmount phải >= startingBid
        // Nếu currentBid > 0 (đã có người đấu giá): bidAmount phải > currentBid
        BigDecimal currentBid = auction.getCurrentBid();
        BigDecimal startingBid = auction.getStartingBid();
        boolean isValidBid;
        String errorMessage;
        
        if (currentBid.compareTo(BigDecimal.ZERO) == 0) {
            // Chưa có ai đấu giá: phải đặt >= startingBid
            isValidBid = bidAmount.compareTo(startingBid) >= 0;
            errorMessage = isValidBid ? null : 
                String.format("Mức giá phải tối thiểu bằng giá khởi điểm: %s. Bạn đặt: %s", startingBid, bidAmount);
        } else {
            // Đã có người đấu giá: phải đặt > currentBid
            isValidBid = bidAmount.compareTo(currentBid) > 0;
            errorMessage = isValidBid ? null : 
                String.format("Mức giá phải cao hơn giá hiện tại: %s. Bạn đặt: %s", currentBid, bidAmount);
        }
        
        if (!isValidBid) {
            log.warn("Bid amount invalid: auctionId={}, bidAmount={}, currentBid={}, startingBid={}", 
                    auctionId, bidAmount, currentBid, startingBid);
            return BidResult.builder()
                    .success(false)
                    .message(errorMessage)
                    .currentBid(auction.getCurrentBid())
                    .highestBidderId(auction.getHighestBidderId())
                    .build();
        }
        
        // 2.4: (Optional) Kiểm tra không tự outbid chính mình
        if (auction.getHighestBidderId() != null && auction.getHighestBidderId().equals(bidderId)) {
            log.warn("User trying to outbid themselves: auctionId={}, bidderId={}", auctionId, bidderId);
            return BidResult.builder()
                    .success(false)
                    .message("Bạn đang là người đặt giá cao nhất")
                    .currentBid(auction.getCurrentBid())
                    .highestBidderId(auction.getHighestBidderId())
                    .build();
        }
        
        // Bước 3: Validate user tồn tại
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> {
                    log.error("Bidder not found: {}", bidderId);
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });
        
        // Bước 4: Cập nhật SkillAuction
        auction.setCurrentBid(bidAmount);
        auction.setHighestBidderId(bidderId);
        auction.setUpdatedAt(now);
        auction = skillAuctionRepository.save(auction);
        
        // Bước 5: Tạo Bid record (audit trail)
        Bid bid = Bid.builder()
                .skillAuction(auction)
                .bidder(bidder)
                .bidAmount(bidAmount)
                .clientTimestamp(clientTimestamp)
                .build();
        bid = bidRepository.save(bid);
        
        log.info("Bid placed successfully: auctionId={}, bidderId={}, amount={}, bidId={}", 
                auctionId, bidderId, bidAmount, bid.getId());
        
        return BidResult.builder()
                .success(true)
                .message("Đặt giá thành công")
                .currentBid(auction.getCurrentBid())
                .highestBidderId(auction.getHighestBidderId())
                .bidId(bid.getId())
                .build();
    }
}

