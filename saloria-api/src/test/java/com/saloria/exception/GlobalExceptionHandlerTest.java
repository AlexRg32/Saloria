package com.saloria.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartException;

public class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleResourceNotFoundReturns404() {
    ResponseEntity<Map<String, String>> response = handler
        .handleResourceNotFoundException(new ResourceNotFoundException("No encontrado"));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("No encontrado", response.getBody().get("message"));
  }

  @Test
  void handleAccessDeniedReturns403() {
    ResponseEntity<Map<String, String>> response = handler
        .handleAccessDeniedException(new AccessDeniedException("forbidden"));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("No tienes permisos para realizar esta acción.", response.getBody().get("message"));
  }

  @Test
  void handleGeneralExceptionReturns500WithoutLeakingInternalMessage() {
    ResponseEntity<Map<String, String>> response = handler
        .handleGeneralException(new RuntimeException("secret internals"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.", response.getBody().get("message"));
  }

  @Test
  void handleMultipartExceptionReturns400WithFriendlyMessage() {
    ResponseEntity<Map<String, String>> response = handler
        .handleMultipartException(new MultipartException("bad multipart"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("No se pudo procesar la subida del servicio. Revisa la imagen e inténtalo de nuevo.",
        response.getBody().get("message"));
  }
}
