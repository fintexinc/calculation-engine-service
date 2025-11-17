package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Component
public class CommonDatesRequestValidator {

    public void validate(final List<Holding> benchmarkHoldings, final Set<MultiplePortfoliosReqDTO.Portfolio> portfolios) {
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
