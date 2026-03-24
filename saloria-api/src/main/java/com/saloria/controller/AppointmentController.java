package com.saloria.controller;

import com.saloria.dto.AppointmentResponse;
import com.saloria.dto.CreateAppointmentRequest;
import com.saloria.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.saloria.model.PaymentMethod;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "7. Citas y Facturación", description = "Gestión de citas, reservas de clientes, cobros de caja (checkout) y resumen de facturación.")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

  private final AppointmentService appointmentService;

  @Operation(summary = "Crear reserva internamente", description = "Crea una reserva de forma administrativa sin que un cliente acceda desde la interfaz pública.")
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #request.enterpriseId)")
  public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
    return ResponseEntity.ok(appointmentService.create(request));
  }

  @Operation(summary = "Mis citas", description = "Lista las próximas citas del usuario autenticado (actualmente en la implementación basada en email logeado).")
  @GetMapping("/me")
  public ResponseEntity<List<AppointmentResponse>> getMyAppointments(java.security.Principal principal) {
    return ResponseEntity.ok(appointmentService.findByUserEmail(principal.getName()));
  }

  @Operation(summary = "Listar todas las citas de empresa", description = "Recupera todas las reservas registradas a nivel global en una empresa (para vista general o administradores).")
  @GetMapping
  @PreAuthorize("@securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  public ResponseEntity<List<AppointmentResponse>> getAll(@RequestParam Long enterpriseId) {
    return ResponseEntity.ok(appointmentService.findByEnterpriseId(enterpriseId));
  }

  @Operation(summary = "Citas por empleado", description = "Útil en el modo vista de calendario por empleados: lista sólo las citas asignadas a la agenda de este profesional específico.")
  @GetMapping("/employee/{employeeId}")
  @PreAuthorize("@securityService.canManageUser(authentication, #employeeId)")
  public ResponseEntity<List<AppointmentResponse>> getByEmployee(@PathVariable Long employeeId) {
    return ResponseEntity.ok(appointmentService.findByEmployeeId(employeeId));
  }

  @Operation(summary = "Pagar/Cobrar Cita (Checkout)", description = "Cambia el estado de la cita a COMPLETADA y registra el pago indicando el método usado (Efectivo, Tarjeta, etc).")
  @PostMapping("/{id}/checkout")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.canManageAppointment(authentication, #id)")
  public ResponseEntity<AppointmentResponse> checkout(@PathVariable Long id, @RequestBody Map<String, String> payload) {
    PaymentMethod method = PaymentMethod.valueOf(payload.get("paymentMethod"));
    return ResponseEntity.ok(appointmentService.checkout(id, method));
  }

  @Operation(summary = "Reporte de Transacciones", description = "Filtro de citas pasadas por fecha para uso de historial y contabilidad.")
  @GetMapping("/transactions")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  public ResponseEntity<List<AppointmentResponse>> getTransactions(
      @RequestParam Long enterpriseId,
      @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime start,
      @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime end) {
    return ResponseEntity.ok(appointmentService.findTransactions(enterpriseId, start, end));
  }

  @Operation(summary = "Resumen de facturación (Billing)", description = "Suma el total ingresado a partir de las citas cobradas en el negocio.")
  @GetMapping("/billing-summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  public ResponseEntity<com.saloria.dto.BillingSummaryDTO> getBillingSummary(@RequestParam Long enterpriseId) {
    return ResponseEntity.ok(appointmentService.getBillingSummary(enterpriseId));
  }
}
