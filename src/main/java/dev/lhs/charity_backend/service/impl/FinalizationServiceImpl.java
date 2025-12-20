package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.entity.Bid;
import dev.lhs.charity_backend.entity.SkillAuction;
import dev.lhs.charity_backend.entity.Transaction;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import dev.lhs.charity_backend.enumeration.TransactionStatus;
import dev.lhs.charity_backend.repository.BidRepository;
import dev.lhs.charity_backend.repository.SkillAuctionRepository;
import dev.lhs.charity_backend.repository.TransactionRepository;
import dev.lhs.charity_backend.service.AuctionStateCacheService;
import dev.lhs.charity_backend.service.FinalizationService;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation của FinalizationService
 * Xử lý kết thúc phiên đấu giá:
 * 1. Lock SkillAuction
 * 2. Kiểm tra status và endTime
 * 3. Xác định người thắng
 * 4. Tạo Transaction
 * 5. Update Redis
 * 6. Phát notification
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizationServiceImpl implements FinalizationService {

    private final SkillAuctionRepository skillAuctionRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final AuctionStateCacheService auctionStateCacheService;

    // scheduleFinalization được implement trong FinalizationProducer
    // Method này không cần implement ở đây

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void finalizeAuction(Long auctionId) {
        log.info("Starting finalization for auction: {}", auctionId);
        
        // Bước 1: Lock SkillAuction
        Optional<SkillAuction> auctionOpt = skillAuctionRepository.findByIdWithLock(auctionId);
        if (auctionOpt.isEmpty()) {
            log.warn("Auction not found during finalization: {}", auctionId);
            return;
        }
        
        SkillAuction auction = auctionOpt.get();
        LocalDateTime now = LocalDateTime.now();
        
        // Bước 2: Kiểm tra trạng thái (idempotent check)
        if (auction.getStatus() == AuctionStatus.COMPLETED) {
            log.info("Auction already finalized: {}", auctionId);
            return; // Đã finalize rồi, không làm gì
        }
        
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            log.warn("Auction is not ACTIVE, cannot finalize: auctionId={}, status={}", 
                    auctionId, auction.getStatus());
            return;
        }
        
        // Kiểm tra thực sự đã hết hạn
        if (now.isBefore(auction.getEndTime())) {
            log.warn("Auction not yet expired: auctionId={}, endTime={}, now={}", 
                    auctionId, auction.getEndTime(), now);
            return;
        }
        
        // Bước 3: Xác định người thắng
        Bid winnerBid = bidRepository.findFirstBySkillAuctionIdOrderByBidAmountDescBidTimeAsc(auctionId)
                .orElse(null);
        
        Long winnerId = null;
        BigDecimal finalBid = auction.getCurrentBid();
        
        if (winnerBid != null) {
            winnerId = winnerBid.getBidder().getId();
            finalBid = winnerBid.getBidAmount();
            log.info("Winner determined: auctionId={}, winnerId={}, finalBid={}", 
                    auctionId, winnerId, finalBid);
        } else {
            log.info("No bids found for auction: {}", auctionId);
        }
        
        // Bước 4: Cập nhật status
        auction.setStatus(AuctionStatus.COMPLETED);
        auction.setUpdatedAt(now);
        auction = skillAuctionRepository.save(auction);
        
        // Bước 5: Tạo Transaction nếu có người thắng
        if (winnerId != null) {
            Transaction transaction = Transaction.builder()
                    .amount(finalBid)
                    .status(TransactionStatus.PENDING)
                    .description("Giao dịch từ đấu giá kỹ năng #" + auctionId)
                    .winner(winnerBid.getBidder())
                    .skillAuction(auction)
                    .campaign(auction.getCampaign())
                    .build();
            transactionRepository.save(transaction);
            log.info("Transaction created: auctionId={}, winnerId={}, amount={}", 
                    auctionId, winnerId, finalBid);
        }
        
        // Bước 6: Update Redis
        auctionStateCacheService.finalizeAuctionState(auctionId, winnerId, finalBid);
        
        log.info("Auction finalized successfully: auctionId={}, winnerId={}, finalBid={}", 
                auctionId, winnerId, finalBid);
        
        // TODO: Publish notification event
        // publishFinalizationEvent(auctionId, winnerId, finalBid);
    }
}

