package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.fee.MerComparisonData;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.MerComparison;
import com.fintex.ce.model.domain.result.fee.MerComparisonResult;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerBenchmarkComparisonServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final MERCalculationServiceImpl merService = new MERCalculationServiceImpl(
      new HoldingCurrencyConverter(fxRateService, new FxProperties()),
      new MerFeeResolver(List.of(new CanadianFeeResolutionStrategy(), new UsFeeResolutionStrategy())));
  private final MerBenchmarkComparisonService service = new MerBenchmarkComparisonService(merService);

  {
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Set<Currency> src = inv.getArgument(0);
      return src.stream().collect(Collectors.toMap(c -> c, c -> BigDecimal.ONE));
    });
  }

  @Test
  void comparesPortfolioMerToBenchmarkMer_perView() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY, WHOLE_PORTFOLIO),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    MerComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getPortfolioMer()).isEqualByComparingTo("0.02");
    assertThat(fundsOnly.getBenchmarkMer()).isEqualByComparingTo("0.01");
    // (0.02 - 0.01) / 0.01 * 100 = 100
    assertThat(fundsOnly.getPercentDifference()).isEqualByComparingTo("100");
    // (0.01 - 0.02) * 100 = -1 (portfolio costs more -> negative)
    assertThat(fundsOnly.getAnnualDollarImpact()).isEqualByComparingTo("-1");
    assertThat(fundsOnly.isEqual()).isFalse();

    assertThat(result.getComparison()).containsKey(WHOLE_PORTFOLIO);
  }

  @Test
  void flagsEqual_whenPortfolioMerIdenticalToBenchmarkMer() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.015")), Map.of(benchmark, fee("0.015"))));

    MerComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.isEqual()).isTrue();
    assertThat(fundsOnly.getPercentDifference()).isEqualByComparingTo("0");
    assertThat(fundsOnly.getAnnualDollarImpact()).isEqualByComparingTo("0");
  }

  @Test
  void positiveDollarImpact_whenPortfolioCheaperThanBenchmark() {
    PortfolioHolding portfolioFund = fund("CIG1101", "200");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, WHOLE_PORTFOLIO),
        data(Map.of(portfolioFund, fee("0.01")), Map.of(benchmark, fee("0.02"))));

    MerComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    // (0.02 - 0.01) * 200 = 2 (portfolio cheaper -> positive)
    assertThat(whole.getAnnualDollarImpact()).isEqualByComparingTo("2");
    // (0.01 - 0.02) / 0.02 * 100 = -50
    assertThat(whole.getPercentDifference()).isEqualByComparingTo("-50");
  }

  @Test
  void scopesDollarImpactBasePerView_whenPortfolioMixesFundsAndNonFundHoldings() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding stock = stock("RY.TO", "300");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(
        command(List.of(portfolioFund, stock), benchmark, FUNDS_ONLY, WHOLE_PORTFOLIO),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    MerComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getPortfolioMer()).isEqualByComparingTo("0.02");
    // funds-only base is the fund value (100) only; the 300 stock is excluded: (0.01 - 0.02) * 100
    assertThat(fundsOnly.getAnnualDollarImpact()).isEqualByComparingTo("-1");

    MerComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    // whole-portfolio MER dilutes the fund across all 400: 0.02 * (100/400) = 0.005
    assertThat(whole.getPortfolioMer()).isEqualByComparingTo("0.005");
    // whole-portfolio base is the full 400: (0.01 - 0.005) * 400
    assertThat(whole.getAnnualDollarImpact()).isEqualByComparingTo("2");
  }

  @Test
  void usesFxConvertedBaseForDollarImpact_whenHoldingCurrencyDiffersFromTargetCurrency() {
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.25"), Currency.CAD, BigDecimal.ONE));

    PortfolioHolding usdFund = fund("TDB952", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(usdFund, benchmark, WHOLE_PORTFOLIO),
        data(Map.of(usdFund, fee("0.02", null, Currency.USD)), Map.of(benchmark, fee("0.01"))));

    MerComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    assertThat(whole.getPortfolioMer()).isEqualByComparingTo("0.02");
    // base is the 100 USD value converted to CAD at 1.25 = 125, not the raw 100: (0.01 - 0.02) * 125
    assertThat(whole.getAnnualDollarImpact()).isEqualByComparingTo("-1.25");
  }

  @Test
  void convertsIntoTheRequestedTargetCurrency_whenTheCommandSuppliesOne() {
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.CAD, new BigDecimal("0.80"), Currency.USD, BigDecimal.ONE));

    PortfolioHolding cadFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    var command = command(cadFund, benchmark, WHOLE_PORTFOLIO);
    command.setTargetCurrency(Currency.USD);

    MerComparisonResult result = service.perform(command,
        data(Map.of(cadFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    // the 100 CAD value is converted to USD at 0.80 = 80, not left at 100: (0.01 - 0.02) * 80
    assertThat(result.getComparison().get(WHOLE_PORTFOLIO).getAnnualDollarImpact()).isEqualByComparingTo("-0.8");
    // both runs — the portfolio's and the benchmark's — convert into the requested currency, not just the portfolio's
    verify(fxRateService, times(2)).spotRates(anySet(), eq(Currency.USD), any());
  }

  @Test
  void weightsBenchmarkMerByHoldingValue_whenBenchmarkHasSeveralHoldings() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding cheap = fund("TDB622", "300");
    PortfolioHolding pricey = fund("RBF556", "100");

    MerComparisonResult result = service.perform(
        command(List.of(portfolioFund), List.of(cheap, pricey), FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(cheap, fee("0.01"), pricey, fee("0.03"))));

    MerComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    // 0.01 * (300/400) + 0.03 * (100/400) = 0.015
    assertThat(fundsOnly.getBenchmarkMer()).isEqualByComparingTo("0.015");
    // (0.015 - 0.02) * 100
    assertThat(fundsOnly.getAnnualDollarImpact()).isEqualByComparingTo("-0.5");
  }

  @Test
  void weightsBenchmarkHoldingsEqually_whenNoBenchmarkHoldingCarriesAValue() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding cheap = fund("TDB622", null);
    PortfolioHolding pricey = fund("RBF556", null);

    MerComparisonResult result = service.perform(
        command(List.of(portfolioFund), List.of(cheap, pricey), FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(cheap, fee("0.01"), pricey, fee("0.03"))));

    // no weights to go on, so the two MERs average evenly: (0.01 + 0.03) / 2
    assertThat(result.getComparison().get(FUNDS_ONLY).getBenchmarkMer()).isEqualByComparingTo("0.02");
  }

  @Test
  void usesTheFundsOwnMer_whenBenchmarkIsASingleFundWithoutAValue() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", null);

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    assertThat(result.getComparison().get(FUNDS_ONLY).getBenchmarkMer()).isEqualByComparingTo("0.01");
  }

  @Test
  void carriesBenchmarkWarnings_whenBenchmarkFallsBackToManagementFee() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee(null, "0.0075"))));

    assertThat(result.getComparison().get(FUNDS_ONLY).getBenchmarkMer()).isEqualByComparingTo("0.0075");
    assertThat(result.getWarnings()).extracting("code").contains("FDS-022");
  }

  private static MerComparisonCommand command(PortfolioHolding portfolioFund, PortfolioHolding benchmark,
      FeeAggregationMode... modes) {
    return command(List.of(portfolioFund), List.of(benchmark), modes);
  }

  private static MerComparisonCommand command(List<PortfolioHolding> holdings, PortfolioHolding benchmark,
      FeeAggregationMode... modes) {
    return command(holdings, List.of(benchmark), modes);
  }

  private static MerComparisonCommand command(List<PortfolioHolding> holdings, List<PortfolioHolding> benchmark,
      FeeAggregationMode... modes) {
    var command = new MerComparisonCommand();
    command.setHoldings(holdings);
    command.setParameterTypes(List.of(modes));
    command.setBenchmarkHoldings(benchmark);
    return command;
  }

  private static MerComparisonData data(Map<PortfolioHolding, FeeData> portfolioFees,
      Map<PortfolioHolding, FeeData> benchmarkFees) {
    return new MerComparisonData(portfolioFees, benchmarkFees);
  }

  private static PortfolioHolding fund(String id, String value) {
    return PortfolioHolding.builder()
        .value(value == null ? null : new BigDecimal(value))
        .holdingType(FinancialInstrumentType.MUTUAL_FUND)
        .country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.FUNDSERV))
        .build();
  }

  private static PortfolioHolding stock(String id, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(FinancialInstrumentType.STOCK)
        .country(Country.CANADA)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .build();
  }

  private static FeeData fee(String mer) {
    return fee(mer, null, Currency.CAD);
  }

  private static FeeData fee(String mer, String managementFee) {
    return fee(mer, managementFee, Currency.CAD);
  }

  private static FeeData fee(String mer, String managementFee, Currency currency) {
    return FeeData.builder()
        .managementExpenseRatio(mer == null ? null : new BigDecimal(mer))
        .managementFee(managementFee == null ? null : new BigDecimal(managementFee))
        .currency(currency)
        .build();
  }
}
