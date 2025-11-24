package dev.lhs.charity_backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubmitResponse {
    private String message;
    private String isMatch;
}
