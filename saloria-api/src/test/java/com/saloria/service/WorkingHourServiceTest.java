package com.saloria.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import com.saloria.dto.WorkingHourDTO;
import com.saloria.model.Enterprise;
import com.saloria.model.User;
import com.saloria.model.WorkingHour;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.UserRepository;
import com.saloria.repository.WorkingHourRepository;

public class WorkingHourServiceTest {

  @Mock
  private WorkingHourRepository workingHourRepository;
  @Mock
  private EnterpriseRepository enterpriseRepository;
  @Mock
  private UserRepository userRepository;

  private WorkingHourService workingHourService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    workingHourService = new WorkingHourService(workingHourRepository, enterpriseRepository, userRepository);
  }

  @Test
  void saveBatchRejectsExistingWorkingHourFromAnotherEnterprise() {
    Enterprise foreignEnterprise = Enterprise.builder().id(2L).name("Salon Sur").build();
    WorkingHour existing = new WorkingHour();
    existing.setId(99L);
    existing.setEnterprise(foreignEnterprise);

    WorkingHourDTO dto = WorkingHourDTO.builder()
        .id(99L)
        .day("LUNES")
        .startTime("09:00")
        .endTime("18:00")
        .enterpriseId(1L)
        .build();

    when(workingHourRepository.findById(99L)).thenReturn(Optional.of(existing));

    assertThrows(AccessDeniedException.class, () -> workingHourService.saveBatch(List.of(dto)));
  }

  @Test
  void saveBatchRejectsEmployeeFromAnotherEnterprise() {
    Enterprise ownEnterprise = Enterprise.builder().id(1L).name("Salon Norte").build();

    WorkingHourDTO dto = WorkingHourDTO.builder()
        .day("LUNES")
        .startTime("09:00")
        .endTime("18:00")
        .enterpriseId(1L)
        .userId(31L)
        .build();

    when(enterpriseRepository.findById(1L)).thenReturn(Optional.of(ownEnterprise));
    when(userRepository.findByIdAndEnterpriseIdAndArchivedFalse(31L, 1L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> workingHourService.saveBatch(List.of(dto)));
  }

  @Test
  void saveBatchAllowsOwnedExistingWorkingHour() {
    Enterprise ownEnterprise = Enterprise.builder().id(1L).name("Salon Norte").build();
    WorkingHour existing = new WorkingHour();
    existing.setId(99L);
    existing.setEnterprise(ownEnterprise);

    WorkingHourDTO dto = WorkingHourDTO.builder()
        .id(99L)
        .day("LUNES")
        .startTime("09:00")
        .endTime("18:00")
        .enterpriseId(1L)
        .build();

    when(workingHourRepository.findById(99L)).thenReturn(Optional.of(existing));
    when(workingHourRepository.save(any(WorkingHour.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertDoesNotThrow(() -> workingHourService.saveBatch(List.of(dto)));
  }
}
