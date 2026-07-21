package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.ce.model.error.exceptions.ValidationException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestValidationFacadeTest {

  @Test
  void shouldNotThrow_whenNoValidatorsRegisteredForMetric() {
    RequestValidationFacade facade = new RequestValidationFacade(List.of());
    PeriodCommand command = periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS);

    assertThatCode(() -> facade.validate(command))
        .doesNotThrowAnyException();
  }

  @Test
  void shouldCallValidator_whenMetricMatches() {
    RequestValidator validator = mock(RequestValidator.class);
    when(validator.supportedMetrics()).thenReturn(List.of(CalculationMetric.TRAILING_TOTAL_RETURNS));
    doNothing().when(validator).validate(any());

    RequestValidationFacade facade = new RequestValidationFacade(List.of(validator));
    PeriodCommand command = periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS);

    facade.validate(command);

    verify(validator).validate(command);
  }

  @Test
  void shouldCollectAllErrors_whenMultipleValidatorsFail() {
    RequestValidator validator1 = mock(RequestValidator.class);
    RequestValidator validator2 = mock(RequestValidator.class);
    when(validator1.supportedMetrics()).thenReturn(List.of(CalculationMetric.TRAILING_TOTAL_RETURNS));
    when(validator2.supportedMetrics()).thenReturn(List.of(CalculationMetric.TRAILING_TOTAL_RETURNS));

    ValidationException error1 = ErrorCode.TIME_INTERVAL_PERIOD_LESS_THAN_12.toValidationException();
    ValidationException error2 = ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE.toValidationException();
    doThrow(error1).when(validator1).validate(any());
    doThrow(error2).when(validator2).validate(any());

    RequestValidationFacade facade = new RequestValidationFacade(List.of(validator1, validator2));
    PeriodCommand command = periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS);

    assertThatThrownBy(() -> facade.validate(command))
        .isInstanceOf(CalculationsFailedException.class)
        .satisfies(ex -> {
          CalculationsFailedException composite = (CalculationsFailedException) ex;
          assertThat(composite.getExceptions()).hasSize(2);
          assertThat(composite.getExceptions()).containsExactly(error1, error2);
        });
  }

  @Test
  void shouldNotThrow_whenAllValidatorsPass() {
    RequestValidator validator1 = mock(RequestValidator.class);
    RequestValidator validator2 = mock(RequestValidator.class);
    when(validator1.supportedMetrics()).thenReturn(List.of(CalculationMetric.SHARPE_RATIO));
    when(validator2.supportedMetrics()).thenReturn(List.of(CalculationMetric.SHARPE_RATIO));
    doNothing().when(validator1).validate(any());
    doNothing().when(validator2).validate(any());

    RequestValidationFacade facade = new RequestValidationFacade(List.of(validator1, validator2));
    PeriodCommand command = periodCommand(CalculationMetric.SHARPE_RATIO);

    assertThatCode(() -> facade.validate(command))
        .doesNotThrowAnyException();

    verify(validator1).validate(command);
    verify(validator2).validate(command);
  }

  @Test
  void shouldOnlyCallValidatorsForRequestedMetric() {
    RequestValidator trailingValidator = mock(RequestValidator.class);
    RequestValidator sharpeValidator = mock(RequestValidator.class);
    when(trailingValidator.supportedMetrics()).thenReturn(List.of(CalculationMetric.TRAILING_TOTAL_RETURNS));
    when(sharpeValidator.supportedMetrics()).thenReturn(List.of(CalculationMetric.SHARPE_RATIO));

    RequestValidationFacade facade = new RequestValidationFacade(List.of(trailingValidator, sharpeValidator));
    PeriodCommand command = periodCommand(CalculationMetric.TRAILING_TOTAL_RETURNS);

    facade.validate(command);

    verify(trailingValidator).validate(command);
    verify(sharpeValidator, never()).validate(any(CalculationCommand.class));
  }

  private static PeriodCommand periodCommand(CalculationMetric metric) {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(metric);
    return command;
  }
}
