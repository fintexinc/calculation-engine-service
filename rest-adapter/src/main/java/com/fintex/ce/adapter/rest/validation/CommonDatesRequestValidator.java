package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Component
public class CommonDatesRequestValidator {

  public void validate(final List<Holding> benchmarkHoldings,
      final Set<MultiplePortfoliosReqDTO.Portfolio> portfolios) {
    if (!CollectionUtils.isEmpty(benchmarkHoldings)) {
      buildHoldingReqValidation(benchmarkHoldings).check();
    }
    if (!CollectionUtils.isEmpty(portfolios)) {
      portfolios.stream()
          .filter(e -> !e.getHoldings().isEmpty())
          .forEach(portfolioHoldings -> buildHoldingReqValidation(portfolioHoldings.getHoldings()).check());
    }
  }

  public HoldingReqValidation buildHoldingReqValidation(final List<Holding> holdings) {
    return new HoldingReqValidation(holdings);
  }
}
