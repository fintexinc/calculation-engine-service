package com.fintex.ce.domain.model.enumeration.calculation;

import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.CANADIAN_EQUITIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.INTERNATIONAL_EQUITIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssetAllocationRegionTest {

  @Test
  void of_checkResult() {
    // SETUP
    final AssetAllocationRegion expected = AssetAllocationRegion.ASIA_PACIFIC_EQUITIES;

    // ACT
    final AssetAllocationRegion actual = AssetAllocationRegion.fromValue(expected.getName());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void of_checkResult2() {
    // SETUP
    final String region = INTERNATIONAL_EQUITIES.getName();

    // ACT
    final AssetAllocationRegion actual = AssetAllocationRegion.fromValue(region);

    // VERIFY
    assertEquals(INTERNATIONAL_EQUITIES, actual);
  }

  @Test
  void of_checkResult3() {
    // SETUP
    final String region = "OtherrR";

    // ACT
    final AssetAllocationRegion actual = AssetAllocationRegion.fromValue(region);

    // VERIFY
    assertNull(actual);
  }

  @Test
  void of_checkResult4() {
    // SETUP
    final String region = "CaNadIaN EqUitIeS";

    // ACT
    final AssetAllocationRegion actual = AssetAllocationRegion.fromValue(region);

    // VERIFY
    assertEquals(CANADIAN_EQUITIES, actual);
  }
}