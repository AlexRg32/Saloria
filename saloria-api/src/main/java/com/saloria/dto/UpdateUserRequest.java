package com.saloria.dto;

import com.saloria.model.Role;
import jakarta.validation.constraints.Email;
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
public class UpdateUserRequest {

  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
  private String name;

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email no es válido")
  private String email;

  @NotNull(message = "El rol es obligatorio")
  private Role role;

  @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
  private String phone;

  private Boolean active;

  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  private String password;
}
