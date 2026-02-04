package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastCanadaUsMutualFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastFixedIncomeEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastPooledFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastStockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IncomeForecastSMRepository
    extends
      MultipleSMAbstractRepository<IncomeForecast, IncomeForecast, IncomeForecast, IncomeForecast> {

  @Autowired
  public IncomeForecastSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, IncomeForecast> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, IncomeForecast> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, IncomeForecast> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastEtfUsEndpoint(), providers);
  }

  @Override
  public Map<CanadaPooledFundHolding, IncomeForecast> queryCanadaPooledFunds(
      final List<CanadaPooledFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastPooledFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, IncomeForecast> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastCanadaUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, IncomeForecast> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<FixedIncomeHolding, IncomeForecast> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastFixedIncomeEndpoint(), providers);
  }

  @Override
  public Map<StockHolding, IncomeForecast> queryBenchOfStock(final List<StockHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new IncomeForecastStockEndpoint(), providers);
  }

}
