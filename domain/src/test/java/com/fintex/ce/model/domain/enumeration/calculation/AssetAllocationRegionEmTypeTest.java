package com.fintex.ce.model.domain.enumeration.calculation;

import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegionEmType;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetAllocationRegionEmTypeTest {

  @Test
  void of_verify() {

    // ACT
    AssetAllocationRegionEmType actual = AssetAllocationRegionEmType.fromValue(
        AssetAllocationRegionEmType.CANADIAN_EQUITY
            .name());

    // VERIFY
    assertEquals(AssetAllocationRegionEmType.CANADIAN_EQUITY, actual);
  }

  @Test
  void of_verify2() {

    // ACT
    AssetAllocationRegionEmType actual = AssetAllocationRegionEmType.fromValue(
        AssetAllocationRegionEmType.CANADIAN_EQUITY
            .getRegion());

    // VERIFY
    assertEquals(AssetAllocationRegionEmType.CANADIAN_EQUITY, actual);
  }

  @Test
  void of_verify3() {

    // ACT
    String region = AssetAllocationRegionEmType.CANADIAN_EQUITY.getRegion() + 1;
    assertThrows(CalculationException.class, () -> AssetAllocationRegionEmType.fromValue(region));
  }

}