package com.saloria.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class ServiceOfferingRequest {
  @NotBlank(message = "El nombre del servicio es obligatorio")
  @Size(max = 150, message = "El nombre del servicio no puede superar los 150 caracteres")
  private String name;

  @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
  private String description;

  @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
  private double price;

  @NotNull(message = "La duración es obligatoria")
  @Min(value = 5, message = "La duración mínima es de 5 minutos")
  private Integer duration;

  @NotBlank(message = "La categoría es obligatoria")
  @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
  private String category;

  @NotNull(message = "La empresa es obligatoria")
  private Long enterpriseId;
}
