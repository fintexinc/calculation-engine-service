package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastStockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class IncomeForecastGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<IncomeForecast> {

  @Autowired
  public IncomeForecastGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, IncomeForecast> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_HEDGE_FUNDS -> new IncomeForecastCanadaHedgeFundEndpoint();
      case US_MUTUAL_FUNDS -> new IncomeForecastCanadaUsMutualFundEndpoint();
      case CANADA_ETF -> new IncomeForecastEtfCanadaEndpoint();
      case US_ETF -> new IncomeForecastEtfUsEndpoint();
      case FIXED_INCOME -> new IncomeForecastFixedIncomeEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new IncomeForecastFundCanadaEndpoint();
      case CANADA_POOLED_FUNDS -> new IncomeForecastPooledFundEndpoint();
      case CANADA_STOCKS, US_STOCKS -> new IncomeForecastStockEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, IncomeForecast> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, IncomeForecast> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, IncomeForecast> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastEtfUsEndpoint(), providers);
  }

    public Map<CanadaPooledFundHolding, IncomeForecast> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastPooledFundEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, IncomeForecast> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastCanadaUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, IncomeForecast> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastCanadaHedgeFundEndpoint(), providers);
  }

    public Map<FixedIncomeHolding, IncomeForecast> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastFixedIncomeEndpoint(), providers);
  }

    public Map<StockHolding, IncomeForecast> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastStockEndpoint(), providers);
  }

}
