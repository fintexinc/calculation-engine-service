package ca.tangerine.pce.application.calculation.service.allocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.model.error.ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE;
import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.application.calculation.service.FxRateService;
import ca.tangerine.pce.application.calculation.service.HoldingCurrencyConverter;
import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.config.FxProperties;
import ca.tangerine.pce.application.mapping.CountryAllocationMappingService;
import ca.tangerine.pce.application.mapping.CountryRegionResolver;
import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.domain.calculation.exposure.CountryExposure;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

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
    var bond = holding(new SecurityIdentifier("XBB", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(bond,
        exposure(Map.of(Country.CANADA, new BigDecimal("0.7"), Country.USA, new BigDecimal("0.3"))));

    var result = service.perform(command(bond), data);

    assertThat(result.getCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.7");
    assertThat(result.getCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.3");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldBucketSupranationalIntoOther_whenNonCountryExposurePresent() {
    var bond = holding(new SecurityIdentifier("XBB", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(bond, exposure(Map.of(
        Country.CANADA, new BigDecimal("0.6"),
        Country.USA, new BigDecimal("0.2"),
        Country.SUPRANATIONAL, new BigDecimal("0.2"))));

    var result = service.perform(command(bond), data);

    assertThat(result.getCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.6");
    assertThat(result.getCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.2");
    assertThat(result.getCountryExposure().get(CountryRegionType.OTHER)).isEqualByComparingTo("0.2");
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * TMI-552: unified with the other breakdowns, fixed-income country exposure now normalizes to 100% (previously it
   * surfaced the raw weighted values without rescaling).
   */
  @Test
  void shouldRescaleToOneHundredPercent_whenAllocationsDoNotSumToOne() {
    var bond = holding(new SecurityIdentifier("XBB", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(bond, exposure(Map.of(Country.CANADA, new BigDecimal("0.5"))));

    var result = service.perform(command(bond), data);

    assertThat(result.getCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("1");
  }

  @Test
  void shouldWarnAndReturnAllNullBuckets_whenDataMissing() {
    var bond = holding(new SecurityIdentifier("XBB", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");

    var result = service.perform(command(bond), Map.of());

    assertThat(result.getCountryExposure().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).extracting("code").containsExactly(MISSING_BOND_COUNTRY_EXPOSURE.getCode());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static CountryExposure exposure(Map<Country, BigDecimal> allocations) {
    return CountryExposure.builder().allocations(allocations).build();
  }
}
