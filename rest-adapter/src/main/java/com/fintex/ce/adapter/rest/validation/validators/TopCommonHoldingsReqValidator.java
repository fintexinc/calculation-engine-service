package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Component
@Order(500)
public class TopCommonHoldingsReqValidator implements RequestValidator {

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(TOP_COMMON_HOLDINGS);
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof TopCommonHoldingsCommand tch)) {
      return;
    }
    int sizeOfAccumulateHoldingTypes = CollectionUtils.isEmpty(tch.getAccumulateHoldingTypes())
        ? 0
        : tch.getAccumulateHoldingTypes().size();
    if (Optional.ofNullable(tch.getNumOfFundsMin()).orElse(1) < 1) {
      throw ErrorCode.NUM_OF_FUNDS_MIN_NOT_POSITIVE.toValidationException();
    }
    if (Objects.nonNull(tch.getNumOfFundsMin()) && tch.getNumOfFundsMin() > tch.getHoldings().size()) {
      throw ErrorCode.NUM_OF_FUNDS_EXCEEDS_PORTFOLIO.toValidationException();
    }
    if (sizeOfAccumulateHoldingTypes > 12) {
      throw ErrorCode.ACCUMULATE_HOLDING_TYPES_EXCEED_MAX.toValidationException();
    }
    if (checkGicHoldingName(tch.getHoldings())) {
      throw ErrorCode.GIC_HOLDING_NAME_EMPTY.toValidationException();
    }
  }

  private boolean checkGicHoldingName(List<PortfolioHolding> holdings) {
    List<GicHolding> gicHoldings = filterHoldings(holdings, GIC_PREDICATE);
    long holdingsWithoutName = gicHoldings.stream()
        .filter(h -> Objects.isNull(h.getName()) || h.getName().isEmpty())
        .count();
    return holdingsWithoutName != 0;
  }
}
