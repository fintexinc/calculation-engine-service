package com.fintex.ce.domain.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataProviderTest {

  @Test
  void of_shouldReturnDataProvider_whenValidStringProvided() {
    // ACT
    final DataProvider actual = DataProvider.of("EAGLE");

    // VERIFY
    assertEquals(DataProvider.EAGLE, actual);
  }

  @Test
  void of_shouldReturnDataProvider_whenCaseInsensitiveStringProvided() {
    // ACT
    final DataProvider actual = DataProvider.of("morningstar");

    // VERIFY
    assertEquals(DataProvider.MORNINGSTAR, actual);
  }

  @Test
  void of_shouldReturnNull_whenInvalidStringProvided() {
    // ACT
    final DataProvider actual = DataProvider.of("INVALID_PROVIDER");

    // VERIFY
    assertNull(actual);
  }

  @Test
  void of_shouldReturnNull_whenNullProvided() {
    // ACT
    final DataProvider actual = DataProvider.of(null);

    // VERIFY
    assertNull(actual);
  }

}