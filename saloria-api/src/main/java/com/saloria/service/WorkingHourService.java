package com.saloria.service;

import com.saloria.dto.WorkingHourDTO;
import com.saloria.model.Enterprise;
import com.saloria.model.User;
import com.saloria.model.WorkingHour;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.UserRepository;
import com.saloria.repository.WorkingHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkingHourService {

  private final WorkingHourRepository workingHourRepository;
  private final EnterpriseRepository enterpriseRepository;
  private final UserRepository userRepository;

  private static final List<String> DAYS = Arrays.asList(
      "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");

  @Transactional
  public List<WorkingHourDTO> getEnterpriseHours(Long enterpriseId) {
    List<WorkingHour> hours = workingHourRepository.findByEnterpriseIdAndUserIdIsNull(enterpriseId);
    if (hours.isEmpty()) {
      return initializeHours(enterpriseId, null);
    }
    return hours.stream()
        .map(this::mapToDTO)
        .sorted(Comparator.comparingInt(dto -> DAYS.indexOf(dto.getDay())))
        .collect(Collectors.toList());
  }

  @Transactional
  public List<WorkingHourDTO> getUserHours(Long userId) {
    List<WorkingHour> hours = workingHourRepository.findByUser_Id(userId);
    if (hours.isEmpty()) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      return initializeHours(user.getEnterprise().getId(), userId);
    }
    return hours.stream()
        .map(this::mapToDTO)
        .sorted(Comparator.comparingInt(dto -> DAYS.indexOf(dto.getDay())))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<WorkingHourDTO> getEnterpriseHoursSnapshot(Long enterpriseId) {
    enterpriseRepository.findById(enterpriseId)
        .orElseThrow(() -> new RuntimeException("Enterprise not found"));

    List<WorkingHour> hours = workingHourRepository.findByEnterpriseIdAndUserIdIsNull(enterpriseId);
    if (hours.isEmpty()) {
      return buildDefaultHours(enterpriseId, null);
    }

    return hours.stream()
        .map(this::mapToDTO)
        .sorted(Comparator.comparingInt(dto -> DAYS.indexOf(dto.getDay())))
        .collect(Collectors.toList());
  }

  @Transactional
  public List<WorkingHourDTO> saveBatch(List<WorkingHourDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return List.of();
    }

    return dtos.stream().map(dto -> {
      WorkingHour entity;
      if (dto.getId() != null) {
        entity = workingHourRepository.findById(dto.getId())
            .orElseThrow(() -> new RuntimeException("Working hour not found"));
        validateWorkingHourOwnership(entity, dto.getEnterpriseId());
      } else {
        entity = new WorkingHour();
      }

      entity.setDay(dto.getDay());
      entity.setStartTime(dto.getStartTime());
      entity.setEndTime(dto.getEndTime());
      entity.setDayOff(dto.isDayOff());

      if (entity.getEnterprise() == null) {
        Enterprise enterprise = enterpriseRepository.findById(dto.getEnterpriseId())
            .orElseThrow(() -> new RuntimeException("Enterprise not found"));
        entity.setEnterprise(enterprise);
      }

      if (dto.getUserId() != null && entity.getUser() == null) {
        User user = userRepository.findByIdAndEnterpriseIdAndArchivedFalse(dto.getUserId(), dto.getEnterpriseId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        entity.setUser(user);
      } else if (dto.getUserId() != null) {
        validateUserOwnership(entity.getUser(), dto.getEnterpriseId());
      } else {
        entity.setUser(null);
      }

      return mapToDTO(workingHourRepository.save(entity));
    }).collect(Collectors.toList());
  }

  private List<WorkingHourDTO> initializeHours(Long enterpriseId, Long userId) {
    Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
        .orElseThrow(() -> new RuntimeException("Enterprise not found"));
    User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

    return DAYS.stream().map(day -> {
      WorkingHour wh = new WorkingHour();
      wh.setDay(day);
      wh.setStartTime("09:00");
      wh.setEndTime("20:00");
      wh.setDayOff(day.equals("DOMINGO"));
      wh.setEnterprise(enterprise);
      wh.setUser(user);
      return mapToDTO(workingHourRepository.save(wh));
    }).sorted(Comparator.comparingInt(dto -> DAYS.indexOf(dto.getDay()))).collect(Collectors.toList());
  }

  private List<WorkingHourDTO> buildDefaultHours(Long enterpriseId, Long userId) {
    return DAYS.stream()
        .map(day -> WorkingHourDTO.builder()
            .day(day)
            .startTime("09:00")
            .endTime("20:00")
            .dayOff(day.equals("DOMINGO"))
            .enterpriseId(enterpriseId)
            .userId(userId)
            .build())
        .collect(Collectors.toList());
  }

  private WorkingHourDTO mapToDTO(WorkingHour wh) {
    return WorkingHourDTO.builder()
        .id(wh.getId())
        .day(wh.getDay())
        .startTime(wh.getStartTime())
        .endTime(wh.getEndTime())
        .dayOff(wh.isDayOff())
        .enterpriseId(wh.getEnterprise().getId())
        .userId(wh.getUser() != null ? wh.getUser().getId() : null)
        .build();
  }

  private void validateWorkingHourOwnership(WorkingHour workingHour, Long enterpriseId) {
    if (workingHour.getEnterprise() == null || !workingHour.getEnterprise().getId().equals(enterpriseId)) {
      throw new AccessDeniedException("El horario no pertenece a la empresa indicada");
    }
  }

  private void validateUserOwnership(User user, Long enterpriseId) {
    if (user == null || user.getEnterprise() == null || !user.getEnterprise().getId().equals(enterpriseId)) {
      throw new AccessDeniedException("El empleado no pertenece a la empresa indicada");
    }
  }
}
