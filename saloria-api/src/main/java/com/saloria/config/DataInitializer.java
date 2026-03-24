package com.saloria.config;

import com.saloria.model.Role;
import com.saloria.model.User;
import com.saloria.repository.UserRepository;
import com.saloria.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final StorageService storageService;

  @org.springframework.beans.factory.annotation.Value("${app.bootstrap.super-admin.enabled:false}")
  private boolean bootstrapSuperAdminEnabled;

  @org.springframework.beans.factory.annotation.Value("${app.bootstrap.super-admin.email:}")
  private String bootstrapSuperAdminEmail;

  @org.springframework.beans.factory.annotation.Value("${app.bootstrap.super-admin.password:}")
  private String bootstrapSuperAdminPassword;

  @Override
  public void run(String... args) throws Exception {
    storageService.init();
    if (!bootstrapSuperAdminEnabled) {
      log.info("Super admin bootstrap disabled");
      return;
    }

    if (!StringUtils.hasText(bootstrapSuperAdminEmail) || !StringUtils.hasText(bootstrapSuperAdminPassword)) {
      log.warn("Super admin bootstrap requested but email/password are missing");
      return;
    }

    if (userRepository.findByEmail(bootstrapSuperAdminEmail).isEmpty()) {
      User superAdmin = User.builder()
          .name("Bootstrap Super Admin")
          .email(bootstrapSuperAdminEmail)
          .password(passwordEncoder.encode(bootstrapSuperAdminPassword))
          .role(Role.SUPER_ADMIN)
          .build();

      userRepository.save(superAdmin);
      log.info("Bootstrap super admin created: {}", bootstrapSuperAdminEmail);
    }
  }
}
