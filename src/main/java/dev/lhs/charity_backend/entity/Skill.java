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
@Table(name = "skills")
public class Skill extends BaseEntity {

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "starting_bid", nullable = false, precision = 19, scale = 3)
    private BigDecimal startingBid;

    @Column(name = "curent_bid", nullable = false, precision = 19, scale = 3)
    private BigDecimal curentBid = BigDecimal.ZERO;

    @Column(name = "target_bid", nullable = false, precision = 19, scale = 3)
    private BigDecimal targetBid;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // skill - skill auc
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SkillAuction> skillAuctions = new ArrayList<>();

    // user - skill
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // n skill - 1 campaign
    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;
}
