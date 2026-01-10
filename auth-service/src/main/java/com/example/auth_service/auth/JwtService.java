package com.example.auth_service.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.auth_service.user.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("roles", user.getRoles().stream().map(r -> r.getName().name()).toList())
                .setIssuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .compact();
    }

    public Claims parseToken(String token) {
        var parser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token);
        return parser.getBody();
    }

}
