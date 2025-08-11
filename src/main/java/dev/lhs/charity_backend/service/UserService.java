package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(UserCreationRequest request);

}
