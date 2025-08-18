package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.constant.PredefinedRole;
import dev.lhs.charity_backend.dto.request.UserCreationRequest;
import dev.lhs.charity_backend.dto.request.UserUpdateRequest;
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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

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
        return userMapper.toUserResponse(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Long id) {
        if (userRepository.existsById(id)) {
            return userMapper.toUserResponse(userRepository.findUserById(id));
        } else throw new AppException(ErrorCode.USER_NOT_EXISTED);
    }

    @Override
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getMyInfo() {
        SecurityContext context = SecurityContextHolder.getContext(); // lay thong tin login
        Authentication auth = context.getAuthentication(); // null neu chua login

        User user = userRepository.findByUsername(auth.getName());
        if (user != null) {
            return userMapper.toUserResponse(user);
        } else throw new AppException(ErrorCode.USER_NOT_EXISTED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return "User has been deleted";
        } else throw new AppException(ErrorCode.USER_NOT_EXISTED);
    }

    @Override
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

        userMapper.updateUser(user, request);
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }
}
