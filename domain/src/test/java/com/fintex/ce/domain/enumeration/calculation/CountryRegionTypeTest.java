package com.fintex.ce.domain.model.enumeration.calculation;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.calculation.CountryRegionType.CANADA;
import static com.fintex.ce.domain.model.calculation.CountryRegionType.INTERNATIONAL_DEVELOPED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryRegionTypeTest {

  @Test
  void of_checkResult() {
    // SETUP
    final CountryRegionType expected = CANADA;

    // ACT
    final CountryRegionType actual = CountryRegionType.of(expected.name());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void of_checkResult2() {
    // SETUP
    final String region = CANADA.getRegion();

    // ACT
    final CountryRegionType actual = CountryRegionType.of(region);

    // VERIFY
    assertEquals(CANADA, actual);
  }

  @Test
  void of_checkResult3() {
    // SETUP
    final String region = "United StatesS";

    // VERIFY
    Assertions.assertThrows(SystemException.class, () -> {
      CountryRegionType.of(region);
    });
  }

  @Test
  void of_checkResult4() {
    // SETUP
    final String region = "IntErnAtiOnaL-DevEloPeD";

    // ACT
    final CountryRegionType actual = CountryRegionType.of(region);

    // VERIFY
    assertEquals(INTERNATIONAL_DEVELOPED, actual);
  }
}