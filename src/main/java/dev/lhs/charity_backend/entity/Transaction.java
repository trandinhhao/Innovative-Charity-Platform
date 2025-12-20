package dev.lhs.charity_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.lhs.charity_backend.enumeration.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho giao dịch tài chính
 * Được tạo khi phiên đấu giá kết thúc và có người thắng
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    // n transactions - 1 user (người thắng đấu giá)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", nullable = false)
    @JsonIgnore
    private User winner;

    // n transactions - 1 skill_auction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_auction_id", nullable = false)
    @JsonIgnore
    private SkillAuction skillAuction;

    // n transactions - 1 campaign (chiến dịch nhận tiền)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    @JsonIgnore
    private Campaign campaign;
}

