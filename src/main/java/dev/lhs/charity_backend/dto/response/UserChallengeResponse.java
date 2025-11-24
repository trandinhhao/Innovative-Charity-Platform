package dev.lhs.charity_backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChallengeResponse {
    private Long id;
    private String proofImageUrl;
    private LocalDateTime submitTime;
    private String message;
    private Boolean isMatch;
//    private Integer status;
    private Long userId;
    private Long challengeId;
}
