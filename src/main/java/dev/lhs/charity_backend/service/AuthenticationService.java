package dev.lhs.charity_backend.service;

import com.nimbusds.jose.JOSEException;
import dev.lhs.charity_backend.dto.request.AuthenticationRequest;
import dev.lhs.charity_backend.dto.request.IntrospectRequest;
import dev.lhs.charity_backend.dto.request.LogoutRequest;
import dev.lhs.charity_backend.dto.request.RefreshTokenRequest;
import dev.lhs.charity_backend.dto.response.AuthenticationResponse;
import dev.lhs.charity_backend.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {

    AuthenticationResponse authenticateUser(AuthenticationRequest request);
    IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;
    String logout (LogoutRequest request) throws ParseException, JOSEException;
}
