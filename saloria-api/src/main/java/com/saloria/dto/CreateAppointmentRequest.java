package com.saloria.dto;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAppointmentRequest {
  private Long userId;
  private Long customerId;

  @Size(max = 120, message = "El nombre del cliente no puede superar los 120 caracteres")
  private String customerName;

  @Size(max = 30, message = "El teléfono del cliente no puede superar los 30 caracteres")
  private String customerPhone;

  @NotNull(message = "El empleado es obligatorio")
  private Long employeeId;

  @NotNull(message = "El servicio es obligatorio")
  private Long serviceId;

  @NotNull(message = "La empresa es obligatoria")
  private Long enterpriseId;

  @NotNull(message = "La fecha es obligatoria")
  @FutureOrPresent(message = "La cita debe ser actual o futura")
  private LocalDateTime date;

  @AssertTrue(message = "Debes seleccionar un cliente registrado o indicar nombre y teléfono del cliente invitado")
  public boolean hasValidCustomerReference() {
    return customerId != null || userId != null
        || (StringUtils.hasText(customerName) && StringUtils.hasText(customerPhone));
  }
}
