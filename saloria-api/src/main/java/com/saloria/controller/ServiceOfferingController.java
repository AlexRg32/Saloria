package com.saloria.controller;

import java.util.List;

import com.saloria.dto.ServiceOfferingRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import com.saloria.dto.ServiceOfferingResponse;
import com.saloria.service.ServiceOfferingService;
import com.saloria.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "6. Servicios", description = "Catálogo de servicios que ofrece la empresa.")
@SecurityRequirement(name = "bearerAuth")
public class ServiceOfferingController {

  private final ServiceOfferingService serviceOfferingService;
  private final StorageService storageService;

  @Operation(summary = "Listar servicios por empresa", description = "Devuelve el catálogo de servicios ofrecidos por una empresa concreta.")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  @GetMapping("/{enterpriseId}")
  public List<ServiceOfferingResponse> getAllServicesByEnterpriseId(@PathVariable Long enterpriseId) {
    return serviceOfferingService.getAllServicesByEnterpriseId(enterpriseId);
  }

  @Operation(summary = "Crear nuevo servicio", description = "Crea un nuevo servicio en la base de datos subiendo además su imagen ilustrativa.")
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #request.enterpriseId)")
  public ResponseEntity<ServiceOfferingResponse> createServiceOffering(
      @Valid @RequestPart("service") ServiceOfferingRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    String imageUrl = null;
    if (image != null && !image.isEmpty()) {
      String filename = storageService.store(image);
      imageUrl = storageService.getPublicUrl(filename);
    }

    return ResponseEntity.ok(serviceOfferingService.createServiceOffering(request, imageUrl));
  }

  @Operation(summary = "Obtener servicio por ID", description = "Recupera la información completa de un servicio activo.")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  @GetMapping("/{enterpriseId}/{id}")
  public ResponseEntity<ServiceOfferingResponse> getServiceById(@PathVariable Long enterpriseId,
      @PathVariable Long id) {
    return ResponseEntity.ok(serviceOfferingService.getServiceByIdResponse(id));
  }

  @Operation(summary = "Eliminar servicio", description = "Borra un servicio existente indicando su ID y la empresa asociada.")
  @DeleteMapping("/{enterpriseId}/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and @securityService.hasEnterpriseAccess(authentication, #enterpriseId)")
  public ResponseEntity<Void> deleteService(@PathVariable Long enterpriseId, @PathVariable Long id) {
    serviceOfferingService.deleteService(id);
    return ResponseEntity.ok().build();
  }
}
