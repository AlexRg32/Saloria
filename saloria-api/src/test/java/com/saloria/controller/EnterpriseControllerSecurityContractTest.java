package com.saloria.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

public class EnterpriseControllerSecurityContractTest {

  @Test
  void findAllIsRestrictedToSuperAdmin() throws NoSuchMethodException {
    Method method = EnterpriseController.class.getMethod("findAll");
    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

    assertNotNull(preAuthorize);
    assertEquals("hasRole('SUPER_ADMIN')", preAuthorize.value());
  }

  @Test
  void createIsRestrictedToSuperAdmin() throws NoSuchMethodException {
    Method method = EnterpriseController.class.getMethod("create", com.saloria.dto.EnterpriseRequest.class);
    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

    assertNotNull(preAuthorize);
    assertEquals("hasRole('SUPER_ADMIN')", preAuthorize.value());
  }
}
