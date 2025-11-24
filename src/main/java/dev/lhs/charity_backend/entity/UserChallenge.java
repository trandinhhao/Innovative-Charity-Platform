package dev.lhs.charity_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
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

    // 1 user - n user_challenge
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // n user_challenge - 1 challenge
    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

}
