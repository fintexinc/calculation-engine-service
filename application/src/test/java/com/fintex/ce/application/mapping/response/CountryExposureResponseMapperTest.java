package com.fintex.ce.application.mapping.response;

import com.fintex.ce.application.mapping.CountryRegionResolver;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountryExposureResponseMapperTest {

  private final CountryRegionResolver countryRegionResolver = mock(CountryRegionResolver.class);
  private final CountryExposureResponseMapper mapper = new CountryExposureResponseMapper(countryRegionResolver);

  @Test
  void shouldReturnDefaultMap_whenDomainIsNull() {
    CountryExposureResult result = mapper.toResponse((CountryExposure) null);

    assertEquals(CountryRegionType.values().length, result.getCountryExposure().size());
    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getCountryExposure().get(CountryRegionType.CANADA));
  }

  @Test
  void shouldMapKnownCountriesAndIgnoreUnmappedOnes_whenMappingFromDomain() {
    when(countryRegionResolver.regionOf(Country.CANADA)).thenReturn(CountryRegionType.CANADA);
    when(countryRegionResolver.regionOf(Country.USA)).thenReturn(CountryRegionType.UNITED_STATES);
    // Country.UNITED_KINGDOM is left unstubbed -> regionOf returns null and the entry is ignored.

    CountryExposure domain = new CountryExposure();
    domain.setAllocations(Map.of(
        Country.CANADA, new BigDecimal("0.12345678901"),
        Country.USA, new BigDecimal("0.2"),
        Country.UNITED_KINGDOM, new BigDecimal("0.9")));

    CountryExposureResult result = mapper.toResponse(domain);

    assertEquals(2, result.getCountryExposure().size());
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.CANADA).compareTo(new BigDecimal(
        "0.1234567890")));
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.UNITED_STATES).compareTo(new BigDecimal(
        "0.2000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());

    CountryExposureResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(CountryRegionType.values().length, result.getCountryExposure().size());
    assertNull(result.getCountryExposure().get(CountryRegionType.EMERGING_MARKET));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());
    Map<CountryRegionType, BigDecimal> netProducts = Map.of(
        CountryRegionType.CANADA, new BigDecimal("0.1"),
        CountryRegionType.UNITED_STATES, new BigDecimal("0.2"));

    CountryExposureResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.CANADA).compareTo(new BigDecimal(
        "0.1000000000")));
    assertEquals(0, result.getCountryExposure().get(CountryRegionType.UNITED_STATES).compareTo(new BigDecimal(
        "0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}
