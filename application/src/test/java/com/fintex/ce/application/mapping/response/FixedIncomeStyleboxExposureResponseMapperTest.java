package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeStyleboxExposureResult;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedIncomeStyleboxExposureResponseMapperTest {

  private final FixedIncomeStyleboxExposureResponseMapper mapper = new FixedIncomeStyleboxExposureResponseMapper();

  @Test
  void shouldReturnDefaultMap_whenDomainIsNull() {
    FixedIncomeStyleboxExposureResult result = mapper.toResponse(null);

    assertEquals(FixedIncomeStyleBoxType.values().length, result.getFixedIncomeStyleboxExposure().size());
    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getFixedIncomeStyleboxExposure().get(FixedIncomeStyleBoxType.HIGH_LIMITED));
  }

  @Test
  void shouldMapEnumKeys_whenMappingFromDomain() {
    FixedIncomeStyleboxExposure domain = new FixedIncomeStyleboxExposure();
    domain.setBoxValues(Map.of(
        FixedIncomeStyleBoxType.HIGH_LIMITED, new BigDecimal("0.12345678901"),
        FixedIncomeStyleBoxType.LOW_EXTENSIVE, new BigDecimal("0.2")));

    FixedIncomeStyleboxExposureResult result = mapper.toResponse(domain);

    assertEquals(2, result.getFixedIncomeStyleboxExposure().size());
    assertEquals(0, result.getFixedIncomeStyleboxExposure().get(FixedIncomeStyleBoxType.HIGH_LIMITED).compareTo(
        new BigDecimal("0.1234567890")));
    assertEquals(0, result.getFixedIncomeStyleboxExposure().get(FixedIncomeStyleBoxType.LOW_EXTENSIVE).compareTo(
        new BigDecimal("0.2000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());

    FixedIncomeStyleboxExposureResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(FixedIncomeStyleBoxType.values().length, result.getFixedIncomeStyleboxExposure().size());
    assertNull(result.getFixedIncomeStyleboxExposure().get(FixedIncomeStyleBoxType.MEDIUM_MODERATE));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());
    Map<FixedIncomeStyleBoxType, BigDecimal> netProducts = Map.of(
        FixedIncomeStyleBoxType.HIGH_LIMITED, new BigDecimal("0.1"),
        FixedIncomeStyleBoxType.LOW_EXTENSIVE, new BigDecimal("0.2"));

    FixedIncomeStyleboxExposureResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getFixedIncomeStyleboxExposure().get(FixedIncomeStyleBoxType.HIGH_LIMITED).compareTo(
        new BigDecimal("0.1000000000")));
    assertEquals(0, result.getFixedIncomeStyleboxExposure().get(FixedIncomeStyleBoxType.LOW_EXTENSIVE).compareTo(
        new BigDecimal("0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}
