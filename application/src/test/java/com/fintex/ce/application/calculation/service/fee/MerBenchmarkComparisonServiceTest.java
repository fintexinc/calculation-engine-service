package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.config.FeeProjectionProperties;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.fee.MerComparisonData;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.FeeComparison;
import com.fintex.ce.model.domain.result.fee.FeeSpendComparison;
import com.fintex.ce.model.domain.result.fee.MerComparisonResult;
import com.fintex.ce.model.dto.command.MerComparisonCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.application.calculation.service.fee.MerBenchmarkComparisonService.NO_BENCHMARK_RATE;
import static com.fintex.ce.application.calculation.service.fee.MerBenchmarkComparisonService.NO_PORTFOLIO_RATE;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.FIVE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerBenchmarkComparisonServiceTest {

  private static final Set<TimePeriod> HORIZONS = new LinkedHashSet<>(List.of(ONE_YR, TEN_YR, TWENTY_YR));

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final MERCalculationServiceImpl merService = new MERCalculationServiceImpl(
      new HoldingCurrencyConverter(fxRateService, new FxProperties()),
      new MerFeeResolver(List.of(new CanadianFeeResolutionStrategy(), new UsFeeResolutionStrategy())));

  /**
   * Flat balance for every test that is about rates, bases or FX: a zero growth rate makes the projected spend exactly
   * {@code annual x years}, so the dollar assertions read straight off the rate and the base. The growth formula itself
   * is covered by {@code FeeProjectionUtilsTest} and by the growing-balance test below.
   */
  private final MerBenchmarkComparisonService service = serviceWithGrowthRate("0");

  {
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Set<Currency> src = inv.getArgument(0);
      return src.stream().collect(Collectors.toMap(c -> c, c -> BigDecimal.ONE));
    });
  }

  @Test
  void shouldComparePortfolioRateToBenchmarkRate_whenSeveralViewsAreRequested() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY, WHOLE_PORTFOLIO),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    FeeComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getFeeRate().portfolio()).isEqualByComparingTo("0.02");
    assertThat(fundsOnly.getFeeRate().benchmark()).isEqualByComparingTo("0.01");
    // (0.02 - 0.01) / 0.01 * 100
    assertThat(fundsOnly.getFeeRate().percentDifference()).isEqualByComparingTo("100");
    assertThat(fundsOnly.getFeeRate().equal()).isFalse();

    // 0.02 * 100 vs 0.01 * 100 a year, flat balance
    assertThat(fundsOnly.getSpend()).containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR);
    assertHorizon(fundsOnly.getSpend().get(ONE_YR), "2", "1", "1");
    assertHorizon(fundsOnly.getSpend().get(TEN_YR), "20", "10", "10");
    assertHorizon(fundsOnly.getSpend().get(TWENTY_YR), "40", "20", "20");

    assertThat(result.getComparison()).containsKey(WHOLE_PORTFOLIO);
  }

  @Test
  void shouldReportZeroSavings_whenPortfolioRateIsIdenticalToBenchmarkRate() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.015")), Map.of(benchmark, fee("0.015"))));

    FeeComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getFeeRate().equal()).isTrue();
    assertThat(fundsOnly.getFeeRate().percentDifference()).isEqualByComparingTo("0");
    assertThat(fundsOnly.getSpend().values())
        .allSatisfy(horizon -> assertThat(horizon.savings()).isEqualByComparingTo("0"));
    assertThat(fundsOnly.getSpend().get(TWENTY_YR).portfolio()).isEqualByComparingTo("30");
    assertThat(fundsOnly.getSpend().get(TWENTY_YR).benchmark()).isEqualByComparingTo("30");
  }

  @Test
  void shouldReportNegativeSavings_whenTheBenchmarkIsDearerThanThePortfolio() {
    PortfolioHolding portfolioFund = fund("CIG1101", "200");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, WHOLE_PORTFOLIO),
        data(Map.of(portfolioFund, fee("0.01")), Map.of(benchmark, fee("0.02"))));

    FeeComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    // (0.01 - 0.02) / 0.02 * 100
    assertThat(whole.getFeeRate().percentDifference()).isEqualByComparingTo("-50");
    // switching would cost more, so the saving is negative at every horizon rather than clamped to zero
    assertHorizon(whole.getSpend().get(ONE_YR), "2", "4", "-2");
    assertHorizon(whole.getSpend().get(TEN_YR), "20", "40", "-20");
    assertHorizon(whole.getSpend().get(TWENTY_YR), "40", "80", "-40");
    assertThat(whole.getSpend().values()).allSatisfy(horizon -> assertThat(horizon.savings()).isNegative());
  }

  @Test
  void shouldScopeTheProjectionBasePerView_whenPortfolioMixesFundsAndNonFundHoldings() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding stock = stock("RY.TO", "300");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(
        command(List.of(portfolioFund, stock), benchmark, FUNDS_ONLY, WHOLE_PORTFOLIO),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    FeeComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    assertThat(fundsOnly.getFeeRate().portfolio()).isEqualByComparingTo("0.02");
    // the funds-only base is the fund value (100) alone; the 300 stock is excluded from both sides
    assertHorizon(fundsOnly.getSpend().get(ONE_YR), "2", "1", "1");

    FeeComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    // the whole-portfolio rate dilutes the fund across all 400: 0.02 * (100/400)
    assertThat(whole.getFeeRate().portfolio()).isEqualByComparingTo("0.005");
    // and its base is the full 400, so the portfolio's dollars match the funds-only view while the benchmark's grow
    assertHorizon(whole.getSpend().get(ONE_YR), "2", "4", "-2");
  }

  @Test
  void shouldProjectOnTheFxConvertedBase_whenHoldingCurrencyDiffersFromTargetCurrency() {
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.25"), Currency.CAD, BigDecimal.ONE));

    PortfolioHolding usdFund = fund("TDB952", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(usdFund, benchmark, WHOLE_PORTFOLIO),
        data(Map.of(usdFund, fee("0.02", null, Currency.USD)), Map.of(benchmark, fee("0.01"))));

    FeeComparison whole = result.getComparison().get(WHOLE_PORTFOLIO);
    assertThat(whole.getFeeRate().portfolio()).isEqualByComparingTo("0.02");
    // the base is the 100 USD value converted to CAD at 1.25 = 125, not the raw 100
    assertHorizon(whole.getSpend().get(ONE_YR), "2.5", "1.25", "1.25");
  }

  @Test
  void shouldProjectInTheRequestedTargetCurrency_whenTheCommandSuppliesOne() {
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.CAD, new BigDecimal("0.80"), Currency.USD, BigDecimal.ONE));

    PortfolioHolding cadFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    var command = command(cadFund, benchmark, WHOLE_PORTFOLIO);
    command.setTargetCurrency(Currency.USD);

    MerComparisonResult result = service.perform(command,
        data(Map.of(cadFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    // the 100 CAD value is converted to USD at 0.80 = 80, not left at 100
    assertHorizon(result.getComparison().get(WHOLE_PORTFOLIO).getSpend().get(ONE_YR), "1.6", "0.8", "0.8");
    // both runs — the portfolio's and the benchmark's — convert into the requested currency, not just the portfolio's
    verify(fxRateService, times(2)).spotRates(anySet(), eq(Currency.USD), any());
  }

  @Test
  void shouldCompoundTheBalanceButNotTheFee_whenAGrowthRateIsConfigured() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = serviceWithGrowthRate("0.06").perform(
        command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    Map<TimePeriod, FeeSpendComparison> byHorizon = result.getComparison().get(FUNDS_ONLY).getSpend();
    // annual is 2 vs 1; the growing balance costs 13.1807949424 annual fees over ten years and 36.7855912035 over
    // twenty
    assertHorizon(byHorizon.get(ONE_YR), "2", "1", "1");
    assertHorizon(byHorizon.get(TEN_YR), "26.3615898848", "13.1807949424", "13.1807949424");
    assertHorizon(byHorizon.get(TWENTY_YR), "73.5711824071", "36.7855912035", "36.7855912035");
    // twenty years of a growing balance costs more than twice ten years of one
    assertThat(byHorizon.get(TWENTY_YR).savings()).isGreaterThan(byHorizon.get(TEN_YR).savings().multiply(BigDecimal
        .valueOf(2)));
    // the portfolio charges exactly double the benchmark on one base, so the saving is the benchmark's own spend to the
    // last reported digit — it would drift by one if it were derived from the two already-rounded spends
    assertThat(byHorizon.values())
        .allSatisfy(horizon -> assertThat(horizon.savings()).isEqualByComparingTo(horizon.benchmark()));
  }

  @Test
  void shouldReportTheRequestedHorizons_whenTheCommandSuppliesThem() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");
    MerComparisonCommand command = command(portfolioFund, benchmark, FUNDS_ONLY);
    command.setProjectionPeriods(new LinkedHashSet<>(List.of(THREE_YR, FIVE_YR)));

    MerComparisonResult result = service.perform(command,
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    // the configured 1/10/20 give way entirely to the requested set, on both sides of the comparison
    Map<TimePeriod, FeeSpendComparison> byHorizon = result.getComparison().get(FUNDS_ONLY).getSpend();
    assertThat(byHorizon).containsOnlyKeys(THREE_YR, FIVE_YR);
    assertHorizon(byHorizon.get(THREE_YR), "6", "3", "3");
    assertHorizon(byHorizon.get(FIVE_YR), "10", "5", "5");
  }

  @Test
  void shouldPreferTheRequestedHorizons_whenTheyDisagreeWithTheConfiguredOnes() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");
    var narrowedByConfig = new MerBenchmarkComparisonService(merService, projection("0", Set.of(FIVE_YR)));
    MerComparisonCommand command = command(portfolioFund, benchmark, FUNDS_ONLY);
    command.setProjectionPeriods(Set.of(THREE_YR));

    MerComparisonResult result = narrowedByConfig.perform(command,
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    // the request wins over the server default rather than being intersected with it
    assertThat(result.getComparison().get(FUNDS_ONLY).getSpend()).containsOnlyKeys(THREE_YR);
  }

  @Test
  void shouldReportOnlyTheConfiguredHorizons_whenTheHorizonListIsNarrowed() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");
    var narrowed = new MerBenchmarkComparisonService(merService, projection("0", Set.of(FIVE_YR)));

    MerComparisonResult result = narrowed.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    assertThat(result.getComparison().get(FUNDS_ONLY).getSpend()).containsOnlyKeys(FIVE_YR);
    assertHorizon(result.getComparison().get(FUNDS_ONLY).getSpend().get(FIVE_YR), "10", "5", "5");
  }

  /**
   * A stock-only portfolio has no fee rate in the funds-only view, so there is nothing to compare the benchmark with.
   * That is reported as an error rather than as a comparison of nulls the caller would have to interpret.
   */
  @Test
  void shouldFail_whenTheViewHasNoPortfolioRateToCompare() {
    PortfolioHolding stock = stock("RY.TO", "300");
    PortfolioHolding benchmark = fund("TDB622", "100");
    MerComparisonCommand command = command(List.of(stock), benchmark, FUNDS_ONLY);
    MerComparisonData data = data(Map.of(), Map.of(benchmark, fee("0.01")));

    assertThatThrownBy(() -> service.perform(command, data))
        .isInstanceOf(CalculationException.class)
        .satisfies(thrown -> {
          CalculationException exception = (CalculationException) thrown;
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEE_COMPARISON_NOT_AVAILABLE);
          assertThat(exception.getMessage()).isEqualTo(
              ErrorCode.FEE_COMPARISON_NOT_AVAILABLE.getFormattedMessage(FUNDS_ONLY, NO_PORTFOLIO_RATE));
          assertThat(exception.getMetadata())
              .containsEntry("param-1", FUNDS_ONLY)
              .containsEntry("param-2", NO_PORTFOLIO_RATE);
        });
  }

  @Test
  void shouldFail_whenTheBenchmarkHasNoRateToCompareAgainst() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmarkStock = stock("RY.TO", "100");
    MerComparisonCommand command = command(List.of(portfolioFund), List.of(benchmarkStock), FUNDS_ONLY);
    MerComparisonData data = data(Map.of(portfolioFund, fee("0.02")), Map.of());

    assertThatThrownBy(() -> service.perform(command, data))
        .isInstanceOf(CalculationException.class)
        .satisfies(thrown -> {
          CalculationException exception = (CalculationException) thrown;
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEE_COMPARISON_NOT_AVAILABLE);
          assertThat(exception.getMessage()).isEqualTo(
              ErrorCode.FEE_COMPARISON_NOT_AVAILABLE.getFormattedMessage(FUNDS_ONLY, NO_BENCHMARK_RATE));
          assertThat(exception.getMetadata()).containsEntry("param-2", NO_BENCHMARK_RATE);
        });
  }

  @Test
  void shouldWeightTheBenchmarkRateByHoldingValue_whenBenchmarkHasSeveralHoldings() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding cheap = fund("TDB622", "300");
    PortfolioHolding pricey = fund("RBF556", "100");

    MerComparisonResult result = service.perform(
        command(List.of(portfolioFund), List.of(cheap, pricey), FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(cheap, fee("0.01"), pricey, fee("0.03"))));

    FeeComparison fundsOnly = result.getComparison().get(FUNDS_ONLY);
    // 0.01 * (300/400) + 0.03 * (100/400)
    assertThat(fundsOnly.getFeeRate().benchmark()).isEqualByComparingTo("0.015");
    assertHorizon(fundsOnly.getSpend().get(ONE_YR), "2", "1.5", "0.5");
  }

  @Test
  void shouldWeightBenchmarkHoldingsEqually_whenNoBenchmarkHoldingCarriesAValue() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding cheap = fund("TDB622", null);
    PortfolioHolding pricey = fund("RBF556", null);

    MerComparisonResult result = service.perform(
        command(List.of(portfolioFund), List.of(cheap, pricey), FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(cheap, fee("0.01"), pricey, fee("0.03"))));

    // no weights to go on, so the two rates average evenly: (0.01 + 0.03) / 2
    assertThat(result.getComparison().get(FUNDS_ONLY).getFeeRate().benchmark()).isEqualByComparingTo("0.02");
  }

  @Test
  void shouldUseTheFundsOwnRate_whenBenchmarkIsASingleFundWithoutAValue() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", null);

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee("0.01"))));

    assertThat(result.getComparison().get(FUNDS_ONLY).getFeeRate().benchmark()).isEqualByComparingTo("0.01");
  }

  @Test
  void shouldCarryBenchmarkWarnings_whenBenchmarkFallsBackToManagementFee() {
    PortfolioHolding portfolioFund = fund("CIG1101", "100");
    PortfolioHolding benchmark = fund("TDB622", "100");

    MerComparisonResult result = service.perform(command(portfolioFund, benchmark, FUNDS_ONLY),
        data(Map.of(portfolioFund, fee("0.02")), Map.of(benchmark, fee(null, "0.0075"))));

    assertThat(result.getComparison().get(FUNDS_ONLY).getFeeRate().benchmark()).isEqualByComparingTo("0.0075");
    assertThat(result.getWarnings()).extracting("code").contains("FDS-022");
  }

  private MerBenchmarkComparisonService serviceWithGrowthRate(String growthRate) {
    return new MerBenchmarkComparisonService(merService, projection(growthRate, HORIZONS));
  }

  private static FeeProjectionProperties projection(String growthRate, Set<TimePeriod> periods) {
    var properties = new FeeProjectionProperties();
    properties.setAnnualGrowthRate(new BigDecimal(growthRate));
    properties.setPeriods(periods);
    return properties;
  }

  /**
   * Compares by value rather than by record equality: money amounts leave the calculation at the shared output scale,
   * except for exact zeros and ones which come back unscaled, so {@code BigDecimal.equals} would reject values that are
   * numerically right.
   */
  private static void assertHorizon(FeeSpendComparison actual, String portfolioSpend, String benchmarkSpend,
      String savings) {
    assertThat(actual.portfolio()).isEqualByComparingTo(portfolioSpend);
    assertThat(actual.benchmark()).isEqualByComparingTo(benchmarkSpend);
    assertThat(actual.savings()).isEqualByComparingTo(savings);
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
