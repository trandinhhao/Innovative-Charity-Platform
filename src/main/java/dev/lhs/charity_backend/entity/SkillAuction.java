package dev.lhs.charity_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.lhs.charity_backend.enumeration.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "skill_auctions")
public class SkillAuction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "starting_bid", nullable = false, precision = 19, scale = 3)
    private BigDecimal startingBid;

    @Column(name = "current_bid", nullable = false, precision = 19, scale = 3)
    @Builder.Default
    private BigDecimal currentBid = BigDecimal.ZERO;

    @Column(name = "target_amount", precision = 19, scale = 3)
    private BigDecimal targetAmount; // Optional

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.PENDING;

    @Column(name = "status_code", nullable = false)
    @Builder.Default
    private Integer statusCode = AuctionStatus.PENDING.getCode();

    @Column(name = "highest_bidder_id")
    private Long highestBidderId;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // n skill_auctions - 1 skill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    @JsonIgnore
    private Skill skill;

    // n skill_auctions - 1 user(owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_owner_id", nullable = false)
    @JsonIgnore
    private User skillOwner;

    // n skill_auctions - 1 campaign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private Campaign campaign;

    // 1 skill_auction - n bids
    @OneToMany(mappedBy = "skillAuction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Bid> bids = new ArrayList<>();

    // 1 skill_auction - n transactions
    @OneToMany(mappedBy = "skillAuction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Transaction> transactions = new ArrayList<>();


    public void setStatus(AuctionStatus status) {
        this.status = status;
        this.statusCode = status.getCode();
    }
}
