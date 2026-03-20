package com.fintex.ce.domain.enumeration.calculation;

import com.fintex.ce.domain.model.calculation.EquityMarketCapType;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.SMALL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EquityMarketCapTypeTest {

  @Test
  void of_checkResult() {
    // SETUP
    final EquityMarketCapType expected = EquityMarketCapType.GIANT;

    // ACT
    final EquityMarketCapType actual = EquityMarketCapType.of(expected.name());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void of_checkResult2() {
    // SETUP
    final String type = SMALL.name();

    // ACT
    final EquityMarketCapType actual = EquityMarketCapType.of(type);

    // VERIFY
    assertEquals(SMALL, actual);
  }

  @Test
  void of_checkResult3() {
    // SETUP
    final String region = "Mediumq";

    // ACT
    final EquityMarketCapType actual = EquityMarketCapType.of(region);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void of_checkResult4() {
    // SETUP
    final String region = "SmaLL";

    // ACT
    final EquityMarketCapType actual = EquityMarketCapType.of(region);

    // VERIFY
    assertEquals(SMALL, actual);
  }
}