package ca.tangerine.pce.application.mapping;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.tangerine.pce.application.util.JacksonUtil;
import ca.tangerine.pce.model.domain.calculation.allocation.CountryAllocation;
import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.enumeration.Country;

/**
 * Resolves a {@link Country} to its {@link CountryRegionType} bucket using the {@code country-allocation-mapping.json}
 * reference table. This is a dependency-free leaf component so both {@link CountryAllocationMappingService} and
 * response mappers can share the lookup without introducing a mapper-to-service dependency.
 */
@Component
public class CountryRegionResolver {

  private static final String COUNTRY_ALLOCATION_MAPPING_PATH = "/jsons/country-allocation-mapping.json";

  // pre-loaded country allocations mapping: country id (ISO alpha-3) or country name -> mapping entry
  private final Map<String, CountryAllocation> countryAllocationMap;

  public CountryRegionResolver() {
    this.countryAllocationMap = initCountryAllocationMapping();
  }

  /**
   * Resolves the {@link CountryRegionType} bucket for a country via the mapping table, or {@code null} when the country
   * is {@code null}, unknown, or has no mapped region.
   *
   * @param country
   *          country
   * @return mapped region, or {@code null} when unmapped
   */
  public CountryRegionType regionOf(Country country) {
    if (country == null) {
      return null;
    }
    final CountryAllocation allocation = countryAllocationMap.get(country.getAlpha3Code());
    return allocation == null ? null : allocation.getRegion();
  }

  public Map<String, CountryAllocation> initCountryAllocationMapping() {
    final InputStream in = getCountryAllocationInputStream();
    if (in == null) {
      throw ErrorCode.INTERNAL_SERVER_ERROR.toException(
          String.format("Country Allocation Mapping is missing from path %s", COUNTRY_ALLOCATION_MAPPING_PATH));
    }
    final List<CountryAllocation> list = JacksonUtil.deserialize(in, new TypeReference<>() {});
    Map<String, CountryAllocation> map = new HashMap<>();
    list.stream().filter(e -> e.getRegion() != null).forEach(e -> {
      map.put(e.getCountryId(), e);
      if (e.getCountryName() != null) {
        map.put(e.getCountryName(), e);
      }
    });
    return map;
  }

  public InputStream getCountryAllocationInputStream() {
    return this.getClass().getResourceAsStream(COUNTRY_ALLOCATION_MAPPING_PATH);
  }
}
