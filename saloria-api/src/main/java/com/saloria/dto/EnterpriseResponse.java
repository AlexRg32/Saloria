package com.saloria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseResponse {
  private Long id;
  private String name;
  private String slug;
  private String cif;
  private String address;
  private String phone;
  private String email;
  private String website;
  private String logo;
  private String banner;
  private String instagram;
  private String facebook;
  private String tiktok;
  private String whatsapp;
  private String primaryColor;
  private String secondaryColor;
  private String description;
  private EnterpriseReadinessResponse readiness;
}
