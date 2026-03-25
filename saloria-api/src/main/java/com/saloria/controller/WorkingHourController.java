package com.saloria.controller;

import com.saloria.dto.WorkingHourDTO;
import com.saloria.service.WorkingHourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.saloria.security.SecurityService;

@RestController
@RequestMapping("/api/working-hours")
@RequiredArgsConstructor
@Tag(name = "8. Horarios de Trabajo", description = "Configuración de franjas de horarios de apertura por defecto y turnos laborales individuales para empleados.")
@SecurityRequirement(name = "bearerAuth")
public class WorkingHourController {

  private final WorkingHourService workingHourService;
  private final SecurityService securityService;

  @Operation(summary = "Consultar horarios generales de empresa", description = "Devuelve los horarios por defecto vinculados explícitamente a la empresa principal.")
  @GetMapping("/enterprise/{enterpriseId}")
  @PreAuthorize("@securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  public ResponseEntity<List<WorkingHourDTO>> getEnterpriseHours(@PathVariable Long enterpriseId) {
    return ResponseEntity.ok(workingHourService.getEnterpriseHours(enterpriseId));
  }

  @Operation(summary = "Consultar horarios de un usuario/empleado específico", description = "Útil para armar el grid del calendario para este empleado únicamente en las horas que labora.")
  @GetMapping("/user/{userId}")
  @PreAuthorize("@securityService.canManageUser(authentication, #userId)")
  public ResponseEntity<List<WorkingHourDTO>> getUserHours(@PathVariable Long userId) {
    return ResponseEntity.ok(workingHourService.getUserHours(userId));
  }

  @Operation(summary = "Guardar cambios masivos en horarios", description = "Permite reemplazar o insertar en lote varias entidades de WorkingHour (por ejemplo, guardar la jornada laboral del lunes a sábado simultáneamente).")
  @PutMapping("/batch")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<WorkingHourDTO>> saveBatch(@RequestBody List<WorkingHourDTO> dtos,
      Authentication authentication) {
    if (dtos != null && !dtos.isEmpty()) {
      for (WorkingHourDTO dto : dtos) {
        if (!securityService.hasEnterpriseAccess(authentication, dto.getEnterpriseId())) {
          throw new org.springframework.security.access.AccessDeniedException("Access Denied");
        }
      }
    }
    return ResponseEntity.ok(workingHourService.saveBatch(dtos));
  }
}
