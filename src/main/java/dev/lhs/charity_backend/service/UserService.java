package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.response.UserCreationResponse;

public interface UserService {

    UserCreationResponse createUser(UserCreationRequest request);

}
