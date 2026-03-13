package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.input.result.EquityStyleboxExposureResult;
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

    assertEquals(EquityStyleboxType.values().length, result.getEquityStyleboxExposure().size());
    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getEquityStyleboxExposure().get(EquityStyleboxType.LARGE_CORE));
  }

  @Test
  void shouldMapKnownKeysAndIgnoreUnknownKeys_whenMappingFromDomain() {
    EquityStyleboxExposure domain = new EquityStyleboxExposure();
    domain.setBoxValues(Map.of(
        "LARGE_CORE", new BigDecimal("0.12345678901"),
        "SMALL_VALUE", new BigDecimal("0.2"),
        "UNKNOWN_KEY", new BigDecimal("0.9")
    ));

    EquityStyleboxExposureResult result = mapper.toResponse(domain);

    assertEquals(2, result.getEquityStyleboxExposure().size());
    assertEquals(0, result.getEquityStyleboxExposure().get(EquityStyleboxType.LARGE_CORE).compareTo(new BigDecimal("0.1234567890")));
    assertEquals(0, result.getEquityStyleboxExposure().get(EquityStyleboxType.SMALL_VALUE).compareTo(new BigDecimal("0.2000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));

    EquityStyleboxExposureResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(EquityStyleboxType.values().length, result.getEquityStyleboxExposure().size());
    assertNull(result.getEquityStyleboxExposure().get(EquityStyleboxType.MID_CORE));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));
    Map<EquityStyleboxType, BigDecimal> netProducts = Map.of(
        EquityStyleboxType.LARGE_CORE, new BigDecimal("0.1"),
        EquityStyleboxType.SMALL_VALUE, new BigDecimal("0.2")
    );

    EquityStyleboxExposureResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getEquityStyleboxExposure().get(EquityStyleboxType.LARGE_CORE).compareTo(new BigDecimal("0.1000000000")));
    assertEquals(0, result.getEquityStyleboxExposure().get(EquityStyleboxType.SMALL_VALUE).compareTo(new BigDecimal("0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}

