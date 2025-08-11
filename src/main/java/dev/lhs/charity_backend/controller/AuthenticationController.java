package dev.lhs.charity_backend.controller;

import com.nimbusds.jose.JOSEException;
import dev.lhs.charity_backend.dto.request.AuthenticationRequest;
import dev.lhs.charity_backend.dto.request.IntrospectRequest;
import dev.lhs.charity_backend.dto.request.LogoutRequest;
import dev.lhs.charity_backend.dto.request.RefreshTokenRequest;
import dev.lhs.charity_backend.dto.response.ApiResponse;
import dev.lhs.charity_backend.dto.response.AuthenticationResponse;
import dev.lhs.charity_backend.dto.response.IntrospectResponse;
import dev.lhs.charity_backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/token") // login
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticateUser(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }

    @DeleteMapping("/logout")
    ApiResponse<String> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        return ApiResponse.<String>builder()
                .result(authenticationService.logout(request))
                .build();
    }
}
