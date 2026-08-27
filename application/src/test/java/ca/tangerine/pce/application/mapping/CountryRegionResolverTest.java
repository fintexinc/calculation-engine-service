package ca.tangerine.pce.application.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.model.domain.calculation.allocation.CountryAllocation;
import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.wm.commons.domain.enumeration.Country;

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

  /**
   * Coverage guard: every {@link Country} constant must map to a region bucket. A country that resolves in commons but
   * has no non-null region row here is silently dropped during region bucketing and its exposure renormalized away, so
   * a new commons constant (or a mapping row left with a null region) must fail the build rather than leak exposure.
   */
  @Test
  void shouldResolveRegion_forEveryCountryConstant() {
    List<String> unmapped = Arrays.stream(Country.values())
        .filter(country -> resolver.regionOf(country) == null)
        .map(Country::getAlpha3Code)
        .sorted()
        .toList();

    assertThat(unmapped)
        .as("Country constants without a region in country-allocation-mapping.json: %s", unmapped)
        .isEmpty();
  }

  /**
   * End-to-end guard for the Morningstar consolidated country-exposure labels: each vendor display name must resolve to
   * a {@link Country} (via the commons alias set) and then bucket into the expected region. Protects against the
   * ~50-80% exposure loss caused when these labels fell through as {@code type = null}.
   */
  @ParameterizedTest
  @CsvSource({
      "United States, UNITED_STATES",
      "United Kingdom, INTERNATIONAL_DEVELOPED",
      "Hong Kong, INTERNATIONAL_DEVELOPED",
      "New Zealand, INTERNATIONAL_DEVELOPED",
      "United Arab Emirates, EMERGING_MARKET",
      "Puerto Rico, INTERNATIONAL_DEVELOPED",
      "Cayman Islands, INTERNATIONAL_DEVELOPED",
      "South Korea, EMERGING_MARKET",
      "South Africa, EMERGING_MARKET",
      "Saudi Arabia, EMERGING_MARKET",
      "Marshall Islands, EMERGING_MARKET",
      "Czech Republic, EMERGING_MARKET",
      "Czechia, EMERGING_MARKET",
      "Supranational, OTHER"
  })
  void shouldResolveAndBucket_forMorningstarDisplayNames(String vendorLabel, CountryRegionType expected) {
    Country country = Country.fromCodeOrString(vendorLabel);
    assertThat(country).as("commons should resolve vendor label '%s'", vendorLabel).isNotNull();
    assertThat(resolver.regionOf(country))
        .as("region bucket for '%s'", vendorLabel)
        .isEqualTo(expected);
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
