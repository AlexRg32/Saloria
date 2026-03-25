package com.saloria.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saloria.dto.WorkingHourDTO;
import com.saloria.exception.GlobalExceptionHandler;
import com.saloria.security.SecurityService;
import com.saloria.service.WorkingHourService;

public class WorkingHourControllerTest {

  private MockMvc mockMvc;
  private WorkingHourService workingHourService;
  private SecurityService securityService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    workingHourService = Mockito.mock(WorkingHourService.class);
    securityService = Mockito.mock(SecurityService.class);

    mockMvc = MockMvcBuilders.standaloneSetup(new WorkingHourController(workingHourService, securityService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void saveBatchRejectsMixedEnterprisePayload() throws Exception {
    Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "n/a");
    List<WorkingHourDTO> payload = List.of(
        WorkingHourDTO.builder().enterpriseId(1L).day("LUNES").startTime("09:00").endTime("18:00").build(),
        WorkingHourDTO.builder().enterpriseId(2L).day("MARTES").startTime("09:00").endTime("18:00").build());

    when(securityService.hasEnterpriseAccess(authentication, 1L)).thenReturn(true);
    when(securityService.hasEnterpriseAccess(authentication, 2L)).thenReturn(false);

    mockMvc.perform(put("/api/working-hours/batch")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(payload))
        .principal(authentication))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("No tienes permisos para realizar esta acción."));

    verifyNoInteractions(workingHourService);
  }
}
