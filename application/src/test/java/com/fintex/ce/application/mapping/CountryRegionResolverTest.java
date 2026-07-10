package com.fintex.ce.application.mapping;

import com.fintex.ce.model.domain.calculation.allocation.CountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.enumeration.Country;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountryRegionResolverTest {

  private final CountryRegionResolver resolver = new CountryRegionResolver();

  @Test
  void shouldResolveRegion_forKnownCountry() {
    assertEquals(CountryRegionType.CANADA, resolver.regionOf(Country.CANADA));
    assertEquals(CountryRegionType.UNITED_STATES, resolver.regionOf(Country.USA));
  }

  @Test
  void shouldReturnNull_forNullCountry() {
    assertNull(resolver.regionOf(null));
  }

  @Test
  void shouldInitCountryAllocationMapping_withKnownEntries() {
    Map<String, CountryAllocation> actual = resolver.initCountryAllocationMapping();

    assertFalse(actual.isEmpty());
    assertEquals(CountryRegionType.CANADA, actual.get("CAN").getRegion());
    assertEquals(CountryRegionType.UNITED_STATES, actual.get("USA").getRegion());
  }

  @Test
  void shouldThrow_whenMappingResourceMissing() {
    var spy = mock(CountryRegionResolver.class);
    when(spy.getCountryAllocationInputStream()).thenReturn(null);
    doCallRealMethod().when(spy).initCountryAllocationMapping();

    assertThrows(CalculationException.class, spy::initCountryAllocationMapping);
  }
}
