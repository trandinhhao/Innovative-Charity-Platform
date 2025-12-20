package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.SkillAuction;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SkillAuctionRepository extends JpaRepository<SkillAuction, Long> {
    
    /**
     * Tìm SkillAuction với Pessimistic Lock (FOR UPDATE)
     * Dùng cho xử lý bid để tránh race condition
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sa FROM SkillAuction sa WHERE sa.id = :id")
    Optional<SkillAuction> findByIdWithLock(@Param("id") Long id);
    
    List<SkillAuction> findByStatus(AuctionStatus status);
    
    List<SkillAuction> findByStatusAndEndTimeBefore(AuctionStatus status, LocalDateTime endTime);
    
    List<SkillAuction> findByCampaignId(Long campaignId);
    
    /**
     * Tìm các auction PENDING đã đến thời gian bắt đầu (startTime <= now)
     * Dùng cho scheduler để tự động activate
     */
    @Query("SELECT sa FROM SkillAuction sa WHERE sa.status = :status AND sa.startTime <= :now")
    List<SkillAuction> findPendingAuctionsReadyToActivate(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);
    
    /**
     * Tìm các auction ACTIVE đã quá thời gian kết thúc (endTime < now)
     * Dùng cho scheduler để tự động disable/complete
     */
    @Query("SELECT sa FROM SkillAuction sa WHERE sa.status = :status AND sa.endTime < :now")
    List<SkillAuction> findActiveAuctionsExpired(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);
}
