package com.aleksa.banking_api.security;

import com.aleksa.banking_api.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.aleksa.banking_api.security.SecurityConstants.EXPIRATION_TIME_IN_MINUTES;
import static com.aleksa.banking_api.security.SecurityConstants.SECRET;

@Slf4j
@Component
public class JwtTokenProvider {

    public String generateToken(Authentication authentication) {
        User customer = (User) authentication.getPrincipal();
        Date now = Date.from(Instant.now());
        Date expirationDate = generateExpirationDate();
        String userId = customer.getId().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("email", customer.getEmail());
        claims.put("fullName", customer.getFullName());

        return Jwts.builder()
                .setSubject(userId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException ex) {
            log.error("Invalid JWT signature or malformed token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.error("JWT exception while validating token: {}", ex.getMessage());
        }
        return false;
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.get("id").toString());
    }

    private Key getSigningKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    private Date generateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME_IN_MINUTES);
        return calendar.getTime();
    }
}
