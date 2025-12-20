package dev.lhs.charity_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "campaign")
public class Campaign extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "current_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "goal_amount", nullable = false, precision = 19, scale = 3)
    private BigDecimal goalAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // 1 capmpaign - n content_block
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.REMOVE)
    private List<CampaignContentBlock> campaignContentBlocks = new ArrayList<>();

    // 1 campaign - n skill
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    // 1 campaign - n challenge
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Challenge> challenges = new ArrayList<>();

    // n campaign - 1 organization
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    // 1 campaign - n comment
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaignComment> campaignComments = new ArrayList<>();
}
