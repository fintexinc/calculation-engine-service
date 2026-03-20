package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.result.CountryExposureResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountryExposureResponseMapperTest {

  private final CountryExposureResponseMapper mapper = new CountryExposureResponseMapper();

  @Test
  void shouldReturnDefaultMap_whenDomainIsNull() {
    CountryExposureResult result = mapper.toResponse(null);

    assertEquals(CountryRegionType.values().length, result.getCountryExposure().size());
    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getCountryExposure().get(CountryRegionType.CANADA));
  }

  @Test
  void shouldMapKnownKeysAndIgnoreUnknownKeys_whenMappingFromDomain() {
    CountryExposure domain = new CountryExposure();
    domain.setAllocations(Map.of(
        "CANADA", new BigDecimal("0.12345678901"),
        "UNITED_STATES", new BigDecimal("0.2"),
        "UNKNOWN_KEY", new BigDecimal("0.9")
    ));

    CountryExposureResult result = mapper.toResponse(domain);

    assertEquals(2, result.getCountryExposure().size());
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.CANADA).compareTo(new BigDecimal("0.1234567890")));
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.UNITED_STATES).compareTo(new BigDecimal("0.2000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));

    CountryExposureResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(CountryRegionType.values().length, result.getCountryExposure().size());
    assertNull(result.getCountryExposure().get(CountryRegionType.EMERGING_MARKET));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));
    Map<CountryRegionType, BigDecimal> netProducts = Map.of(
        CountryRegionType.CANADA, new BigDecimal("0.1"),
        CountryRegionType.UNITED_STATES, new BigDecimal("0.2")
    );

    CountryExposureResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.CANADA).compareTo(new BigDecimal("0.1000000000")));
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.UNITED_STATES).compareTo(new BigDecimal("0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}

