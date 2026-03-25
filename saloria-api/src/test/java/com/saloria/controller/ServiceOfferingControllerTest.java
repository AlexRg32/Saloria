package com.saloria.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saloria.dto.ServiceOfferingRequest;
import com.saloria.dto.ServiceOfferingResponse;
import com.saloria.exception.GlobalExceptionHandler;
import com.saloria.service.ServiceOfferingService;
import com.saloria.service.StorageService;

public class ServiceOfferingControllerTest {

  private MockMvc mockMvc;
  private ServiceOfferingService serviceOfferingService;
  private StorageService storageService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    serviceOfferingService = Mockito.mock(ServiceOfferingService.class);
    storageService = Mockito.mock(StorageService.class);

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders.standaloneSetup(new ServiceOfferingController(serviceOfferingService, storageService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @Test
  void createServiceAcceptsJsonPartWithoutImage() throws Exception {
    ServiceOfferingRequest request = ServiceOfferingRequest.builder()
        .name("Corte")
        .description("Corte clásico")
        .price(15.0)
        .duration(30)
        .category("Corte")
        .enterpriseId(8L)
        .build();

    ServiceOfferingResponse response = ServiceOfferingResponse.builder()
        .id(1L)
        .name("Corte")
        .enterpriseId(8L)
        .build();

    when(serviceOfferingService.createServiceOffering(eq(request), eq(null))).thenReturn(response);

    MockMultipartFile servicePart = new MockMultipartFile(
        "service",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(request));

    mockMvc.perform(multipart("/api/services").file(servicePart))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Corte"))
        .andExpect(jsonPath("$.enterpriseId").value(8));

    verify(serviceOfferingService).createServiceOffering(eq(request), eq(null));
    verify(storageService, never()).store(Mockito.any());
  }

  @Test
  void createServiceStoresImageWhenProvided() throws Exception {
    ServiceOfferingRequest request = ServiceOfferingRequest.builder()
        .name("Barba")
        .description("Arreglo de barba")
        .price(12.0)
        .duration(25)
        .category("Barba")
        .enterpriseId(9L)
        .build();

    ServiceOfferingResponse response = ServiceOfferingResponse.builder()
        .id(2L)
        .name("Barba")
        .image("https://cdn.example/barba.png")
        .enterpriseId(9L)
        .build();

    when(storageService.store(Mockito.any())).thenReturn("barba.png");
    when(storageService.getPublicUrl("barba.png")).thenReturn("https://cdn.example/barba.png");
    when(serviceOfferingService.createServiceOffering(eq(request), eq("https://cdn.example/barba.png")))
        .thenReturn(response);

    MockMultipartFile servicePart = new MockMultipartFile(
        "service",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(request));
    MockMultipartFile imagePart = new MockMultipartFile(
        "image",
        "barba.png",
        "image/png",
        "fake-image".getBytes());

    mockMvc.perform(multipart("/api/services").file(servicePart).file(imagePart))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.image").value("https://cdn.example/barba.png"));

    verify(storageService).store(Mockito.any());
    verify(storageService).getPublicUrl("barba.png");
    verify(serviceOfferingService).createServiceOffering(eq(request), eq("https://cdn.example/barba.png"));
  }

  @Test
  void getServiceByIdPassesEnterpriseScopeToService() throws Exception {
    ServiceOfferingResponse response = ServiceOfferingResponse.builder()
        .id(22L)
        .name("Color")
        .enterpriseId(7L)
        .build();

    when(serviceOfferingService.getServiceByIdResponse(7L, 22L)).thenReturn(response);

    mockMvc.perform(get("/api/services/7/22"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(22))
        .andExpect(jsonPath("$.enterpriseId").value(7));

    verify(serviceOfferingService).getServiceByIdResponse(7L, 22L);
  }

  @Test
  void deleteServicePassesEnterpriseScopeToService() throws Exception {
    mockMvc.perform(delete("/api/services/7/22"))
        .andExpect(status().isOk());

    verify(serviceOfferingService).deleteService(7L, 22L);
  }
}
