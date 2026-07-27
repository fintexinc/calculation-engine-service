package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.mapping.CountryRegionResolver;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
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

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    var fund = fund("RBF605", "100");
    var data = Map.of(fund,
        alloc(Map.of(Country.CANADA, new BigDecimal("0.6"), Country.USA, new BigDecimal("0.4"))));

    var result = service.perform(command(fund), data);

    assertThat(result.getEquityCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.6");
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.4");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldRescaleSurvivorsToOneHundredPercent_whenAllocationsDoNotSumToOne() {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund,
        alloc(Map.of(Country.CANADA, new BigDecimal("0.3"), Country.USA, new BigDecimal("0.1"))));

    var result = service.perform(command(fund), data);

    // survivors rescaled to 100%: 0.3/0.4 and 0.1/0.4
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.CANADA)).isEqualByComparingTo("0.75");
    assertThat(result.getEquityCountryExposure().get(CountryRegionType.UNITED_STATES)).isEqualByComparingTo("0.25");
  }

  @Test
  void shouldBlendByValue_acrossHoldings() {
    var canFund = fund("CAN1", "100");
    var usFund = fund("US1", "100");
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
    var fund = fund("RBF605", "100");

    var result = service.perform(command(fund), Map.of());

    assertThat(result.getEquityCountryExposure().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).extracting("code").containsExactly(MISSING_EQUITY_COUNTRY_EXPOSURE.getCode());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static PortfolioHolding fund(String id, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(FinancialInstrumentType.MUTUAL_FUND_CANADA)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .build();
  }

  private static EquityCountryAllocation alloc(Map<Country, BigDecimal> allocations) {
    return EquityCountryAllocation.builder().allocations(allocations).build();
  }
}
