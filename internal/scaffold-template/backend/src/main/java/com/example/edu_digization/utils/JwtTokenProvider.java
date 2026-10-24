package com.example.edu_digization.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${jwt.secret:abcdefghijklmnopqrstuvwxyz1234567890}") String secret,
                            @Value("${jwt.expiration-ms:7200000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // 旧方法保留兼容
    public String createToken(String username) {
        return createToken(username,null);
    }

    // 新增：携带角色存入JWT载荷
    public String createToken(String username,String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        if(role != null){
            claims.put("role",role); // ADMIN / USER 存入token
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // 新增：从token获取角色
    public String getRoleByToken(String token){
        Claims claims = parseToken(token);
        return claims.get("role",String.class);
    }

    public String getUsernameByToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("sub", String.class);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}