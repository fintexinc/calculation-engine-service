package com.fintex.ce.e2e;

import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.ErrorResponse;
import com.fintex.wm.commons.error.Notification;

import org.springframework.http.HttpStatus;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
}
