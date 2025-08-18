package dev.lhs.charity_backend.dto.request;

import dev.lhs.charity_backend.validator.dob.DobConstraint;
import dev.lhs.charity_backend.validator.phonenumber.PhoneNumberConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(min = 8, message = "INVALID_PASSWORD")
    private String password;
    @DobConstraint(min = 16, message = "INVALID_DOB") // tren 16 tuoi
    private LocalDate dob;
    @Email(message = "INVALID_EMAIL")
    private String email;
    @PhoneNumberConstraint(length = 10, message = "INVALID_PHONENUMBER")
    private String phoneNumber;

    private String fullName;
    private String address;
    private String avatarUrl;

    private Set<String> roles = new HashSet<>();
}

