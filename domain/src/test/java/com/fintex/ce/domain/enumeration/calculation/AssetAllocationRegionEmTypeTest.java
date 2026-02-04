package com.fintex.ce.domain.enumeration.calculation;

import com.fintex.ce.domain.exception.SystemException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetAllocationRegionEmTypeTest {

  @Test
  void of_verify() {

    // ACT
    AssetAllocationRegionEmType actual = AssetAllocationRegionEmType.of(AssetAllocationRegionEmType.CANADIAN_EQUITY
        .name());

    // VERIFY
    assertEquals(AssetAllocationRegionEmType.CANADIAN_EQUITY, actual);
  }

  @Test
  void of_verify2() {

    // ACT
    AssetAllocationRegionEmType actual = AssetAllocationRegionEmType.of(AssetAllocationRegionEmType.CANADIAN_EQUITY
        .getRegion());

    // VERIFY
    assertEquals(AssetAllocationRegionEmType.CANADIAN_EQUITY, actual);
  }

  @Test
  void of_verify3() {

    // ACT
    String region = AssetAllocationRegionEmType.CANADIAN_EQUITY.getRegion() + 1;
    assertThrows(SystemException.class, () -> AssetAllocationRegionEmType.of(region));
  }

}