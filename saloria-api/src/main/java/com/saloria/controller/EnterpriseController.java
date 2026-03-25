package com.saloria.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.saloria.dto.EnterpriseRequest;
import com.saloria.service.EnterpriseService;
import com.saloria.dto.EnterpriseResponse;
import com.saloria.dto.UserResponse;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
@Tag(name = "4. Empresas", description = "Administración de los datos, sede y configuración general de la empresa.")
@SecurityRequirement(name = "bearerAuth")
public class EnterpriseController {

  private final EnterpriseService enterpriseService;

  @Operation(summary = "Listar todas las empresas", description = "Obtiene un directorio de todas las empresas afiliadas a la plataforma.")
  @GetMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public List<EnterpriseResponse> findAll() {
    return enterpriseService.findAll();
  }

  @Operation(summary = "Crear nueva empresa internamente")
  @PostMapping
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<EnterpriseResponse> create(@Valid @RequestBody EnterpriseRequest request) {
    return ResponseEntity.ok(enterpriseService.save(request));
  }

  @Operation(summary = "Obtener detalles de empresa por ID")
  @GetMapping("/{id}")
  @PreAuthorize("@securityService.hasEnterpriseAccess(authentication, #id)")
  public ResponseEntity<EnterpriseResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(enterpriseService.findByIdResponse(id));
  }

  @Operation(summary = "Actualizar información de la empresa")
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #id)")
  public ResponseEntity<EnterpriseResponse> update(@PathVariable Long id, @Valid @RequestBody EnterpriseRequest request) {
    return ResponseEntity.ok(enterpriseService.update(id, request));
  }

  @Operation(summary = "Obtener empleados internos de la empresa")
  @GetMapping("/{id}/employees")
  @PreAuthorize("@securityService.hasEnterpriseAccess(authentication, #id)")
  public ResponseEntity<List<UserResponse>> getEmployees(@PathVariable Long id) {
    return ResponseEntity.ok(enterpriseService.getEmployeesByEnterpriseId(id));
  }
}
