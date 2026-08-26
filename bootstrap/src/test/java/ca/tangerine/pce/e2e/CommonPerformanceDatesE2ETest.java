package ca.tangerine.pce.e2e;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static ca.tangerine.pce.e2e.E2EPortfolios.etf;
import static ca.tangerine.pce.e2e.MicAttributeResponses.attributeResult;
import static ca.tangerine.pce.e2e.MicAttributeResponses.morningstarOnly;
import static ca.tangerine.pce.e2e.MicAttributeResponses.singleAttributeDispatcher;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.CommonPerformanceDatesResult;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.performance.MonthlyReturns;
import ca.tangerine.wm.commons.domain.value.DateBigDecimalValue;
import ca.tangerine.wm.commons.error.Notification;

/**
 * End-to-end coverage for the {@code /common-performance-dates} endpoint, which answers the question every return and
 * risk metric has to ask first: over what window can these holdings be compared at all. The window is the intersection
 * of their return histories — the latest start and the earliest end — and the portfolio and benchmark sides are
 * computed independently, because a benchmark that goes back further does not extend what the portfolio can be measured
 * over.
 *
 * <p>
 * This is the only metric taking several portfolios in one request, and it flattens them into a single window rather
 * than reporting one per portfolio, so the positive scenario holds two portfolios whose histories differ at both ends:
 * the answer must come from one holding's start and the other's end.
 */
