package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.result.returns.Growth10KResult;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CAD growth-of-10k scenarios (mixed holdings, validation inherited from {@link AbstractGrowthOf10kE2ETest} and
 * {@link AbstractPortfolioCalculationE2ETest}). FX-specific coverage lives in {@link Growth10kWithFxE2ETest}.
 */
@Tag("e2e")
class GrowthOf10kE2ETest extends AbstractGrowthOf10kE2ETest {

  @Test
  void shouldReturnBadRequest_whenGicHoldingOmitsInterestRate() {
    ReturnCommand command = commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "45234.67"),
        gicWithoutInterestRate(Currency.CAD, "25000.00", "365")));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    ErrorResponse error = readJson(response.responseBody(), ErrorResponse.class);
    assertThat(error.getNotifications()).hasSize(1);
    Notification notification = error.getNotifications().getFirst();
    assertThat(notification.getCode()).isEqualTo("GIC-001");
    assertThat(notification.getSeverity().name()).isEqualTo("ERROR");
    assertThat(notification.getMessage()).isEqualTo("The gic holding is missing interest rate");
  }

  @Test
  void shouldReturnComparison_whenBenchmarkHoldingsAreProvided() {
    enqueueMicMockResponse(writeJson(List.of(
        securityAttributeResult(XBAL, twoMonthReturns("5.0", "-2.0")),
        securityAttributeResult(F0CAN999, twoMonthReturns("1.0", "2.0")))));
    enqueueMicMockResponse(writeJson(List.of(
        securityAttributeResult(VCNS, twoMonthReturns("2.0", "1.0")),
        securityAttributeResult(CCM4752, twoMonthReturns("0.5", "-1.0")))));
    ReturnCommand command = commandFor(Currency.CAD, List.of(
        etfCanada(XBAL, "50000"),
        fund(F0CAN999, FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "50000")));
    command.setBenchmarkHoldings(List.of(
        etfCanada(VCNS, "50000"),
        fundServ(CCM4752, "50000")));

    HttpResponse response = postCalculation(writeJson(command));

    assertThat(response.status().value()).isEqualTo(HttpStatus.OK.value());
    Growth10KResult result = readJson(response.responseBody(), Growth10KResult.class);
    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getPerformanceStartDate()).isEqualTo(LocalDate.of(2023, 12, 31));
    assertThat(result.getPerformanceEndDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    assertThat(result.getGrowth10k()).hasSize(3);
    assertGrowthPoint(result.getGrowth10k().get(0), "2023-12-31", "10000");
    assertGrowthPoint(result.getGrowth10k().get(1), "2024-01-31", "10300");
    assertGrowthPoint(result.getGrowth10k().get(2), "2024-02-29", "10300");
    assertThat(result.getComparison()).hasSize(3);
    assertThat(result.getComparison().get(0).period()).isEqualTo(LocalDate.of(2023, 12, 31));
    assertThat(result.getComparison().get(0).portfolio()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(result.getComparison().get(0).benchmark()).isEqualByComparingTo(new BigDecimal("10000"));
    assertThat(result.getComparison().get(0).percentDifference()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.getComparison().get(1).period()).isEqualTo(LocalDate.of(2024, 1, 31));
    assertThat(result.getComparison().get(1).portfolio()).isEqualByComparingTo(new BigDecimal("10300"));
    assertThat(result.getComparison().get(1).benchmark()).isEqualByComparingTo(new BigDecimal("10125"));
    assertThat(result.getComparison().get(1).percentDifference())
        .isEqualByComparingTo(new BigDecimal("1.7283950617"));
    assertThat(result.getComparison().get(2).period()).isEqualTo(LocalDate.of(2024, 2, 29));
    assertThat(result.getComparison().get(2).portfolio()).isEqualByComparingTo(new BigDecimal("10300"));
    assertThat(result.getComparison().get(2).benchmark()).isEqualByComparingTo(new BigDecimal("10125"));
    assertThat(result.getComparison().get(2).percentDifference())
        .isEqualByComparingTo(new BigDecimal("1.7283950617"));
  }
}
