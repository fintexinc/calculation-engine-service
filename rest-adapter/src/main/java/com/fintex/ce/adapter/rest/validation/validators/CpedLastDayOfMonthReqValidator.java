package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.contract.CustomPedProvider;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(201)
public class CpedLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<CustomPedProvider> {

  public CpedLastDayOfMonthReqValidator() {
    super(CustomPedProvider.class, CustomPedProvider::getCustomPed, ErrorCode.CPED_NOT_MONTH_END);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        TRAILING_TOTAL_RETURNS, STANDARD_DEVIATION, SHARPE_RATIO, MAX_DRAWDOWN, ANNUAL_RETURNS, GROWTH_OF_10K);
  }
}
