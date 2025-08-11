package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.constant.PredefinedRole;
import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.response.UserResponse;
import dev.lhs.charity_backend.entity.User;
import dev.lhs.charity_backend.entity.auth.Role;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.mapper.UserMapper;
import dev.lhs.charity_backend.repository.RoleRepository;
import dev.lhs.charity_backend.repository.UserRepository;
import dev.lhs.charity_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // bcrypt encoding

        // set role for user
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        // save
        try {
            user = userRepository.save(user);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.ACCOUNT_INFO_EXISTED); // end and "return" exception
        }
        return userMapper.toUserCreationResponse(user);
    }
}
