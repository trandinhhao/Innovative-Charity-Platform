package dev.lhs.charity_backend.entity;

import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "challenges")
public class Challenge extends BaseEntity {

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "curent_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal currentAmount;

    @Column(name = "unit_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal unitAmount;

    @Column(name = "goal_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal goalAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // 1 challenge - n user_challenge
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChallenge> userChallenges = new ArrayList<>();

    // 1 user - n challenge
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // n challenge - 1 campaign
    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;
}
