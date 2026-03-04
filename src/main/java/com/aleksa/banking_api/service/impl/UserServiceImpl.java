package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.dto.request.LoginRequest;
import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.LoginResponse;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.UserMapper;
import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.RoleName;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.RoleRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.security.JwtTokenProvider;
import com.aleksa.banking_api.service.UserService;
import com.aleksa.banking_api.dto.event.NotificationEvent;
import com.aleksa.banking_api.service.impl.notification.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

import static com.aleksa.banking_api.exception.ExceptionMessages.ROLE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {

        Role role = roleRepository.findByRoleName(request.roleName())
                .orElseGet(() -> roleRepository.findByRoleName(RoleName.ROLE_USER)
                        .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND)));

        User user = mapper.registerRequestToUser(request);
        user.setRoles(Collections.singleton(role));
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

        sendWelcomeNotification(user);

        return mapper.userToRegisterResponse(user);
    }

    private void sendWelcomeNotification(User user) {
        try {
            notificationProducer.sendEmailNotification(new NotificationEvent(
                    user.getEmail(),
                    "Welcome to Banking API!",
                    String.format("Hello %s, thank you for registering with us.", user.getFullName()),
                    NotificationEvent.EventType.USER_REGISTRATION,
                    Map.of("userId", user.getId())
            ));
        } catch (Exception e) {
            log.error("Failed to send welcome notification for user ID: {}", user.getId(), e);
        }
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new LoginResponse(jwt, true);
    }
}
