package com.saloria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.saloria.repository.UserRepository;
import com.saloria.security.JwtUtil;

public class AuthenticationServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private EnterpriseService enterpriseService;
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
        enterpriseService,
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

    when(enterpriseService.createInitialEnterprise("Salon Norte", "lucia@example.com"))
        .thenThrow(new IllegalArgumentException(
            "Ya existe una empresa con ese nombre. Contacta con soporte si necesitas acceso."));

    IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
        () -> authenticationService.register(request));

    assertEquals("Ya existe una empresa con ese nombre. Contacta con soporte si necesitas acceso.", error.getMessage());
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

    Enterprise enterprise = Enterprise.builder().id(8L).name("Salon Nuevo").slug("salon-nuevo").build();
    User savedUser = User.builder()
        .id(14L)
        .name("Lucia")
        .email("lucia@example.com")
        .role(Role.ADMIN)
        .enterprise(enterprise)
        .build();

    when(enterpriseService.createInitialEnterprise("Salon Nuevo", "lucia@example.com")).thenReturn(enterprise);
    when(passwordEncoder.encode("secret123")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtUtil.generateToken(any(), any(User.class))).thenReturn("token");

    AuthResponse response = authenticationService.register(request);

    assertEquals("token", response.getToken());
    verify(enterpriseService).createInitialEnterprise("Salon Nuevo", "lucia@example.com");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void registerCreatesClientUserWithoutEnterprise() {
    RegisterRequest request = RegisterRequest.builder()
        .name("Ana")
        .email("ana@example.com")
        .password("secret123")
        .build();

    when(passwordEncoder.encode("secret123")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(21L);
      return user;
    });
    when(jwtUtil.generateToken(any(), any(User.class))).thenReturn("token");

    AuthResponse response = authenticationService.register(request);

    assertEquals("token", response.getToken());
    verify(enterpriseService, never()).createInitialEnterprise(any(), any());
    verify(userRepository).save(argThat(user -> user.getEnterprise() == null && user.getRole() == Role.CLIENTE));
  }
}
