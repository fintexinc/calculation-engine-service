package com.fintex.ce.config.constant.graphql;

import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.HedgeFund;
import com.fintex.smclient.graphql.Index;
import com.fintex.smclient.graphql.PagGuidedPortfolio;
import com.fintex.smclient.graphql.PooledFund;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.UsFund;

import java.util.List;
import java.util.function.Function;

public class GraphQlEndpointConstants {

    public static final Function<Query, List<Etf>> GET_CANADA_ETFS_BY_TICKERS = Query::getGetCanadaEtfsByTickers;
    public static final Function<Query, List<Etf>> GET_US_ETFS_BY_TICKERS = Query::getGetUsEtfsByTickers;
    public static final Function<Query, List<FundSeries>> GET_FUND_SERIES_BY_HOLDING_CODES = Query::getGetFundSeriesByHoldingCodes;
    public static final Function<Query, List<Index>> GET_GET_INDEXES_BY_MORNINGSTAR_IDS = Query::getGetIndexesByMorningstarIds;
    public static final Function<Query, List<Stock>> GET_STOCKS_BY_TICKERS_AND_EXCHANGE_IDS = Query::getGetStocksByTickersAndExchangeIds;
    public static final Function<Query, List<UsFund>> GET_US_FUND_BY_TICKERS = Query::getGetUsFundsByTickers;
    public static final Function<Query, List<PooledFund>> GET_CANADA_POOLED_FUNDS_BY_MORNINGSTAR_IDS = Query::getGetCanadaPooledFundsByMorningstarIds;
    public static final Function<Query, List<HedgeFund>> GET_CANADA_HEDGE_FUNDS_BY_MORNINGSTAR_IDS = Query::getGetCanadaHedgeFundsByMorningstarIds;
    public static final Function<Query, List<FixedIncome>> GET_FIXED_INCOME_BY_ADP_NUMBERS = Query::getGetFixedIncomeByBroadridgeAdpNumbers;
    public static final Function<Query, List<SeparatelyManagedAccount>> GET_SEPARATELY_MANAGED_ACCOUNT_BY = Query::getGetSeparatelyManagedAccountsBy;
    public static final Function<Query, List<PagGuidedPortfolio>> GET_PAG_GUIDED_PORTFOLIOS = Query::getGetPagGuidedPortfolios;

    private GraphQlEndpointConstants() {
    }

}
