package com.example.fantasy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    // For demo purposes only; in production load from env/secret manager.
    private static final String SECRET_BASE64 = "YXZlcnktc2VjcmV0LXNlY3JldC1rZXktZm9yLWZhbmdhbWUvYnNrLXByb2plY3Q=";
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 6; // 6 hours

    private static Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}