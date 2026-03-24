package com.saloria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseRequest {

  @NotBlank(message = "El nombre comercial es obligatorio")
  @Size(max = 150, message = "El nombre comercial no puede superar los 150 caracteres")
  private String name;

  @Size(max = 30, message = "El CIF no puede superar los 30 caracteres")
  private String cif;

  @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
  private String address;

  @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
  private String phone;

  @Email(message = "El email no es válido")
  private String email;

  @Size(max = 255, message = "La web no puede superar los 255 caracteres")
  private String website;

  @Size(max = 255, message = "El logo no puede superar los 255 caracteres")
  private String logo;

  @Size(max = 255, message = "El banner no puede superar los 255 caracteres")
  private String banner;

  @Size(max = 255, message = "Instagram no puede superar los 255 caracteres")
  private String instagram;

  @Size(max = 255, message = "Facebook no puede superar los 255 caracteres")
  private String facebook;

  @Size(max = 255, message = "TikTok no puede superar los 255 caracteres")
  private String tiktok;

  @Size(max = 255, message = "WhatsApp no puede superar los 255 caracteres")
  private String whatsapp;

  @Size(max = 20, message = "El color principal no puede superar los 20 caracteres")
  private String primaryColor;

  @Size(max = 20, message = "El color secundario no puede superar los 20 caracteres")
  private String secondaryColor;

  @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
  private String description;

  @Size(max = 120, message = "El slug no puede superar los 120 caracteres")
  private String slug;
}
