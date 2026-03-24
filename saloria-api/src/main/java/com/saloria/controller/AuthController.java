package com.saloria.controller;

import com.saloria.dto.AuthRequest;
import com.saloria.dto.AuthResponse;
import com.saloria.dto.RegisterRequest;
import com.saloria.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1. Autenticación", description = "Endpoints para registro y login de usuarios y empresas.")
public class AuthController {

  private final AuthenticationService service;

  @Operation(summary = "Registrar nueva empresa", description = "Crea una nueva empresa junto con su cuenta de administrador inicial.")
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(service.register(request));
  }

  @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario usando email y contraseña, y devuelve el token JWT de acceso.")
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> authenticate(
      @Valid @RequestBody AuthRequest request) {
    return ResponseEntity.ok(service.authenticate(request));
  }
}
