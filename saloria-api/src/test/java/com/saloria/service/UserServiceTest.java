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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.saloria.dto.CreateUserRequest;
import com.saloria.dto.UserResponse;
import com.saloria.model.Enterprise;
import com.saloria.model.Role;
import com.saloria.model.User;
import com.saloria.repository.AppointmentRepository;
import com.saloria.repository.CustomerRepository;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.UserRepository;

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private AppointmentRepository appointmentRepository;
  @Mock
  private CustomerRepository customerRepository;
  @Mock
  private EnterpriseRepository enterpriseRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private Authentication authentication;

  private UserService userService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userService = new UserService(
        userRepository,
        appointmentRepository,
        customerRepository,
        enterpriseRepository,
        passwordEncoder);
  }

  @Test
  void createUserRejectsSuperAdminAssignmentForAdminActor() {
    User actor = User.builder().id(99L).role(Role.ADMIN).active(true).build();
    CreateUserRequest request = CreateUserRequest.builder()
        .name("Root Candidate")
        .email("root@example.com")
        .password("secret123")
        .role(Role.SUPER_ADMIN)
        .enterpriseId(1L)
        .build();

    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(actor);
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    assertThrows(AccessDeniedException.class, () -> userService.createUser(request, authentication));

    verify(enterpriseRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void createUserAllowsInternalRoleWithinEnterprise() {
    User actor = User.builder().id(99L).role(Role.ADMIN).active(true).build();
    Enterprise enterprise = Enterprise.builder().id(1L).name("Salon Norte").build();
    CreateUserRequest request = CreateUserRequest.builder()
        .name("Ana")
        .email("ana@example.com")
        .password("secret123")
        .role(Role.EMPLEADO)
        .enterpriseId(1L)
        .phone("600000000")
        .build();

    User savedUser = User.builder()
        .id(10L)
        .name("Ana")
        .email("ana@example.com")
        .role(Role.EMPLEADO)
        .enterprise(enterprise)
        .phone("600000000")
        .active(true)
        .build();

    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(actor);
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(enterpriseRepository.findById(1L)).thenReturn(Optional.of(enterprise));
    when(passwordEncoder.encode("secret123")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserResponse response = userService.createUser(request, authentication);

    assertEquals(Role.EMPLEADO, response.getRole());
    assertEquals(1L, response.getEnterpriseId());
    assertEquals("ana@example.com", response.getEmail());
  }
}
