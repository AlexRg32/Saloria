package com.saloria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saloria.dto.EnterpriseRequest;
import com.saloria.dto.EnterpriseResponse;
import com.saloria.dto.PublicEnterpriseSummaryResponse;
import com.saloria.model.Enterprise;
import com.saloria.model.Role;
import com.saloria.model.ServiceOffering;
import com.saloria.model.User;
import com.saloria.model.WorkingHour;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.ServiceOfferingRepository;
import com.saloria.repository.UserRepository;
import com.saloria.repository.WorkingHourRepository;

@ExtendWith(MockitoExtension.class)
public class EnterpriseServiceTest {

  @Mock
  private EnterpriseRepository enterpriseRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ServiceOfferingRepository serviceOfferingRepository;

  @Mock
  private WorkingHourRepository workingHourRepository;

  @InjectMocks
  private EnterpriseService enterpriseService;

  @Test
  public void testFindBySlugIncludesReadiness() {
    String slug = "barberia-alex";
    Enterprise enterprise = Enterprise.builder()
        .id(1L)
        .name("Barberia Alex")
        .slug(slug)
        .address("Calle Mayor 10, Madrid")
        .description("Cortes con reserva")
        .phone("600111222")
        .build();

    WorkingHour workingHour = new WorkingHour();
    workingHour.setEnterprise(enterprise);
    workingHour.setDay("LUNES");
    workingHour.setDayOff(false);
    workingHour.setStartTime("09:00");
    workingHour.setEndTime("20:00");

    when(enterpriseRepository.findBySlug(slug)).thenReturn(Optional.of(enterprise));
    when(serviceOfferingRepository.findByEnterpriseIdAndDeletedFalse(1L))
        .thenReturn(List.of(ServiceOffering.builder().id(2L).name("Corte").enterprise(enterprise).build()));
    when(userRepository.findByEnterpriseIdAndRoleAndArchivedFalse(1L, Role.EMPLEADO))
        .thenReturn(List.of(User.builder().id(7L).name("Alex").enterprise(enterprise).role(Role.EMPLEADO).build()));
    when(workingHourRepository.findByEnterpriseId(1L)).thenReturn(List.of(workingHour));

    EnterpriseResponse response = enterpriseService.findBySlug(slug);

    assertNotNull(response);
    assertEquals("Barberia Alex", response.getName());
    assertEquals(slug, response.getSlug());
    assertNotNull(response.getReadiness());
    assertTrue(response.getReadiness().isPublicProfileReady());
    assertTrue(response.getReadiness().isBookingReady());
    assertEquals("/b/barberia-alex", response.getReadiness().getPublicProfilePath());
  }

  @Test
  void saveGeneratesNormalizedSlugWhenMissing() {
    EnterpriseRequest request = EnterpriseRequest.builder()
        .name("Peluquería Ñandú")
        .email("hola@pelu.com")
        .build();

    when(enterpriseRepository.findByName("Peluquería Ñandú")).thenReturn(Optional.empty());
    when(enterpriseRepository.save(any(Enterprise.class))).thenAnswer(invocation -> {
      Enterprise enterprise = invocation.getArgument(0);
      enterprise.setId(8L);
      return enterprise;
    });
    when(serviceOfferingRepository.findByEnterpriseIdAndDeletedFalse(8L)).thenReturn(List.of());
    when(userRepository.findByEnterpriseIdAndRoleAndArchivedFalse(8L, Role.EMPLEADO)).thenReturn(List.of());
    when(workingHourRepository.findByEnterpriseId(8L)).thenReturn(List.of());

    EnterpriseResponse response = enterpriseService.save(request);

    assertEquals("peluqueria-nandu", response.getSlug());
    assertFalse(response.getReadiness().isPublicProfileReady());
    assertTrue(response.getReadiness().getMissingPublicProfile().contains("Añade la dirección del negocio"));
  }

  @Test
  void findPublicDirectoryOmitsIncompleteBusinesses() {
    Enterprise ready = Enterprise.builder()
        .id(1L)
        .name("Salon Visible")
        .slug("salon-visible")
        .address("Gran Via 1, Madrid")
        .description("Color y corte")
        .email("hola@visible.com")
        .build();

    Enterprise incomplete = Enterprise.builder()
        .id(2L)
        .name("Salon Oculto")
        .slug("salon-oculto")
        .email("hola@oculto.com")
        .build();

    ServiceOffering readyService = ServiceOffering.builder()
        .id(10L)
        .name("Corte")
        .category("Corte")
        .enterprise(ready)
        .price(20.0)
        .build();

    when(enterpriseRepository.findAll()).thenReturn(List.of(ready, incomplete));
    when(serviceOfferingRepository.findByEnterpriseIdInAndDeletedFalse(List.of(1L)))
        .thenReturn(List.of(readyService));

    List<PublicEnterpriseSummaryResponse> result = enterpriseService.findPublicDirectory(null);

    assertEquals(1, result.size());
    assertEquals("salon-visible", result.get(0).getSlug());
  }
}
