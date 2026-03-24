package com.saloria.service;

import com.saloria.dto.CreateUserRequest;
import com.saloria.dto.UpdateUserRequest;
import com.saloria.exception.ResourceNotFoundException;
import com.saloria.model.Enterprise;
import com.saloria.model.Role;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

import com.saloria.dto.UserResponse;
import com.saloria.model.User;
import com.saloria.repository.UserRepository;
import com.saloria.repository.AppointmentRepository;
import com.saloria.repository.CustomerRepository;
import com.saloria.repository.EnterpriseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final AppointmentRepository appointmentRepository;
  private final CustomerRepository customerRepository;
  private final EnterpriseRepository enterpriseRepository;
  private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  public List<UserResponse> getAllUsers(Long enterpriseId) {
    return userRepository.findByEnterpriseId(enterpriseId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public UserResponse createUser(CreateUserRequest request, Authentication authentication) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Ya existe un usuario con ese email");
    }

    User actor = getAuthenticatedUser(authentication);
    validateAssignableRole(actor, request.getRole());

    Enterprise enterprise = enterpriseRepository.findById(request.getEnterpriseId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

    User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .enterprise(enterprise)
        .phone(request.getPhone())
        .active(true)
        .build();

    User savedUser = userRepository.save(user);
    return mapToResponse(savedUser);
  }

  public User getUserById(Long id) {
    return userRepository.findById(id).orElse(null);
  }

  public UserResponse updateUser(Long id, UpdateUserRequest request, Authentication authentication) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    User actor = getAuthenticatedUser(authentication);
    validateAssignableRole(actor, request.getRole());

    userRepository.findByEmail(request.getEmail())
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(existing -> {
          throw new IllegalArgumentException("Ya existe un usuario con ese email");
        });

    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setRole(request.getRole());
    user.setPhone(request.getPhone());
    user.setActive(request.getActive() != null ? request.getActive() : user.getActive());

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    User updatedUser = userRepository.save(user);
    return mapToResponse(updatedUser);
  }

  private User getAuthenticatedUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User user)) {
      throw new AccessDeniedException("No se pudo determinar el usuario autenticado");
    }
    return user;
  }

  private void validateAssignableRole(User actor, Role requestedRole) {
    if (requestedRole == Role.CLIENTE) {
      throw new IllegalArgumentException("Este endpoint solo permite crear o editar personal interno");
    }
    if (requestedRole == Role.SUPER_ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
      throw new AccessDeniedException("No tienes permisos para asignar el rol SUPER_ADMIN");
    }
  }

  public List<UserResponse> getUsersByEnterpriseId(Long enterpriseId) {
    return userRepository.findByEnterpriseId(enterpriseId).stream()
        .filter(user -> user.getRole() != com.saloria.model.Role.CLIENTE)
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private UserResponse mapToResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .role(user.getRole())
        .enterpriseId(user.getEnterprise() != null ? user.getEnterprise().getId() : null)
        .phone(user.getPhone())
        .active(user.getActive() != null ? user.getActive() : true)
        .build();
  }

  @Transactional
  public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // 1. Delete appointments where user is the employee
    appointmentRepository.deleteByEmployeeId(id);

    // 2. Check if user is linked to a customer record
    customerRepository.findByUserId(id).ifPresent(customer -> {
      // Delete customer's appointments
      appointmentRepository.deleteByCustomerId(customer.getId());
      // Delete customer record
      customerRepository.delete(customer);
    });

    // 3. Delete user
    userRepository.delete(user);
  }
}
