package dev.lhs.charity_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class) // de cho @CreatedDate hoat dong auto fill
@Table(name = "skill_auctions")
public class SkillAuction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal bidAmount;

    @Column(name = "bid_time", nullable = false, precision = 19, scale = 3)
    @CreatedDate
    private LocalDateTime bidTime;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    // 1 user - n skill_auctions
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // n skill_auctions - 1 skill
    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

}
