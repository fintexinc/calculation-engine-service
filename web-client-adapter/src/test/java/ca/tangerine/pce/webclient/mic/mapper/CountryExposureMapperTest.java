package ca.tangerine.pce.webclient.mic.mapper;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.exposure.CountryExposure;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocation;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocationValue;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class CountryExposureMapperTest {

  private final CountryExposureMapper mapper = new CountryExposureMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasCountryAllocationValues() {
    var canada = createCountryAllocationValue(Country.CANADA, "0.65");
    var usa = createCountryAllocationValue(Country.USA, "0.35");

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(canada, usa));
    micResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    CountryExposure result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-001", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry(Country.CANADA, BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry(Country.USA, BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    CountryExposure result = mapper.map(null, holding(new SecurityIdentifier("SEC-002", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var micResponse = new CountryAllocation();
    micResponse.setAllocations(null);

    CountryExposure result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-003", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of());
    micResponse.setDataProviders(null);

    CountryExposure result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-004", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldFilterOutEntriesWithNullIsoCodeOrValue() {
    var valid = createCountryAllocationValue(Country.CANADA, "0.65");

    var nullIso = new CountryAllocationValue();
    nullIso.setType(null);
    nullIso.setValue(BigDecimal.valueOf(0.20));

    var nullValue = new CountryAllocationValue();
    nullValue.setType(Country.UNITED_KINGDOM);
    nullValue.setValue(null);

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(valid, nullIso, nullValue));

    CountryExposure result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-005", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations()).containsEntry(Country.CANADA, BigDecimal.valueOf(0.65));
  }

  @Test
  void shouldSumValues_whenDuplicateIsoCodesExist() {
    var can1 = createCountryAllocationValue(Country.CANADA, "0.40");
    var can2 = createCountryAllocationValue(Country.CANADA, "0.25");

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(can1, can2));

    CountryExposure result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-006", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations().get(Country.CANADA)).isEqualByComparingTo("0.65");
  }

  private CountryAllocationValue createCountryAllocationValue(Country country, String value) {
    var cv = new CountryAllocationValue();
    cv.setType(country);
    cv.setValue(new BigDecimal(value));
    return cv;
  }

}
