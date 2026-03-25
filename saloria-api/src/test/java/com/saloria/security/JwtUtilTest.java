package com.saloria.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.saloria.model.Role;
import com.saloria.model.User;

public class JwtUtilTest {

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secretKey",
        "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
  }

  @Test
  void tokenIsValidForEnabledUser() {
    User user = User.builder()
        .email("ana@example.com")
        .role(Role.ADMIN)
        .active(true)
        .archived(false)
        .build();

    String token = jwtUtil.generateToken(user);

    assertTrue(jwtUtil.isTokenValid(token, user));
  }

  @Test
  void tokenIsRejectedForInactiveOrArchivedUser() {
    User inactiveUser = User.builder()
        .email("inactive@example.com")
        .role(Role.ADMIN)
        .active(false)
        .archived(false)
        .build();
    User archivedUser = User.builder()
        .email("archived@example.com")
        .role(Role.ADMIN)
        .active(true)
        .archived(true)
        .build();

    String inactiveToken = jwtUtil.generateToken(inactiveUser);
    String archivedToken = jwtUtil.generateToken(archivedUser);

    assertFalse(jwtUtil.isTokenValid(inactiveToken, inactiveUser));
    assertFalse(jwtUtil.isTokenValid(archivedToken, archivedUser));
  }

  @Test
  void tokenSupportsPlainTextSecretsUsedInLocalDevelopment() {
    ReflectionTestUtils.setField(jwtUtil, "secretKey", "dev-only-change-this-secret-before-sharing");

    User user = User.builder()
        .email("plain@example.com")
        .role(Role.ADMIN)
        .active(true)
        .archived(false)
        .build();

    String token = jwtUtil.generateToken(user);

    assertTrue(jwtUtil.isTokenValid(token, user));
  }
}
