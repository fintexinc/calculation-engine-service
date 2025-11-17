package com.fintex.ce.dto.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.core.PortfolioReqDTO;

import java.math.BigDecimal;
import java.util.List;

public class RollingCorrelationCalculationReqDTO extends RollingCalculationReqDTO {

    @Override
    public PortfolioReqDTO setHoldings(List<Holding> holdings) {
        holdings.forEach(holding -> holding.setValue(BigDecimal.ONE));
        return super.setHoldings(holdings);
    }

    @Override
    public PortfolioReqDTO setBenchmarkHoldings(List<Holding> benchmarkHoldings) {
        benchmarkHoldings.forEach(holding -> holding.setValue(BigDecimal.ONE));
        return super.setBenchmarkHoldings(benchmarkHoldings);
    }
}
