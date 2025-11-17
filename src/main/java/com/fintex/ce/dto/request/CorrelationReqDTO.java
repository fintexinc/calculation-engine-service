package com.fintex.ce.dto.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.core.PortfolioReqDTO;

import java.math.BigDecimal;
import java.util.List;

public class CorrelationReqDTO extends PeriodsReqDTO {

    @Override
    public PortfolioReqDTO setHoldings(final List<Holding> holdings) {
        holdings.forEach(holding -> holding.setValue(BigDecimal.ONE));
        return super.setHoldings(holdings);
    }
}
