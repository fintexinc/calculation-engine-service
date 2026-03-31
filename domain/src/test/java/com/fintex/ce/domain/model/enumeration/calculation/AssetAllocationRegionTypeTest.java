package com.fintex.ce.domain.model.enumeration.calculation;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegionType.CASH;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegionType.US_EQUITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AssetAllocationRegionTypeTest {

  @Test
  void of_checkResult() {
    // SETUP
    final AssetAllocationRegionType expected = AssetAllocationRegionType.FIXED_INCOME;

    // ACT
    final AssetAllocationRegionType actual = AssetAllocationRegionType.fromValue(expected.name());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void of_checkResult2() {
    // SETUP
    final String region = CASH.getRegion();

    // ACT
    final AssetAllocationRegionType actual = AssetAllocationRegionType.fromValue(region);

    // VERIFY
    assertEquals(CASH, actual);
  }

  @Test
  void of_checkResult3() {
    // SETUP
    final String region = "Cashh";

    // VERIFY
    Assertions.assertThrows(SystemException.class, () -> {
      AssetAllocationRegionType.fromValue(region);
    });
  }

  @Test
  void of_checkResult4() {
    // SETUP
    final String region = "Us eQuItY";

    // ACT
    final AssetAllocationRegionType actual = AssetAllocationRegionType.fromValue(region);

    // VERIFY
    assertEquals(US_EQUITY, actual);
  }
}