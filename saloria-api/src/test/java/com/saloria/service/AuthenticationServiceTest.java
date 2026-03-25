package com.saloria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.saloria.dto.AuthResponse;
import com.saloria.dto.RegisterRequest;
import com.saloria.model.Enterprise;
import com.saloria.model.Role;
import com.saloria.model.User;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.UserRepository;
import com.saloria.security.JwtUtil;

public class AuthenticationServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private EnterpriseRepository enterpriseRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtUtil jwtUtil;
  @Mock
  private AuthenticationManager authenticationManager;

  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    authenticationService = new AuthenticationService(
        userRepository,
        enterpriseRepository,
        passwordEncoder,
        jwtUtil,
        authenticationManager);
  }

  @Test
  void registerRejectsProfessionalSignupWhenEnterpriseAlreadyExists() {
    RegisterRequest request = RegisterRequest.builder()
        .name("Lucia")
        .email("lucia@example.com")
        .password("secret123")
        .enterpriseName("Salon Norte")
        .build();

    when(enterpriseRepository.findByName("Salon Norte"))
        .thenReturn(Optional.of(Enterprise.builder().id(3L).name("Salon Norte").build()));

    IllegalStateException error = assertThrows(IllegalStateException.class, () -> authenticationService.register(request));

    assertEquals("Ya existe una empresa con ese nombre. Contacta con soporte si necesitas acceso.", error.getMessage());
    verify(enterpriseRepository, never()).save(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void registerCreatesNewEnterpriseForProfessionalSignup() {
    RegisterRequest request = RegisterRequest.builder()
        .name("Lucia")
        .email("lucia@example.com")
        .password("secret123")
        .enterpriseName("Salon Nuevo")
        .build();

    Enterprise enterprise = Enterprise.builder().id(8L).name("Salon Nuevo").build();
    User savedUser = User.builder()
        .id(14L)
        .name("Lucia")
        .email("lucia@example.com")
        .role(Role.ADMIN)
        .enterprise(enterprise)
        .build();

    when(enterpriseRepository.findByName("Salon Nuevo")).thenReturn(Optional.empty());
    when(enterpriseRepository.save(any(Enterprise.class))).thenReturn(enterprise);
    when(passwordEncoder.encode("secret123")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtUtil.generateToken(any(), any(User.class))).thenReturn("token");

    AuthResponse response = authenticationService.register(request);

    assertEquals("token", response.getToken());
    verify(enterpriseRepository).save(any(Enterprise.class));
    verify(userRepository).save(any(User.class));
  }
}
