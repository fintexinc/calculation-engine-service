package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.fintex.ce.e2e.SmsAttributeResponses.attributeResult;
import static com.fintex.ce.e2e.SmsAttributeResponses.morningstarOnly;
import static com.fintex.ce.e2e.SmsAttributeResponses.singleAttributeDispatcher;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.etfCa;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.fundCa;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage for the {@code /number-of-unique-holdings} endpoint. It counts the distinct underlying holdings
 * behind the portfolio's securities, so the number it reports is only meaningful together with what it did about the
 * securities it could not see through — a fund counted as one holding and a fund whose 200 positions were not returned
 * are the same "1" in the payload, and only the warnings tell them apart.
 *
 * <p>
 * The identifiers are compared on the configured comparison type ({@code default.holdings-identifier-type}, Morningstar
 * ids), which is what makes the deduplication meaningful in the first place: the same company reached through two funds
 * is one holding only if both funds name it the same way. Fixtures therefore state the identifier type on every
 * underlying holding, and the gap scenario includes a security whose ids are of another type — indistinguishable, to
 * this metric, from a security with no identifiers at all.
 */
@Tag("e2e")
class NumberOfUniqueHoldingsE2ETest extends AbstractPortfolioCalculationE2ETest {

  private static final String CANADIAN_FUND = "F00000UNQ1";
  private static final String GLOBAL_FUND = "F00000UNQ2";
  private static final String WORLD_ETF = "XAW";

  @Override
  protected String metricPath() {
    return CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS.getValue();
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return writeJson(uniqueHoldingsCommand(fundCa(CANADIAN_FUND, 50_000), fundCa(GLOBAL_FUND, 50_000)));
  }

