package dev.lhs.charity_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campaign_comments")
public class CampaignComment extends BaseEntity {

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // n comments - 1 user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // n comments - 1 campaign
    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;
}
