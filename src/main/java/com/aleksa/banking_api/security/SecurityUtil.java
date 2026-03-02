package com.aleksa.banking_api.security;

import com.aleksa.banking_api.exception.ForbiddenException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == "anonymousUser") {
            throw new ForbiddenException("Access denied ");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new NotFoundException(userDetails.getUsername()));
        }

        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
