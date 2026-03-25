package com.saloria.service;

import com.saloria.dto.CreateAppointmentRequest;
import com.saloria.dto.AppointmentResponse;
import com.saloria.exception.ResourceNotFoundException;
import com.saloria.model.*;
import com.saloria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final UserRepository userRepository;
  private final CustomerRepository customerRepository;
  private final ServiceOfferingRepository serviceOfferingRepository;
  private final EnterpriseRepository enterpriseRepository;
  private final WorkingHourRepository workingHourRepository;

  private static final Map<java.time.DayOfWeek, String> DAY_LABELS = Map.of(
      java.time.DayOfWeek.MONDAY, "LUNES",
      java.time.DayOfWeek.TUESDAY, "MARTES",
      java.time.DayOfWeek.WEDNESDAY, "MIERCOLES",
      java.time.DayOfWeek.THURSDAY, "JUEVES",
      java.time.DayOfWeek.FRIDAY, "VIERNES",
      java.time.DayOfWeek.SATURDAY, "SABADO",
      java.time.DayOfWeek.SUNDAY, "DOMINGO");

  public AppointmentResponse create(CreateAppointmentRequest request) {
    User employee = userRepository.findById(request.getEmployeeId())
        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));
    ServiceOffering service = serviceOfferingRepository.findById(request.getServiceId())
        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    Enterprise enterprise = enterpriseRepository.findById(request.getEnterpriseId())
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

    validateEmployee(employee, enterprise);
    validateService(service, enterprise);

    LocalDateTime requestStart = request.getDate();
    LocalDateTime requestEnd = requestStart.plusMinutes(service.getDuration());
    validateWorkingSchedule(employee, enterprise, requestStart, requestEnd);

    // Check for conflicts
    LocalDateTime dayStart = requestStart.toLocalDate().atStartOfDay();
    LocalDateTime dayEnd = requestStart.toLocalDate().atTime(23, 59, 59);

    List<Appointment> existingAppointments = appointmentRepository.findByEmployeeIdAndDateBetween(
        request.getEmployeeId(), dayStart, dayEnd);

    boolean hasConflict = existingAppointments.stream()
        .filter(a -> a.getStatus() != AppointmentStatus.CANCELED)
        .anyMatch(a -> {
          LocalDateTime existingStart = a.getDate();
          LocalDateTime existingEnd = existingStart.plusMinutes(a.getService().getDuration());
          return requestStart.isBefore(existingEnd) && requestEnd.isAfter(existingStart);
        });

    if (hasConflict) {
      throw new IllegalStateException("El empleado ya tiene una cita en ese horario.");
    }

    Customer customer = getOrCreateCustomer(request, enterprise);

    Appointment appointment = new Appointment();
    appointment.setCustomer(customer);
    appointment.setEmployee(employee);
    appointment.setService(service);
    appointment.setEnterprise(enterprise);
    appointment.setDate(request.getDate());
    appointment.setPrice(service.getPrice());
    appointment.setStatus(AppointmentStatus.PENDING);

    Appointment saved = appointmentRepository.save(appointment);

    // Increment visit count
    customer.setVisitsCount(customer.getVisitsCount() + 1);
    customerRepository.save(customer);

    return mapToResponse(saved);
  }

  private Customer getOrCreateCustomer(CreateAppointmentRequest request, Enterprise enterprise) {
    if (request.getCustomerId() != null) {
      Customer customer = customerRepository.findById(request.getCustomerId())
          .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
      validateCustomer(customer, enterprise);
      return customer;
    } else if (request.getUserId() != null) {
      // Registered User
      return customerRepository.findByEnterpriseIdAndUserId(enterprise.getId(), request.getUserId())
          .map(existing -> {
            validateCustomer(existing, enterprise);
            if (existing.getUser() != null) {
              validatePortalUser(existing.getUser(), enterprise);
            }
            return existing;
          })
          .orElseGet(() -> {
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            validatePortalUser(user, enterprise);
            return customerRepository.save(Customer.builder()
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .enterprise(enterprise)
                .user(user)
                .build());
          });
    } else {
      // Guest / Walk-in
      return customerRepository.findByEnterpriseIdAndPhone(enterprise.getId(), request.getCustomerPhone())
          .map(existing -> {
            // Update name if it was empty or changed
            if (request.getCustomerName() != null && !request.getCustomerName().equals(existing.getName())) {
              existing.setName(request.getCustomerName());
              return customerRepository.save(existing);
            }
            return existing;
          })
          .orElseGet(() -> customerRepository.save(Customer.builder()
              .name(request.getCustomerName())
              .phone(request.getCustomerPhone())
              .enterprise(enterprise)
              .build()));
    }
  }

  public List<AppointmentResponse> findByEnterpriseId(Long enterpriseId) {
    return appointmentRepository.findByEnterpriseId(enterpriseId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<AppointmentResponse> findByEmployeeId(Long employeeId) {
    return appointmentRepository.findByEmployeeIdOrderByDateDesc(employeeId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<AppointmentResponse> findByUserEmail(String email) {
    User user = userRepository.findByEmailAndArchivedFalse(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    return appointmentRepository.findByCustomerUserIdOrderByDateDesc(user.getId()).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public AppointmentResponse checkout(Long id, PaymentMethod method) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

    appointment.setPaid(true);
    appointment.setPaymentMethod(method);
    appointment.setPaidAt(java.time.LocalDateTime.now());
    appointment.setStatus(AppointmentStatus.COMPLETED);

    return mapToResponse(appointmentRepository.save(appointment));
  }

  public List<AppointmentResponse> findTransactions(Long enterpriseId, java.time.LocalDateTime start,
      java.time.LocalDateTime end) {
    return appointmentRepository.findTransactions(enterpriseId, start, end).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public com.saloria.dto.BillingSummaryDTO getBillingSummary(Long enterpriseId) {
    java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
    java.time.LocalDateTime weekStart = java.time.LocalDate.now().minusWeeks(1).atStartOfDay();
    java.time.LocalDateTime monthStart = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();

    Double today = appointmentRepository.sumRevenueSince(enterpriseId, todayStart);
    Double week = appointmentRepository.sumRevenueSince(enterpriseId, weekStart);
    Double month = appointmentRepository.sumRevenueSince(enterpriseId, monthStart);

    return com.saloria.dto.BillingSummaryDTO.builder()
        .revenueToday(today != null ? today : 0.0)
        .revenueThisWeek(week != null ? week : 0.0)
        .revenueThisMonth(month != null ? month : 0.0)
        .transactionsCount(appointmentRepository.countByEnterpriseId(enterpriseId))
        .build();
  }

  private AppointmentResponse mapToResponse(Appointment a) {
    Customer c = a.getCustomer();
    return AppointmentResponse.builder()
        .id(a.getId())
        .customerName(c.getName())
        .customerPhone(c.getPhone())
        .employeeName(a.getEmployee().getName())
        .serviceName(a.getService().getName())
        .date(a.getDate())
        .duration(a.getService().getDuration())
        .price(a.getPrice())
        .status(a.getStatus().name())
        .paid(a.isPaid())
        .paymentMethod(a.getPaymentMethod() != null ? a.getPaymentMethod().name() : null)
        .paidAt(a.getPaidAt())
        .enterpriseId(a.getEnterprise().getId())
        .enterpriseName(a.getEnterprise().getName())
        .enterpriseSlug(a.getEnterprise().getSlug())
        .build();
  }

  private void validateEmployee(User employee, Enterprise enterprise) {
    if (employee.getEnterprise() == null || !enterprise.getId().equals(employee.getEnterprise().getId())) {
      throw new AccessDeniedException("El empleado no pertenece a la empresa indicada");
    }
    if (employee.getRole() == Role.CLIENTE) {
      throw new IllegalArgumentException("El profesional seleccionado no es válido");
    }
    if (!employee.isEnabled()) {
      throw new IllegalArgumentException("El profesional seleccionado está inactivo");
    }
  }

  private void validateService(ServiceOffering service, Enterprise enterprise) {
    if (service.getEnterprise() == null || !enterprise.getId().equals(service.getEnterprise().getId())) {
      throw new AccessDeniedException("El servicio no pertenece a la empresa indicada");
    }
    if (service.isDeleted()) {
      throw new IllegalArgumentException("El servicio seleccionado ya no está disponible");
    }
  }

  private void validateCustomer(Customer customer, Enterprise enterprise) {
    if (customer.getEnterprise() == null || !enterprise.getId().equals(customer.getEnterprise().getId())) {
      throw new AccessDeniedException("El cliente no pertenece a la empresa indicada");
    }
  }

  private void validatePortalUser(User user, Enterprise enterprise) {
    if (user.getRole() != Role.CLIENTE) {
      throw new IllegalArgumentException("Solo se pueden vincular usuarios cliente a una reserva");
    }
    if (!user.isEnabled()) {
      throw new IllegalArgumentException("El usuario cliente está inactivo");
    }
    if (user.getEnterprise() != null && !enterprise.getId().equals(user.getEnterprise().getId())) {
      throw new AccessDeniedException("El usuario no puede reservar en una empresa distinta");
    }
  }

  private void validateWorkingSchedule(User employee, Enterprise enterprise, LocalDateTime requestStart,
      LocalDateTime requestEnd) {
    WorkingWindow workingWindow = resolveWorkingWindow(employee, enterprise, requestStart.getDayOfWeek());

    if (workingWindow.dayOff()) {
      throw new IllegalStateException("El profesional no trabaja el día seleccionado.");
    }

    LocalTime requestedStartTime = requestStart.toLocalTime();
    LocalTime requestedEndTime = requestEnd.toLocalTime();

    if (requestedStartTime.isBefore(workingWindow.startTime()) || requestedEndTime.isAfter(workingWindow.endTime())) {
      throw new IllegalStateException("La cita debe estar dentro del horario laboral del profesional.");
    }
  }

  private WorkingWindow resolveWorkingWindow(User employee, Enterprise enterprise, java.time.DayOfWeek dayOfWeek) {
    String requestedDay = DAY_LABELS.get(dayOfWeek);

    return workingHourRepository.findFirstByUser_IdAndDay(employee.getId(), requestedDay)
        .map(this::toWorkingWindow)
        .or(() -> workingHourRepository.findFirstByEnterpriseIdAndUserIdIsNullAndDay(enterprise.getId(), requestedDay)
            .map(this::toWorkingWindow))
        .orElseGet(() -> defaultWorkingWindow(dayOfWeek));
  }

  private WorkingWindow toWorkingWindow(WorkingHour workingHour) {
    return new WorkingWindow(
        LocalTime.parse(workingHour.getStartTime()),
        LocalTime.parse(workingHour.getEndTime()),
        workingHour.isDayOff());
  }

  private WorkingWindow defaultWorkingWindow(java.time.DayOfWeek dayOfWeek) {
    boolean isSunday = dayOfWeek == java.time.DayOfWeek.SUNDAY;
    return new WorkingWindow(LocalTime.of(9, 0), LocalTime.of(20, 0), isSunday);
  }

  private record WorkingWindow(LocalTime startTime, LocalTime endTime, boolean dayOff) {
  }
}
