package com.saloria.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseReadinessResponse {
  private boolean publicProfileReady;
  private boolean bookingReady;
  private String publicProfilePath;
  private List<String> missingPublicProfile;
  private List<String> missingBookingSetup;
}
