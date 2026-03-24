package com.saloria.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.saloria.model.Enterprise;
import com.saloria.model.Role;
import com.saloria.dto.EnterpriseRequest;
import com.saloria.dto.EnterpriseResponse;
import com.saloria.dto.UserResponse;
import com.saloria.repository.EnterpriseRepository;
import com.saloria.repository.UserRepository;
import com.saloria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

  private final EnterpriseRepository enterpriseRepository;
  private final UserRepository userRepository;

  public List<EnterpriseResponse> findAll() {
    return enterpriseRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public EnterpriseResponse save(EnterpriseRequest request) {
    if (StringUtils.hasText(request.getCif()) && enterpriseRepository.findByCif(request.getCif()).isPresent()) {
      throw new IllegalArgumentException("Ya existe una empresa con ese CIF");
    }
    Enterprise enterprise = new Enterprise();
    applyRequest(enterprise, request);
    return mapToResponse(enterpriseRepository.save(enterprise));
  }

  public Enterprise findById(Long id) {
    return enterpriseRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
  }

  public EnterpriseResponse findByIdResponse(Long id) {
    return mapToResponse(findById(id));
  }

  public EnterpriseResponse findBySlug(String slug) {
    Enterprise enterprise = enterpriseRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + slug));
    return mapToResponse(enterprise);
  }

  public EnterpriseResponse update(Long id, EnterpriseRequest request) {
    Enterprise enterprise = findById(id);
    if (StringUtils.hasText(request.getCif())) {
      enterpriseRepository.findByCif(request.getCif())
          .filter(existing -> !existing.getId().equals(id))
          .ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe una empresa con ese CIF");
          });
    }
    applyRequest(enterprise, request);

    return mapToResponse(enterpriseRepository.save(enterprise));
  }

  public List<UserResponse> getEmployeesByEnterpriseId(Long enterpriseId) {
    return userRepository.findByEnterpriseIdAndRole(enterpriseId, Role.EMPLEADO)
        .stream()
        .map(user -> UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .enterpriseId(user.getEnterprise() != null ? user.getEnterprise().getId() : null)
            .build())
        .collect(Collectors.toList());
  }

  public void delete(Long id) {
    enterpriseRepository.deleteById(id);
  }

  private void applyRequest(Enterprise enterprise, EnterpriseRequest request) {
    enterprise.setName(request.getName());
    enterprise.setCif(request.getCif());
    enterprise.setAddress(request.getAddress());
    enterprise.setPhone(request.getPhone());
    enterprise.setEmail(request.getEmail());
    enterprise.setWebsite(request.getWebsite());
    enterprise.setLogo(request.getLogo());
    enterprise.setBanner(request.getBanner());
    enterprise.setInstagram(request.getInstagram());
    enterprise.setFacebook(request.getFacebook());
    enterprise.setTiktok(request.getTiktok());
    enterprise.setWhatsapp(request.getWhatsapp());
    enterprise.setPrimaryColor(request.getPrimaryColor());
    enterprise.setSecondaryColor(request.getSecondaryColor());
    enterprise.setDescription(request.getDescription());
    if (request.getSlug() != null) {
      enterprise.setSlug(request.getSlug());
    }
  }

  private EnterpriseResponse mapToResponse(Enterprise enterprise) {
    return EnterpriseResponse.builder()
        .id(enterprise.getId())
        .name(enterprise.getName())
        .slug(enterprise.getSlug())
        .cif(enterprise.getCif())
        .address(enterprise.getAddress())
        .phone(enterprise.getPhone())
        .email(enterprise.getEmail())
        .website(enterprise.getWebsite())
        .logo(enterprise.getLogo())
        .banner(enterprise.getBanner())
        .instagram(enterprise.getInstagram())
        .facebook(enterprise.getFacebook())
        .tiktok(enterprise.getTiktok())
        .whatsapp(enterprise.getWhatsapp())
        .primaryColor(enterprise.getPrimaryColor())
        .secondaryColor(enterprise.getSecondaryColor())
        .description(enterprise.getDescription())
        .build();
  }
}
