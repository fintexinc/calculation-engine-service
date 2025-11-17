package com.fintex.ce.dto.request;

import com.fintex.ce.dto.holding.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
public class MultiplePortfoliosReqDTO {

    private Set<Portfolio> portfolios;
    private List<Holding> benchmarkHoldings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Portfolio {

        private List<Holding> holdings;

    }

}
