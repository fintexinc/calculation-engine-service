package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.result.income.YieldResult;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class YieldResponseMapper implements ResponseMapper<Yield, YieldResult> {

  @Override
  public YieldResult toResponse(Yield domain) {
    YieldResult response = new YieldResult();
    if (domain != null) {
      response.setYield(domain.getDividendYield());
    }
    return response;
  }

  @Override
  public YieldResult toResponse(Map<Holding, Yield> domainMap, List<Warning> warnings) {
    BigDecimal weightedYield = calculateWeightedAverageYield(domainMap);
    YieldResult response = new YieldResult();
    response.setYield(weightedYield);
    response.setWarnings(warnings);
    return response;
  }

  private BigDecimal calculateWeightedAverageYield(Map<Holding, Yield> holdingYieldMap) {
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalWeight = BigDecimal.ZERO;

    for (Map.Entry<Holding, Yield> entry : holdingYieldMap.entrySet()) {
      Holding holding = entry.getKey();
      Yield yield = entry.getValue();

      if (yield == null || yield.getDividendYield() == null || holding.getValue() == null) {
        continue;
      }

      BigDecimal dividendYield = getDividendYield(holding, yield);
      BigDecimal weight = holding.getValue();

      weightedSum = weightedSum.add(dividendYield.multiply(weight));
      totalWeight = totalWeight.add(weight);
    }

    return (totalWeight.compareTo(BigDecimal.ZERO) > 0)
        ? DecimalUtils.divide(weightedSum, totalWeight)
        : BigDecimal.ZERO;
  }

  private BigDecimal getDividendYield(Holding holding, Yield yield) {
    if (Objects.nonNull(yield.getDividendYield()) && FinancialInstrumentType.GIC.equals(holding.getHoldingType())) {
      return DecimalUtils.divide(yield.getDividendYield(), new BigDecimal(100));
    }
    return yield.getDividendYield();
  }
}
