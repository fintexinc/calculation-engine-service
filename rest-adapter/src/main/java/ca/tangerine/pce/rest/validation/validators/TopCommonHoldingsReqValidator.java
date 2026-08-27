package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

import static ca.tangerine.pce.model.domain.enumeration.CalculationMetric.TOP_COMMON_HOLDINGS;
import static ca.tangerine.pce.util.FilterUtils.GIC_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.filterHoldings;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.TopCommonHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.rest.validation.RequestValidator;

@Component
@Order(500)
@RequiredArgsConstructor
public class TopCommonHoldingsReqValidator implements RequestValidator {

  private final HoldingsValidator holdingsValidator;

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(TOP_COMMON_HOLDINGS);
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof TopCommonHoldingsCommand tch)) {
      return;
    }
    // The accumulate types used to be free-form strings capped at twelve entries; they are now HoldingType, so the set
    // cannot hold more members than the provider's vocabulary has and an unrecognised code is a 400 from
    // deserialization — naming the field and the accepted values — instead of a value that silently matches no holding.
    if (checkGicHoldingName(tch.getHoldings())) {
      throw ErrorCode.GIC_HOLDING_NAME_EMPTY.toValidationException();
    }
    holdingsValidator.validateHoldingValues(tch.getHoldings());
  }

  private boolean checkGicHoldingName(List<PortfolioHolding> holdings) {
    List<GicHolding> gicHoldings = filterHoldings(holdings, GIC_PREDICATE);
    long holdingsWithoutName = gicHoldings.stream()
        .filter(h -> Objects.isNull(h.getName()) || h.getName().isEmpty())
        .count();
    return holdingsWithoutName != 0;
  }
}
