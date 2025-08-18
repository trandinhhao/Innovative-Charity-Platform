package dev.lhs.charity_backend.service;

import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.request.UserUpdateRequest;
import dev.lhs.charity_backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreationRequest request);
    List<UserResponse> getUsers();
    UserResponse getUser(Long userId);
    UserResponse getMyInfo ();
    String deleteUser(Long userId);
    UserResponse updateUser(Long userId, UserUpdateRequest request);

}
