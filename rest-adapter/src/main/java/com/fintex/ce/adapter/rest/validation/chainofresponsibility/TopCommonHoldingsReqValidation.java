package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@EqualsAndHashCode(callSuper = true)
public class TopCommonHoldingsReqValidation extends ReqValidation {

  private final TopCommonHoldingsReqDTO reqDTO;

  public TopCommonHoldingsReqValidation(final TopCommonHoldingsReqDTO reqDTO) {
    this.reqDTO = reqDTO;
  }

  @Override
  public void check() {
    final int sizeOfAccumulateHoldingTypes = CollectionUtils.isEmpty(reqDTO.getAccumulateHoldingTypes())
        ? 0
        : reqDTO.getAccumulateHoldingTypes().size();
    if (Optional.ofNullable(reqDTO.getNumOfFundsMin()).orElse(1) < 1) {
      throw ExceptionCode.ERR_TCH_NFM_001.reqValidationError();
    }
    if (Objects.nonNull(reqDTO.getNumOfFundsMin()) && reqDTO.getNumOfFundsMin() > reqDTO.getHoldings().size()) {
      throw ExceptionCode.ERR_TCH_NFM_002.reqValidationError();
    }
    if (sizeOfAccumulateHoldingTypes > 12) {
      throw ExceptionCode.ERR_TCH_AHT_001.reqValidationError();
    }
    if (checkGicHoldingName(reqDTO.getHoldings())) {
      throw ExceptionCode.ERR_TCH_GNM_003.reqValidationError();
    }
  }

  private boolean checkGicHoldingName(List<Holding> holdings) {
    final List<GicHolding> gicHoldings = filterHoldings(holdings, GIC_PREDICATE);
    final long holdingsWithoutName = gicHoldings.stream().filter(h -> Objects.isNull(h.getName()) || h.getName()
        .isEmpty()).count();
    return holdingsWithoutName != 0;
  }
}
