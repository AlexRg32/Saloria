package com.saloria.controller;

import java.util.List;

import com.saloria.dto.CreateUserRequest;
import com.saloria.dto.UpdateUserRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.saloria.dto.UserResponse;
import com.saloria.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "3. Empleados y Usuarios", description = "Gestión de empleados de la empresa (barberos, técnicos, recepcionistas) e información de usuarios.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;

  @Operation(summary = "Listar usuarios de una empresa", description = "Obtiene la lista de todos los usuarios (empleados/admins) asociados al ID de una empresa específica.")
  @PreAuthorize("(hasRole('ADMIN') or hasRole('SUPER_ADMIN')) and @securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  @GetMapping("/{enterpriseId}")
  public List<UserResponse> getAllUsers(@PathVariable Long enterpriseId) {
    return userService.getUsersByEnterpriseId(enterpriseId);
  }

  @Operation(summary = "Crear nuevo usuario", description = "Permite a un administrador registrar a un nuevo empleado dentro de su empresa.")
  @PreAuthorize("(hasRole('ADMIN') or hasRole('SUPER_ADMIN')) and @securityService.hasEnterpriseAccess(authentication, #request.enterpriseId)")
  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request,
      Authentication authentication) {
    return ResponseEntity.ok(userService.createUser(request, authentication));
  }

  @Operation(summary = "Actualizar usuario", description = "Actualiza los datos personales, rol o información profesional de un usuario existente.")
  @PreAuthorize("(hasRole('ADMIN') or hasRole('SUPER_ADMIN')) and @securityService.canManageUser(authentication, #id)")
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
      @Valid @RequestBody UpdateUserRequest request,
      Authentication authentication) {
    return ResponseEntity.ok(userService.updateUser(id, request, authentication));
  }

  @Operation(summary = "Eliminar usuario", description = "Realiza un borrado lógico (soft delete) del usuario desvinculándolo del panel activo.")
  @PreAuthorize("(hasRole('ADMIN') or hasRole('SUPER_ADMIN')) and @securityService.canManageUser(authentication, #id)")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok().build();
  }
}
