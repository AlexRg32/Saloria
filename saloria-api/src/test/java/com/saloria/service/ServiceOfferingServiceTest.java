package com.saloria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.saloria.dto.ServiceOfferingResponse;
import com.saloria.exception.ResourceNotFoundException;
import com.saloria.model.Enterprise;
import com.saloria.model.ServiceOffering;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.ServiceOfferingRepository;

public class ServiceOfferingServiceTest {

  @Mock
  private ServiceOfferingRepository serviceOfferingRepository;
  @Mock
  private EnterpriseRepository enterpriseRepository;

  private ServiceOfferingService serviceOfferingService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    serviceOfferingService = new ServiceOfferingService(serviceOfferingRepository, enterpriseRepository);
  }

  @Test
  void getServiceByIdRejectsCrossTenantLookup() {
    when(serviceOfferingRepository.findByIdAndEnterpriseIdAndDeletedFalse(44L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> serviceOfferingService.getServiceByIdResponse(1L, 44L));
  }

  @Test
  void deleteServiceRejectsCrossTenantLookup() {
    when(serviceOfferingRepository.findByIdAndEnterpriseIdAndDeletedFalse(44L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> serviceOfferingService.deleteService(1L, 44L));
    verify(serviceOfferingRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void getServiceByIdReturnsOwnedService() {
    Enterprise enterprise = Enterprise.builder().id(1L).name("Salon Norte").build();
    ServiceOffering service = ServiceOffering.builder()
        .id(44L)
        .name("Color")
        .enterprise(enterprise)
        .deleted(false)
        .build();

    when(serviceOfferingRepository.findByIdAndEnterpriseIdAndDeletedFalse(44L, 1L)).thenReturn(Optional.of(service));

    ServiceOfferingResponse response = serviceOfferingService.getServiceByIdResponse(1L, 44L);

    assertEquals(44L, response.getId());
    assertEquals(1L, response.getEnterpriseId());
  }
}
