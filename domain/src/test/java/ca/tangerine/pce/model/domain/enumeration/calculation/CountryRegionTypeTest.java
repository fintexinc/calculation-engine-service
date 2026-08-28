package ca.tangerine.pce.model.domain.enumeration.calculation;

import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType.CANADA;
import static ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType.INTERNATIONAL_DEVELOPED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryRegionTypeTest {

  @Test
  void of_checkResult() {
    // SETUP
    final CountryRegionType expected = CANADA;

    // ACT
    final CountryRegionType actual = CountryRegionType.fromValue(expected.name());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void of_checkResult2() {
    // SETUP
    final String region = CANADA.getRegion();

    // ACT
    final CountryRegionType actual = CountryRegionType.fromValue(region);

    // VERIFY
    assertEquals(CANADA, actual);
  }

  @Test
  void of_checkResult3() {
    // SETUP
    final String region = "United StatesS";

    // VERIFY
    Assertions.assertThrows(CalculationException.class, () -> {
      CountryRegionType.fromValue(region);
    });
  }

  @Test
  void of_checkResult4() {
    // SETUP
    final String region = "IntErnAtiOnaL-DevEloPeD";

    // ACT
    final CountryRegionType actual = CountryRegionType.fromValue(region);

    // VERIFY
    assertEquals(INTERNATIONAL_DEVELOPED, actual);
  }
}