package ca.tangerine.pce.application.calculation.service.allocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.model.error.ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE;
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
import ca.tangerine.pce.model.domain.calculation.allocation.EquityCountryAllocation;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class EquityCountryExposureServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final EquityCountryExposureService service = new EquityCountryExposureService(
      new PortfolioWeightCalculator(new HoldingCurrencyConverter(fxRateService, new FxProperties())),
      new CountryAllocationMappingService(new CountryRegionResolver()));

  @BeforeEach
  void setUp() {
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());
  }

  @Test
  void shouldAggregateCountryRegions_whenHoldingHasAllocation() {
    var fund = holding(new SecurityIdentifier("RBF605", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund,
        alloc(Map.of(Country.CANADA, new BigDecimal("0.6"), Country.USA, new BigDecimal("0.4"))));

    var result = service.perform(command(fund), data);

    assertThat(result.getEquityCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.6");
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.4");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldRescaleSurvivorsToOneHundredPercent_whenAllocationsDoNotSumToOne() {
    var fund = holding(new SecurityIdentifier("RBF605", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(fund,
        alloc(Map.of(Country.CANADA, new BigDecimal("0.3"), Country.USA, new BigDecimal("0.1"))));

    var result = service.perform(command(fund), data);

    // survivors rescaled to 100%: 0.3/0.4 and 0.1/0.4
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.75");
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.25");
  }

  @Test
  void shouldBlendByValue_acrossHoldings() {
    var canFund = holding(new SecurityIdentifier("CAN1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var usFund = holding(new SecurityIdentifier("US1", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");
    var data = Map.of(
        canFund, alloc(Map.of(Country.CANADA, BigDecimal.ONE)),
        usFund, alloc(Map.of(Country.USA, BigDecimal.ONE)));

    var result = service.perform(command(canFund, usFund), data);

    assertThat(result.getEquityCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.5");
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.5");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWarnAndReturnAllNullBuckets_whenDataMissing() {
    var fund = holding(new SecurityIdentifier("RBF605", FiIdentifierType.TICKER),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "100");

    var result = service.perform(command(fund), Map.of());

    assertThat(result.getEquityCountryExposure().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).extracting("code").containsExactly(MISSING_EQUITY_COUNTRY_EXPOSURE.getCode());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static EquityCountryAllocation alloc(Map<Country, BigDecimal> allocations) {
    return EquityCountryAllocation.builder().allocations(allocations).build();
  }
}
