package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;
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
      throw ErrorCode.ERR_TCH_NFM_001.reqValidationError();
    }
    if (Objects.nonNull(tch.getNumOfFundsMin()) && tch.getNumOfFundsMin() > tch.getHoldings().size()) {
      throw ErrorCode.ERR_TCH_NFM_002.reqValidationError();
    }
    if (sizeOfAccumulateHoldingTypes > 12) {
      throw ErrorCode.ERR_TCH_AHT_001.reqValidationError();
    }
    if (checkGicHoldingName(tch.getHoldings())) {
      throw ErrorCode.ERR_TCH_GNM_003.reqValidationError();
    }
  }

  private boolean checkGicHoldingName(List<Holding> holdings) {
    List<GicHolding> gicHoldings = filterHoldings(holdings, GIC_PREDICATE);
    long holdingsWithoutName = gicHoldings.stream()
        .filter(h -> Objects.isNull(h.getName()) || h.getName().isEmpty())
        .count();
    return holdingsWithoutName != 0;
  }
}
