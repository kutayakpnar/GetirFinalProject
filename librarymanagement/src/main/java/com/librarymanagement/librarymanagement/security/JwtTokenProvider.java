package com.librarymanagement.librarymanagement.security;

import com.librarymanagement.librarymanagement.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        logger.info("Initializing JWT token provider with secret key");
        // Use a consistent key based on the secret from application.properties
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.debug("JWT signing key initialized successfully");
    }

    public String generateToken(User user) {
        logger.debug("Generating JWT token for user: {}", user.getEmail());
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        
        logger.debug("JWT token generated successfully for user: {} with expiry: {}", 
                user.getEmail(), expiryDate);
        
        return token;
    }

    public String getUsernameFromToken(String token) {
        logger.trace("Extracting username from JWT token");
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        logger.trace("Username extracted from JWT token: {}", username);
        
        return username;
    }

    public boolean validateToken(String token) {
        logger.trace("Validating JWT token");
        
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.trace("JWT token validated successfully");
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            logger.error("JWT validation error: {}", ex.getMessage());
        }
        return false;
    }
} 