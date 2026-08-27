package ca.tangerine.pce.webclient.mic.mapper;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.allocation.EquityCountryAllocation;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocation;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocationValue;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class EquityCountryAllocationMapperTest {

  private final EquityCountryAllocationMapper mapper = new EquityCountryAllocationMapper();

  @Test
  void shouldMapAllocationsAndProvider_whenResponseHasCountryAllocationValues() {
    var canada = new CountryAllocationValue();
    canada.setType(Country.CANADA);
    canada.setValue(BigDecimal.valueOf(0.65));

    var usa = new CountryAllocationValue();
    usa.setType(Country.USA);
    usa.setValue(BigDecimal.valueOf(0.35));

    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of(canada, usa));
    micResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    EquityCountryAllocation result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-001", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getAllocations()).hasSize(2);
    assertThat(result.getAllocations()).containsEntry(Country.CANADA, BigDecimal.valueOf(0.65));
    assertThat(result.getAllocations()).containsEntry(Country.USA, BigDecimal.valueOf(0.35));
  }

  @Test
  void shouldReturnEmptyAllocations_whenResponseIsNull() {
    EquityCountryAllocation result = mapper.map(null, holding(new SecurityIdentifier("SEC-002", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getAllocations()).isEmpty();
  }

  @Test
  void shouldReturnEmptyAllocations_whenAllocationListIsNull() {
    var micResponse = new CountryAllocation();
    micResponse.setAllocations(null);

    EquityCountryAllocation result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-003", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var micResponse = new CountryAllocation();
    micResponse.setAllocations(List.of());
    micResponse.setDataProviders(null);

    EquityCountryAllocation result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-004", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
  }

}