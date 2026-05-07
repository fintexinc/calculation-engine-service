package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.wm.commons.domain.rating.StyleBoxType;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquityStyleboxExposureResponseMapperTest {

  private final EquityStyleboxExposureResponseMapper mapper = new EquityStyleboxExposureResponseMapper();

  @Test
  void shouldReturnDefaultMap_whenDomainIsNull() {
    EquityStyleboxExposureResult result = mapper.toResponse(null);

    assertEquals(StyleBoxType.values().length, result.getEquityStyleboxExposure().size());
    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getEquityStyleboxExposure().get(StyleBoxType.LARGE_CORE));
  }

  @Test
  void shouldMapAndScaleValues_whenMappingFromDomain() {
    EquityStyleboxExposure domain = new EquityStyleboxExposure();
    domain.setBoxValues(Map.of(
        StyleBoxType.LARGE_CORE, new BigDecimal("0.12345678901"),
        StyleBoxType.SMALL_VALUE, new BigDecimal("0.2")));

    EquityStyleboxExposureResult result = mapper.toResponse(domain);

    assertEquals(2, result.getEquityStyleboxExposure().size());
    assertEquals(0, result.getEquityStyleboxExposure().get(StyleBoxType.LARGE_CORE).compareTo(new BigDecimal(
        "0.1234567890")));
    assertEquals(0, result.getEquityStyleboxExposure().get(StyleBoxType.SMALL_VALUE).compareTo(new BigDecimal(
        "0.2000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());

    EquityStyleboxExposureResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(StyleBoxType.values().length, result.getEquityStyleboxExposure().size());
    assertNull(result.getEquityStyleboxExposure().get(StyleBoxType.MID_CORE));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());
    Map<StyleBoxType, BigDecimal> netProducts = Map.of(
        StyleBoxType.LARGE_CORE, new BigDecimal("0.1"),
        StyleBoxType.SMALL_VALUE, new BigDecimal("0.2"));

    EquityStyleboxExposureResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getEquityStyleboxExposure().get(StyleBoxType.LARGE_CORE).compareTo(new BigDecimal(
        "0.1000000000")));
    assertEquals(0, result.getEquityStyleboxExposure().get(StyleBoxType.SMALL_VALUE).compareTo(new BigDecimal(
        "0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}