@Tag("e2e")
class CommonPerformanceDatesE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String EARLY_START_ETF = "XBAL";
  private static final String LATE_START_ETF = "VCNS";
  private static final String BENCHMARK_ETF = "SPY";

  @Override
  protected String metricPath() {
    return CalculationMetric.COMMON_PERFORMANCE_DATES.getValue();
  }

  @Override
  protected String requestBodyForMicUnavailableScenario() {
    return writeJson(commonDatesCommand(
        List.of(List.of(etf(EARLY_START_ETF, 50_000)), List.of(etf(LATE_START_ETF, 50_000))),
        List.of(etf(BENCHMARK_ETF, 100_000))));
  }

  /**
   * Two portfolios of one ETF each, plus a benchmark. The histories are picked so that no single holding decides the
   * answer: the portfolio window starts where the later fund starts and ends where the earlier one ends, and the
   * benchmark — which both precedes and stops short of them — reports its own window untouched by theirs.
   */
  @Override
  protected String requestBodyForPositiveMicScenario() {
    return requestBodyForMicUnavailableScenario();
  }

  @Override
  protected String micPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The portfolio side and the benchmark side are fetched separately, so this scenario makes two attribute calls rather
   * than one. A dispatcher answers both from the same set of rows, which is what Market Investment Catalogue would do:
   * the rows a caller receives are the ones it asked identifiers for.
   */
  @Override
  protected void enqueueForPositiveMicScenario() {
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.MONTHLY_RETURNS, positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(etf(EARLY_START_ETF, 50_000)));
    return writeJson(command);
  }

  /**
   * The full expected payload. The portfolio holdings run 2023-01 to 2024-06 and 2023-06 to 2024-12, so the window they
   * share is 2023-06-30 to 2024-06-30 — the later start and the earlier end, each contributed by a different holding.
   * The benchmark runs 2022-01 to 2024-03 and reports exactly that, which is the assertion that the two sides are
   * computed independently: pooled together, the four series would give 2023-06-30 to 2024-03-31 for both.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    CommonPerformanceDatesResult result = readJson(responseBody, CommonPerformanceDatesResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getCommonPerformanceStartDatePf()).isEqualTo(LocalDate.of(2023, 6, 30));
    assertThat(result.getCommonPerformanceEndDatePf()).isEqualTo(LocalDate.of(2024, 6, 30));
    assertThat(result.getCommonPerformanceStartDateBm()).isEqualTo(LocalDate.of(2022, 1, 31));
    assertThat(result.getCommonPerformanceEndDateBm()).isEqualTo(LocalDate.of(2024, 3, 31));
  }

  /**
   * Two portfolios whose histories do not overlap at all: one ran through 2020, the other starts in 2024. Taken
   * together their intersection is empty — the latest start falls after the earliest end — and the metric does not
   * report an inverted window or fail. It drops the holding that starts after the common end, says so with
   * {@code HOLDING_PSD_OUT_OF_RANGE} naming that holding, and reports the window the remaining history actually
   * supports.
   *
   * <p>
   * The benchmark side is left out of this request, and the two benchmark dates come back null rather than borrowed
   * from the portfolio side — an absent question has no answer.
   */
  @Test
  void shouldDropTheHoldingAndWarn_whenTheHistoriesDoNotOverlap() {
    PortfolioHolding older = etf(EARLY_START_ETF, 50_000);
    PortfolioHolding newer = etf(LATE_START_ETF, 50_000);
    micMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.MONTHLY_RETURNS, List.of(
            returnsRow(EARLY_START_ETF, YearMonth.of(2020, 1), YearMonth.of(2020, 12)),
            returnsRow(LATE_START_ETF, YearMonth.of(2024, 1), YearMonth.of(2024, 12)))));

    var response = postCalculation(writeJson(commonDatesCommand(
        List.of(List.of(older), List.of(newer)), List.of())));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    CommonPerformanceDatesResult result = readJson(response.responseBody(), CommonPerformanceDatesResult.class);
    assertThat(result.getWarnings()).hasSize(1);
    Notification warning = result.getWarnings().getFirst();
    String droppedHoldingId = newer.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.HOLDING_PSD_OUT_OF_RANGE);
    assertThat(warning.getMessage())
        .isEqualTo(ErrorCode.HOLDING_PSD_OUT_OF_RANGE.getFormattedMessage(droppedHoldingId));
    assertThat(warning.getDescription()).isEqualTo(ErrorCode.HOLDING_PSD_OUT_OF_RANGE.getDescription());
    assertThat(warning.getAction()).isEqualTo(ErrorCode.HOLDING_PSD_OUT_OF_RANGE.getAction());
    assertThat(warning.getMetadata())
        .as("the holding is named in the message and repeated in the metadata, where a client can read it without parsing prose")
        .containsEntry("holdingId", droppedHoldingId);
    assertThat(result.getCommonPerformanceStartDatePf()).isEqualTo(LocalDate.of(2020, 1, 31));
    assertThat(result.getCommonPerformanceEndDatePf()).isEqualTo(LocalDate.of(2020, 12, 31));
    assertThat(result.getCommonPerformanceStartDateBm()).isNull();
    assertThat(result.getCommonPerformanceEndDateBm()).isNull();
  }

  private static List<SecurityAttributeResult<MonthlyReturns>> positiveScenarioRows() {
    return List.of(
        returnsRow(EARLY_START_ETF, YearMonth.of(2023, 1), YearMonth.of(2024, 6)),
        returnsRow(LATE_START_ETF, YearMonth.of(2023, 6), YearMonth.of(2024, 12)),
        returnsRow(BENCHMARK_ETF, YearMonth.of(2022, 1), YearMonth.of(2024, 3)));
  }

  private static MultiplePortfoliosCommand commonDatesCommand(List<List<PortfolioHolding>> portfolios,
      List<PortfolioHolding> benchmarkHoldings) {
    MultiplePortfoliosCommand command = new MultiplePortfoliosCommand();
    command.setMetric(CalculationMetric.COMMON_PERFORMANCE_DATES);
    command.setPortfolios(portfolios.stream()
        .map(MultiplePortfoliosCommand.Portfolio::new)
        .collect(toCollection(LinkedHashSet::new)));
    command.setBenchmarkHoldings(benchmarkHoldings);
    command.setDataProviders(morningstarOnly());
    return command;
  }

  /**
   * A contiguous month-end series, which is the shape the engine expects — a hole in the middle is a different defect,
   * rejected upstream in the mapper. The values vary month to month rather than repeating one constant, so a series is
   * a plausible return path rather than a placeholder; only its first and last dates matter to this metric.
   */
  private static SecurityAttributeResult<MonthlyReturns> returnsRow(String ticker, YearMonth from, YearMonth to) {
    List<DateBigDecimalValue> returns = new ArrayList<>();
    int index = 0;
    for (YearMonth month = from; !month.isAfter(to); month = month.plusMonths(1)) {
      BigDecimal value = BigDecimal.valueOf((index % 7) - 2L).movePointLeft(1);
      returns.add(new DateBigDecimalValue(month.atEndOfMonth().toString(), value));
      index++;
    }
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setDataProviders(morningstarOnly());
    return attributeResult(ticker, FiIdentifierType.TICKER, monthlyReturns);
  }
}
