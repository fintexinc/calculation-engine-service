package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import lombok.RequiredArgsConstructor;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.COMMON_PERFORMANCE_DATES;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.rest.validation.RequestValidator;

@Component
@Order(400)
@RequiredArgsConstructor
public class CommonPerformanceDatesReqValidator implements RequestValidator {

  private final HoldingsValidator holdingsValidator;

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
      holdingsValidator.validate(benchmarkHoldings);
    }
    if (!CollectionUtils.isEmpty(mpc.getPortfolios())) {
      mpc.getPortfolios().stream()
          .filter(portfolio -> !portfolio.getHoldings().isEmpty())
          .forEach(portfolio -> holdingsValidator.validate(portfolio.getHoldings()));
    }
  }
}
