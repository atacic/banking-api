package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {Role.class, Collectors.class})
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()))")
    RegisterResponse userToRegisterResponse(User user);

    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    User registerRequestToUser(RegisterRequest register);

    @Named("encodePassword")
    default String encodePassword(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
}
