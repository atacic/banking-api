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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.aleksa.banking_api.exception.ExceptionMessages.ROLE_NOT_FOUND;
import static com.aleksa.banking_api.security.SecurityConstants.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {

        Role role = roleRepository.findByRoleName(request.roleName())
                .orElse(roleRepository.findByRoleName(RoleName.ROLE_USER)
                        .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND)));

        User user = mapper.registerRequestToUser(request);
        user.setRoles(Collections.singleton(role));
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

        return mapper.userToRegisterResponse(user);
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = TOKEN_PREFIX + jwtTokenProvider.generateToken(authentication);

        return new LoginResponse(jwt, true);
    }
}