  /**
   * Two funds that overlap: the point of the metric is that a company held through both is one holding, not two, so a
   * portfolio whose funds share positions must report fewer holdings than the two lists add up to.
   */
  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return writeJson(uniqueHoldingsCommand(fundCa(CANADIAN_FUND, 60_000), fundCa(GLOBAL_FUND, 40_000)));
  }

  @Override
  protected String smsPositiveResponseBody() {
    return writeJson(positiveScenarioRows());
  }

  /**
   * The shared positive scenario enqueues a single response, which suffices for one holding; this portfolio holds two,
   * and how many attribute calls the fetcher batches them into is an implementation detail. The dispatcher answers all
   * of them, and only on this metric's attribute path.
   */
  @Override
  protected void enqueueForPositiveSmsScenario() {
    smsMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.HOLDING_IDENTIFIERS, positiveScenarioRows()));
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setCurrency(Currency.CAD);
    command.setHoldings(List.of(fundCa(CANADIAN_FUND, 50_000)));
    return writeJson(command);
  }

  /**
   * The two funds name five underlying holdings between them and share one — {@code U-003} — so the portfolio holds
   * four distinct ones. Nothing was missing, so there are no warnings, and that is half the assertion: the same count
   * with a warning beside it would mean something quite different.
   */
  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    NumberOfUniqueHoldingsResult result = readJson(responseBody, NumberOfUniqueHoldingsResult.class);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getNumberOfUniqueHoldings()).isEqualTo(4L);
  }

  /**
   * Every way this metric can fail to see through a security, in one request, because the count they produce is the
   * same and only their warnings differ:
   *
   * <ul>
   * <li>a security Security Master has no record of — reported per holding, and counted as one holding of its own;</li>
   * <li>a security whose underlying ids are all of another identifier type, which this metric cannot compare and so
   * cannot deduplicate — counted as one, reported as a count of affected securities rather than per holding;</li>
   * <li>an underlying holding whose id value is null — its parent cannot be deduplicated either, so it too counts as
   * one.</li>
   * </ul>
   *
   * <p>
   * The count is therefore 1 (the one fund that resolved) + 3, and a client reading 4 without the warnings would
   * believe it holds four companies rather than one company and three unknowns.
   */
  @Test
  void shouldCountUnresolvedSecuritiesAsOneEach_andWarnOnEachReason() {
    PortfolioHolding resolved = fundCa(CANADIAN_FUND, 25_000);
    PortfolioHolding unknownToSecurityMaster = fundCa(GLOBAL_FUND, 25_000);
    PortfolioHolding wrongIdentifierType = etfCa(WORLD_ETF, 25_000);
    PortfolioHolding nullIdentifierValue = fundCa("F00000UNQ3", 25_000);
    smsMockServer.setDispatcher(
        singleAttributeDispatcher(CompositeSecurityAttribute.HOLDING_IDENTIFIERS, List.of(
            identifiersRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID,
                underlying("U-001", FiIdentifierType.MORNINGSTAR_ID)),
            identifiersRow(WORLD_ETF, FiIdentifierType.TICKER,
                underlying("U-002", FiIdentifierType.TICKER)),
            identifiersRow("F00000UNQ3", FiIdentifierType.MORNINGSTAR_ID,
                underlying(null, FiIdentifierType.MORNINGSTAR_ID)))));

    var response = postCalculation(writeJson(uniqueHoldingsCommand(
        resolved, unknownToSecurityMaster, wrongIdentifierType, nullIdentifierValue)));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    NumberOfUniqueHoldingsResult result = readJson(response.responseBody(), NumberOfUniqueHoldingsResult.class);
    assertThat(result.getNumberOfUniqueHoldings()).isEqualTo(4L);
    assertThat(result.getWarnings()).hasSize(3)
        .extracting(Notification::getCode)
        .containsExactlyInAnyOrder(ErrorCode.Codes.MISSING_HOLDING_IDENTIFIERS,
            ErrorCode.Codes.MISSING_UNDERLYING_HOLDING_ID_VALUE, ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertCountingWarning(warningWith(result, ErrorCode.MISSING_HOLDING_IDENTIFIERS),
        ErrorCode.MISSING_HOLDING_IDENTIFIERS, 1);
    assertCountingWarning(warningWith(result, ErrorCode.MISSING_UNDERLYING_HOLDING_ID_VALUE),
        ErrorCode.MISSING_UNDERLYING_HOLDING_ID_VALUE, 1);
    assertSecurityNotFoundWarning(warningWith(result, ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC),
        unknownToSecurityMaster);
  }

  /**
   * Picks a warning out by its code rather than by its position. The three reasons are independent of one another and
   * the order the metric happens to emit them in is not part of its contract, so indexing into the list would make this
   * an assertion about that order.
   */
  private static Notification warningWith(NumberOfUniqueHoldingsResult result, ErrorCode expected) {
    return result.getWarnings().stream()
        .filter(warning -> expected.getCode().equals(warning.getCode()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no warning with code " + expected.getCode()));
  }

  /**
   * These two warnings report how many securities or underlying holdings were affected rather than which — the metric
   * aggregates them — so the substituted count is the whole of their information and is asserted as such.
   */
  private static void assertCountingWarning(Notification warning, ErrorCode expected, int expectedCount) {
    assertThat(warning.getCode()).isEqualTo(expected.getCode());
    assertThat(warning.getMessage()).isEqualTo(expected.getFormattedMessage(expectedCount));
    assertThat(warning.getDescription()).isEqualTo(expected.getDescription());
    assertThat(warning.getAction()).isEqualTo(expected.getAction());
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata()).containsEntry("param-1", expectedCount);
  }

  private static void assertSecurityNotFoundWarning(Notification warning, PortfolioHolding expectedFor) {
    String holdingId = expectedFor.getIdsString();
    assertThat(warning.getCode()).isEqualTo(ErrorCode.Codes.SECURITY_NOT_FOUND_FOR_METRIC);
    assertThat(warning.getMessage()).isEqualTo(ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC
        .getFormattedMessage(holdingId, CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS.getUserFriendlyName()));
    assertThat(warning.getSeverity()).isEqualTo(Severity.WARNING);
    assertThat(warning.getMetadata())
        .containsEntry("holdingId", holdingId)
        .containsEntry("param-2", CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS.getUserFriendlyName());
  }

  private static List<SecurityAttributeResult<HoldingIdentifiers>> positiveScenarioRows() {
    return List.of(
        identifiersRow(CANADIAN_FUND, FiIdentifierType.MORNINGSTAR_ID,
            underlying("U-001", FiIdentifierType.MORNINGSTAR_ID),
            underlying("U-002", FiIdentifierType.MORNINGSTAR_ID),
            underlying("U-003", FiIdentifierType.MORNINGSTAR_ID)),
        identifiersRow(GLOBAL_FUND, FiIdentifierType.MORNINGSTAR_ID,
            underlying("U-003", FiIdentifierType.MORNINGSTAR_ID),
            underlying("U-004", FiIdentifierType.MORNINGSTAR_ID)));
  }

  private static PortfolioHoldingsCommand uniqueHoldingsCommand(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder()
        .metric(CalculationMetric.NUMBER_OF_UNIQUE_HOLDINGS)
        .holdings(List.of(holdings))
        .dataProviders(morningstarOnly())
        .build();
  }

  private static SecurityAttributeResult<HoldingIdentifiers> identifiersRow(String id, FiIdentifierType idType,
      SecurityIdentifier... underlyingHoldings) {
    HoldingIdentifiers identifiers = new HoldingIdentifiers();
    identifiers.setHoldingIds(Arrays.asList(underlyingHoldings));
    identifiers.setDataProviders(morningstarOnly());
    return attributeResult(id, idType, identifiers);
  }

  private static SecurityIdentifier underlying(String id, FiIdentifierType idType) {
    return new SecurityIdentifier(id, idType);
  }
}
