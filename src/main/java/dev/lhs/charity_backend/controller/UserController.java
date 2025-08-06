package dev.lhs.charity_backend.controller;

import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.UserCreationResponse;
import dev.lhs.charity_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    ApiResponse<UserCreationResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.createUser(request))
                .build();
    }
}
