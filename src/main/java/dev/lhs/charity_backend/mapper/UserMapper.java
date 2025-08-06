package dev.lhs.charity_backend.mapper;

import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.response.UserCreationResponse;
import dev.lhs.charity_backend.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser (UserCreationRequest request);
    UserCreationResponse toUserCreationResponse (User user);
}
