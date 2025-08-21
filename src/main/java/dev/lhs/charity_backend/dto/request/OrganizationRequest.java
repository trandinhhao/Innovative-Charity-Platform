package dev.lhs.charity_backend.dto.request;

import dev.lhs.charity_backend.validator.phonenumber.PhoneNumberConstraint;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {

    private String name;
    private String description;
    private String avatarUrl;
    @Email(message = "INVALID_EMAIL")
    private String email;
    @PhoneNumberConstraint(length = 10, message = "INVALID_PHONENUMBER")
    private String phoneNumber;
    private String address;

}
