package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    
    List<Bid> findBySkillAuctionIdOrderByBidTimeAsc(Long skillAuctionId);
    
    @Query("SELECT b FROM Bid b WHERE b.skillAuction.id = :auctionId ORDER BY b.bidAmount DESC, b.bidTime ASC")
    List<Bid> findHighestBidsByAuctionId(@Param("auctionId") Long auctionId);
    
    Optional<Bid> findFirstBySkillAuctionIdOrderByBidAmountDescBidTimeAsc(Long skillAuctionId);
    
    /**
     * Tìm bid gần nhất của user trong auction
     */
    @Query("SELECT b FROM Bid b WHERE b.skillAuction.id = :auctionId AND b.bidder.id = :bidderId ORDER BY b.bidTime DESC")
    List<Bid> findLatestBidByAuctionAndBidder(@Param("auctionId") Long auctionId, @Param("bidderId") Long bidderId);
    
    /**
     * Tìm tất cả bids của auction, sắp xếp theo bidAmount DESC, bidTime ASC
     */
    @Query("SELECT b FROM Bid b WHERE b.skillAuction.id = :auctionId ORDER BY b.bidAmount DESC, b.bidTime ASC")
    List<Bid> findAllBidsByAuctionId(@Param("auctionId") Long auctionId);
    
    /**
     * Tìm tất cả bids của một user, sắp xếp theo bidTime DESC (mới nhất trước)
     */
    @Query("SELECT b FROM Bid b WHERE b.bidder.id = :userId ORDER BY b.bidTime DESC")
    List<Bid> findAllBidsByUserIdOrderByBidTimeDesc(@Param("userId") Long userId);
}

