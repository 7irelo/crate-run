package com.variate.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(String username);
    String extractUsername(String token);

    boolean validateToken(String token, UserDetails userDetails);
}
