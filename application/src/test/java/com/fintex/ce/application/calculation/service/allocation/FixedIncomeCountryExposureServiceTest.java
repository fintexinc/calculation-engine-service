package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.mapping.CountryRegionResolver;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.error.ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FixedIncomeCountryExposureServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final FixedIncomeCountryExposureService service = new FixedIncomeCountryExposureService(
      new PortfolioWeightCalculator(new HoldingCurrencyConverter(fxRateService, new FxProperties())),
      new CountryAllocationMappingService(new CountryRegionResolver()));

  @BeforeEach
  void setUp() {
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());
  }

  @Test
  void shouldAggregateCountryRegions_whenHoldingHasAllocation() {
    var bond = bond("XBB", "100");
    var data = Map.of(bond,
        exposure(Map.of(Country.CANADA, new BigDecimal("0.7"), Country.USA, new BigDecimal("0.3"))));

    var result = service.perform(command(bond), data);

    assertThat(result.getCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.7");
    assertThat(result.getCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.3");
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * TMI-552: unified with the other breakdowns, fixed-income country exposure now normalizes to 100% (previously it
   * surfaced the raw weighted values without rescaling).
   */
  @Test
  void shouldRescaleToOneHundredPercent_whenAllocationsDoNotSumToOne() {
    var bond = bond("XBB", "100");
    var data = Map.of(bond, exposure(Map.of(Country.CANADA, new BigDecimal("0.5"))));

    var result = service.perform(command(bond), data);

    assertThat(result.getCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("1");
  }

  @Test
  void shouldWarnAndReturnAllNullBuckets_whenDataMissing() {
    var bond = bond("XBB", "100");

    var result = service.perform(command(bond), Map.of());

    assertThat(result.getCountryExposure().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).extracting("code").containsExactly(MISSING_BOND_COUNTRY_EXPOSURE.getCode());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static PortfolioHolding bond(String id, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(FinancialInstrumentType.MUTUAL_FUND)
        .country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .build();
  }

  private static CountryExposure exposure(Map<Country, BigDecimal> allocations) {
    return CountryExposure.builder().allocations(allocations).build();
  }
}
