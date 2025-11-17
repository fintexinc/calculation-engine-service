package com.fintex.ce.config.enumeration.cache;

/**
 * Caches could be:
 * - MER_{@this}
 * - MONTHLY_RETURNS_{@this}
 * - MER_{@this}
 * - MONTHLY_RETURNS_{@this}
 * etc..
 */
public enum CacheCategory {

    CANADA_MUTUAL_FUNDS,

    ETF,
    US_ETF,
    CANADA_ETF,

    STOCKS,
    CANADA_STOCKS,
    US_STOCKS,

    BENCHMARK_INDEXES,

    US_MUTUAL_FUNDS,
    CANADA_HEDGE_FUNDS,
    CANADA_POOLED_FUNDS,
    FIXED_INCOME,
    SEPARATELY_MANAGED_ACCOUNT,
    PAG_GUIDED_PORTFOLIO;

    public boolean isMutualFund() {
        return this == CANADA_MUTUAL_FUNDS;
    }

    public boolean isEtf() {
        return this == ETF || this == US_ETF || this == CANADA_ETF;
    }

    public boolean isStock() {
        return this == STOCKS || this == US_STOCKS || this == CANADA_STOCKS;
    }

    public boolean isBenchmark() {
        return this == BENCHMARK_INDEXES;
    }

    public boolean isUsMutualFund() {
        return this == US_MUTUAL_FUNDS;
    }

    public boolean isCanadaPooledFund() {
        return this == CANADA_POOLED_FUNDS;
    }

    public boolean isCanadaHedgeFund() {
        return this == CANADA_HEDGE_FUNDS;
    }

    public boolean isFixedIncome() {
        return this == FIXED_INCOME;
    }

    public boolean isSeparatelyManagedAccount() {
        return this == SEPARATELY_MANAGED_ACCOUNT;
    }

    public boolean isPagGuidedPortfolio() {
        return this == PAG_GUIDED_PORTFOLIO;
    }
}
