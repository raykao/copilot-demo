package com.contoso.storefront;

import com.contoso.storefront.store.InMemoryStore;
import com.contoso.storefront.store.SeedData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/** Builds fresh stores from the shared seed file for unit tests. */
public final class TestStores {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .findAndRegisterModules()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private TestStores() {}

  /**
   * Creates a store loaded from {@code ../data/seed.json}.
   *
   * @return a new, independent store
   */
  public static InMemoryStore seeded() {
    try {
      return new InMemoryStore(
          MAPPER.readValue(Path.of("../data/seed.json").toFile(), SeedData.class));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
