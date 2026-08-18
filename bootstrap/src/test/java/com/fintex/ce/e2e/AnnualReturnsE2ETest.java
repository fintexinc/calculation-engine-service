package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CAD annual-returns scenarios (mixed holdings, validation inherited from {@link AbstractAnnualReturnsE2ETest} and
 * {@link AbstractPortfolioCalculationE2ETest}). FX-specific coverage lives in {@link AnnualReturnsWithFxE2ETest}.
 */
@Tag("e2e")
class AnnualReturnsE2ETest extends AbstractAnnualReturnsE2ETest {

  // Rolling window Oct 2024 -> Sep 2025: 12 months, but neither calendar year is a complete Jan-Dec span.
  private static final List<String> OCT_2024_TO_SEP_2025 = List.of(
      "2024-10-31", "2024-11-30", "2024-12-31", "2025-01-31", "2025-02-28", "2025-03-31",
      "2025-04-30", "2025-05-31", "2025-06-30", "2025-07-31", "2025-08-31", "2025-09-30");

  @Test
  void shouldReturnBadRequest_whenGicHoldingOmitsInterestRate() {
    ReturnCommand command = commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "45234.67"),
        gicWithoutInterestRate(Currency.CAD, "25000.00", "365")));

    Notification notification = assertSingleError(postCalculation(writeJson(command)), "GIC-001",
        "The gic holding is missing interest rate");
    assertThat(notification.getMetadata()).isEmpty();
  }

  @Test
  void shouldReturnBadRequest_whenNoCompleteCalendarYear() {
    enqueueMicMockResponse(writeJson(List.of(
        securityAttributeResult(XBAL, monthlyReturnsFor("2025-09-30T00:00:00", OCT_2024_TO_SEP_2025)))));
    ReturnCommand command = commandFor(Currency.CAD, List.of(etfCanada(XBAL, "45234.67")));

    Notification notification = assertSingleError(postCalculation(writeJson(command)), "RET-010",
        "No complete calendar year (Jan-Dec) found in monthly returns range [2024-10-31, 2025-09-30]; "
            + "annual returns cannot be computed");
    assertThat(notification.getMetadata()).containsKeys("param-1", "param-2");
  }

  @Test
  void shouldReturnComparison_whenBenchmarkHoldingsAreProvided() {
    enqueueMicMockResponse(writeJson(List.of(
        securityAttributeResult(XBAL, fullYear2024Returns()),
        securityAttributeResult(F0CAN999, fullYear2024Returns()))));
    enqueueMicMockResponse(writeJson(List.of(
        securityAttributeResult(VCNS, benchmarkFullYear2024Returns()),
        securityAttributeResult(CCM4752, benchmarkFullYear2024Returns()))));
    ReturnCommand command = commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "60000"),
        fund(F0CAN999, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "40000")));
    command.setBenchmarkHoldings(List.of(
        etfCanada(VCNS, "55000"),
        fundServ(CCM4752, "45000")));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    AnnualReturnResult<?> result = readJson(response.responseBody(), AnnualReturnResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getAnnualReturns()).singleElement().satisfies(annualReturn -> {
      assertThat(annualReturn.key()).isEqualTo(2024);
      assertThat(annualReturn.value()).isEqualByComparingTo(new BigDecimal("0.0114842466"));
    });
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(2024);
      assertThat(comparison.portfolio()).isEqualByComparingTo(new BigDecimal("0.0114842466"));
      assertThat(comparison.benchmark()).isEqualByComparingTo(new BigDecimal("0.0068870814"));
      assertThat(comparison.percentDifference()).isEqualByComparingTo(new BigDecimal("66.7505570647"));
    });
  }

  private Notification assertSingleError(HttpResponse response, String expectedCode, String expectedMessage) {
    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo(expectedCode);
    assertThat(notification.getSeverity().name()).isEqualTo("ERROR");
    assertThat(notification.getMessage()).isEqualTo(expectedMessage);
    return notification;
  }
}
