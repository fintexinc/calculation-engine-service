package com.fintex.ce.service.impl.cache.core;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;

import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.constant.GeneralConstants.METHOD_NOT_IMPLEMENTED;

/**
 * @param <F> Fund Canada
 * @param <C> ETF Canada
 * @param <U> ETF US
 * @param <S> Stock
 */
public interface MultipleCacheStorage<F, C, U, S> {

    /**
     * For only for CANADA_MUTUAL_FUND
     *
     * @return mapped object
     */
    Map<FundSeriesHolding, F> loadBenchOfFundCanada(final List<FundSeriesHolding> holdings, final List<DataProvider> providers);

    default Map<UsMutualFundHolding, F> loadUsMutualFunds(final List<UsMutualFundHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    /**
     * Could work for both Canada & US ETFs
     *
     * @return mapped object
     */
    default Map<EtfHolding, C> loadForBenchOfEtf(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    /**
     * Could work for both Canada & US Stocks
     *
     * @return mapped object
     */
    default Map<StockHolding, S> loadForBenchOfStock(final List<StockHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<CanadaHedgeFundHolding, F> loadCanadaHedgeFunds(List<CanadaHedgeFundHolding> holdings, List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<EtfHolding, C> loadForBenchOfEtfCanada(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<EtfHolding, U> loadForBenchOfEtfUs(final List<EtfHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<BenchmarkIndexHolding, F> loadForBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<CanadaPooledFundHolding, F> loadCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<FixedIncomeHolding, F> loadBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<SmaHolding, F> loadBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

    default Map<PagHolding, F> loadBenchOfPagGuidedPortfolios(final List<PagHolding> holdings, final List<DataProvider> providers) {
        throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
    }

}
