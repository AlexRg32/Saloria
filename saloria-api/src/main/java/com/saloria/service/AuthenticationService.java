package com.saloria.service;

import com.saloria.dto.AuthRequest;
import com.saloria.dto.AuthResponse;
import com.saloria.dto.RegisterRequest;
import com.saloria.model.Enterprise;
import com.saloria.model.Role;
import com.saloria.model.User;
import com.saloria.repository.UserRepository;
import com.saloria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final EnterpriseService enterpriseService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    Enterprise enterprise = null;
    Role role = Role.CLIENTE;

    // Si hay nombre de empresa, es un registro Profesional (ADMIN)
    if (request.getEnterpriseName() != null && !request.getEnterpriseName().trim().isEmpty()) {
      enterprise = enterpriseService.createInitialEnterprise(request.getEnterpriseName(), request.getEmail());
      role = Role.ADMIN;
    }

    var user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(role)
        .enterprise(enterprise)
        .build();
    repository.save(user);

    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("userId", user.getId());
    extraClaims.put("name", user.getName());
    if (enterprise != null) {
      extraClaims.put("enterpriseName", enterprise.getName());
      extraClaims.put("enterpriseId", enterprise.getId());
      extraClaims.put("enterpriseSlug", enterprise.getSlug());
      extraClaims.put("primaryColor", enterprise.getPrimaryColor());
      extraClaims.put("secondaryColor", enterprise.getSecondaryColor());
    }
    extraClaims.put("role", user.getRole().name());

    var jwtToken = jwtService.generateToken(extraClaims, user);
    return AuthResponse.builder()
        .token(jwtToken)
        .build();
  }

  public AuthResponse authenticate(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()));
    var user = repository.findByEmailAndArchivedFalse(request.getEmail())
        .orElseThrow();

    // Validar que el rol sea acorde al portal (requiredRole)
    if (request.getRequiredRole() != null) {
      boolean isValid = false;
      if (request.getRequiredRole().equalsIgnoreCase("ADMIN")) {
        // Portal profesional acepta ADMIN, SUPER_ADMIN y EMPLEADO
        isValid = user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.EMPLEADO;
      } else if (request.getRequiredRole().equalsIgnoreCase("CLIENTE")) {
        // Portal cliente solo acepta CLIENTE
        isValid = user.getRole() == Role.CLIENTE;
      }

      if (!isValid) {
        throw new BadCredentialsException("Credenciales inválidas para este portal");
      }
    }

    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("userId", user.getId());
    extraClaims.put("name", user.getName());
    if (user.getEnterprise() != null) {
      extraClaims.put("enterpriseName", user.getEnterprise().getName());
      extraClaims.put("enterpriseId", user.getEnterprise().getId());
      extraClaims.put("enterpriseSlug", user.getEnterprise().getSlug());
      extraClaims.put("primaryColor", user.getEnterprise().getPrimaryColor());
      extraClaims.put("secondaryColor", user.getEnterprise().getSecondaryColor());
    }
    extraClaims.put("role", user.getRole().name());

    var jwtToken = jwtService.generateToken(extraClaims, user);
    return AuthResponse.builder()
        .token(jwtToken)
        .build();
  }
}
