package com.saloria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email no es válido")
  private String email;

  @NotBlank(message = "La contraseña es obligatoria")
  private String password;

  @Pattern(regexp = "ADMIN|CLIENTE", message = "El portal solicitado no es válido")
  private String requiredRole;
}
