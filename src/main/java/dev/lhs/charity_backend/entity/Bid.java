package dev.lhs.charity_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một lượt đặt giá (bid) trong phiên đấu giá
 * Audit trail cho tất cả các bid
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal bidAmount;

    @Column(name = "bid_time", nullable = false)
    @CreatedDate
    private LocalDateTime bidTime;

    @Column(name = "client_timestamp")
    private Long clientTimestamp; // Timestamp từ client để xử lý tie-breaker

    // n bids - 1 skill_auction (phiên đấu giá)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_auction_id", nullable = false)
    @JsonIgnore
    private SkillAuction skillAuction;

    // n bids - 1 user (người đặt giá)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User bidder;
}

