package com.contoso.storefront;

import com.contoso.storefront.config.StorefrontProperties;
import com.contoso.storefront.store.InMemoryStore;
import com.contoso.storefront.store.SeedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

/** Entry point for the Contoso Outfitters storefront API. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class StorefrontApplication {

  /**
   * Starts the application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(StorefrontApplication.class, args);
  }

  /**
   * Loads the shared seed file into the in-memory store.
   *
   * @param properties storefront settings
   * @param objectMapper Spring's configured Jackson mapper
   * @return the seeded store
   * @throws IOException if the seed file cannot be read
   */
  @Bean
  public InMemoryStore inMemoryStore(StorefrontProperties properties, ObjectMapper objectMapper)
      throws IOException {
    SeedData seed = objectMapper.readValue(Path.of(properties.seedPath()).toFile(), SeedData.class);
    return new InMemoryStore(seed);
  }

  /**
   * System clock, injectable so tests can pin "today".
   *
   * @return the system default clock
   */
  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
