package com.saloria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

  @org.springframework.beans.factory.annotation.Value("${app.jwt.secret}")
  private String secretKey;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return username.equals(userDetails.getUsername())
        && userDetails.isEnabled()
        && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = resolveSecretBytes();
    if (keyBytes.length < 32) {
      throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes o ser un Base64 equivalente.");
    }
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private byte[] resolveSecretBytes() {
    if (secretKey == null || secretKey.isBlank()) {
      throw new IllegalStateException("JWT_SECRET no está configurado.");
    }

    String normalizedSecret = secretKey.trim();
    try {
      byte[] decoded = Decoders.BASE64.decode(normalizedSecret);
      if (decoded.length >= 32) {
        return decoded;
      }
    } catch (RuntimeException ignored) {
      // If it is not valid Base64 we treat it as a raw secret for local/dev setups.
    }

    return normalizedSecret.getBytes(StandardCharsets.UTF_8);
  }
}
