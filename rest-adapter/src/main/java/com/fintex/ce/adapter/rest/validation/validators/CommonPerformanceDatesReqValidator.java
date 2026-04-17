package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(400)
public class CommonPerformanceDatesReqValidator implements RequestValidator {

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(COMMON_PERFORMANCE_DATES);
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof MultiplePortfoliosCommand mpc)) {
      return;
    }
    List<PortfolioHolding> benchmarkHoldings = mpc.getBenchmarkHoldings();
    if (!CollectionUtils.isEmpty(benchmarkHoldings)) {
      HoldingsValidationHelper.validate(benchmarkHoldings);
    }
    if (!CollectionUtils.isEmpty(mpc.getPortfolios())) {
      mpc.getPortfolios().stream()
          .filter(portfolio -> !portfolio.getHoldings().isEmpty())
          .forEach(portfolio -> HoldingsValidationHelper.validate(portfolio.getHoldings()));
    }
  }
}
