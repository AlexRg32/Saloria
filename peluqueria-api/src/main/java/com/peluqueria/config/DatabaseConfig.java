package com.peluqueria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

  @Value("${spring.datasource.url:jdbc:postgresql://localhost:5433/peluqueria_db}")
  private String datasourceUrl;

  @Value("${spring.datasource.username:postgres}")
  private String username;

  @Value("${spring.datasource.password:postgres}")
  private String password;

  @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
  private String driverClassName;

  @Bean
  @Primary
  public DataSource dataSource() {
    String workingUrl = datasourceUrl;
    String user = username;
    String pass = password;

    // Use a flag to check if we should parse as URI
    boolean isUriStyle = (workingUrl.startsWith("jdbc:postgresql://") && workingUrl.contains("@")) ||
        workingUrl.startsWith("postgresql://") ||
        workingUrl.startsWith("postgres://");

    if (isUriStyle) {
      try {
        logger.info("Detected URI-style connection URL. Re-parsing for robustness.");

        // Strip jdbc: prefix if present for URI class processing
        String uriToParse = workingUrl.startsWith("jdbc:") ? workingUrl.substring(5) : workingUrl;
        URI uri = new URI(uriToParse);
        String host = uri.getHost();

        if (host != null && host.endsWith("supabase.co") && !host.contains("pooler")) {
          logger.warn(
              "DIRECT SUPABASE HOST DETECTED: [{}]. This often causes connectivity issues (UnknownHostException) in some cloud environments like Render (due to IPv6-only records). Consider using the Transaction Pooler host instead: [*.pooler.supabase.com].",
              host);
        }

        int port = uri.getPort();
        String path = uri.getPath();
        String query = uri.getQuery();
        String userInfo = uri.getUserInfo();

        // Reconstruct JDBC URL preserving path and query parameters (CRITICAL for
        // Supabase SSL)
        StringBuilder jdbcUrlBuilder = new StringBuilder("jdbc:postgresql://");
        jdbcUrlBuilder.append(host != null ? host : "localhost");
        if (port != -1) {
          jdbcUrlBuilder.append(":").append(port);
        }
        if (path != null) {
          jdbcUrlBuilder.append(path);
        }

        // Clean user/password from query params if credentials come from userInfo
        String cleanQuery = query;
        if (userInfo != null && cleanQuery != null) {
          cleanQuery = java.util.Arrays.stream(cleanQuery.split("&"))
              .filter(p -> !p.startsWith("user=") && !p.startsWith("password="))
              .reduce((a, b) -> a + "&" + b)
              .orElse(null);
        }
        if (cleanQuery != null && !cleanQuery.isEmpty()) {
          jdbcUrlBuilder.append("?").append(cleanQuery);
        }
        workingUrl = jdbcUrlBuilder.toString();

        if (userInfo != null && userInfo.contains(":")) {
          String[] parts = userInfo.split(":", 2);
          user = parts[0];
          pass = parts[1];
          logger.info("Credentials extracted from URL.");
        }
      } catch (URISyntaxException | IndexOutOfBoundsException e) {
        logger.warn("Failed to parse URI-style URL [{}]. Using default logic. Error: {}", workingUrl, e.getMessage());
      }
    } else if (!workingUrl.startsWith("jdbc:")) {
      // Basic prefix normalization if it's a simple postgres://host/db without creds
      workingUrl = "jdbc:" + workingUrl;
    }

    // Security: Mask password in logs
    String maskedUrl = workingUrl.replaceAll(":([^/@?]+)@", ":****@")
        .replaceAll("password=[^&]*", "password=****");
    logger.info("Final DataSource URL: {}", maskedUrl);

    return DataSourceBuilder.create()
        .url(workingUrl)
        .username(user)
        .password(pass)
        .driverClassName(driverClassName)
        .build();
  }
}
