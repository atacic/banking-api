package com.aleksa.banking_api.security;

import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.service.impl.UserSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.aleksa.banking_api.security.SecurityConstants.HEADER_STRING;
import static com.aleksa.banking_api.security.SecurityConstants.TOKEN_PREFIX;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSecurityService userSecurityService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            Optional<String> jwt = getJwtFromRequest(request);
            if (jwt.isPresent() && StringUtils.hasText(jwt.get()) && jwtTokenProvider.validateToken(jwt.get())) {
                Long userId = jwtTokenProvider.getUserIdFromJwt(jwt.get());
                User customerDetails = userSecurityService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(customerDetails, null, customerDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception exception) {
            logger.error("Could not set user authentication in security context", exception);
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX))
            return Optional.of(bearerToken.substring(TOKEN_PREFIX.length()));
        return Optional.empty();
    }
}
