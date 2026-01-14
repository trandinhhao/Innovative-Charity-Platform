package dev.lhs.charity_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.lhs.charity_backend.enumeration.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_challenges")
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proof_image_url", nullable = false)
    private String proofImageUrl;

    @Column(name = "submit_time", nullable = false)
    @CreatedDate
    private LocalDateTime submitTime;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "is_match", nullable = false)
    private Boolean isMatch;

    // Evidence Verification Pipeline
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "analysis_details", columnDefinition = "TEXT")
    private String analysisDetails; // JSON string containing detailed analysis

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "processed_at")
    @LastModifiedDate
    private LocalDateTime processedAt;

    // 1 user - n user_challenge
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // n user_challenge - 1 challenge
    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    @JsonIgnore
    private Challenge challenge;

}
