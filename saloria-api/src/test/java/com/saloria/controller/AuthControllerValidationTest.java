package com.saloria.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saloria.exception.GlobalExceptionHandler;
import com.saloria.service.AuthenticationService;

public class AuthControllerValidationTest {

  private MockMvc mockMvc;
  private AuthenticationService authenticationService;

  @BeforeEach
  void setUp() {
    authenticationService = Mockito.mock(AuthenticationService.class);
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authenticationService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
        .build();
  }

  @Test
  void loginRejectsInvalidPayload() throws Exception {
    String payload = """
        {
          "email": "correo-invalido",
          "password": "",
          "requiredRole": "SUPER_ADMIN"
        }
        """;

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Revisa los campos marcados e inténtalo de nuevo."))
        .andExpect(jsonPath("$.fieldErrors.email").value("El email no es válido"))
        .andExpect(jsonPath("$.fieldErrors.password").value("La contraseña es obligatoria"))
        .andExpect(jsonPath("$.fieldErrors.requiredRole").value("El portal solicitado no es válido"));

    verifyNoInteractions(authenticationService);
  }

  @Test
  void registerReturnsConflictWhenEnterpriseAlreadyExists() throws Exception {
    String payload = """
        {
          "name": "Lucia",
          "email": "lucia@example.com",
          "password": "secret123",
          "enterpriseName": "Salon Norte"
        }
        """;

    when(authenticationService.register(Mockito.any()))
        .thenThrow(new IllegalStateException("Ya existe una empresa con ese nombre. Contacta con soporte si necesitas acceso."));

    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message")
            .value("Ya existe una empresa con ese nombre. Contacta con soporte si necesitas acceso."));
  }
}
