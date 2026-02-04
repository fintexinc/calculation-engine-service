package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.StringDatapoint;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Log4j2
public abstract class EtfAbstractEndpoint<Res> extends AbstractSMEndpoint<EtfHolding, String, EtfQuery, Etf, Res> {

  public EtfAbstractEndpoint(final Function<Query, List<Etf>> getSMEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers,
      final UnaryOperator<EtfQuery> preDefinedSMQuery) {
    return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedSMQuery::apply);
  }

  @Override
  public List<String> collectIds(final List<EtfHolding> holdings) {
    return holdings.stream().map(EtfHolding::getTicker).collect(Collectors.toList());
  }

  // TODO: throw ERROR message that ticker in request is not correct (to reproduce NPE use payload with wrong ticker)
  @Override
  public EtfHolding findHoldingBasedOnRes(final List<EtfHolding> holdings, final Etf etf) {
    final String ticker = Objects.requireNonNull(etf.getTicker().getValue());
    return holdings.stream().filter(holding -> ticker.equals(holding.getTicker())).findFirst().orElseThrow();
  }

  @Override
  public void populateEmptyResponseWithIdentifier(final List<Etf> responses, final EtfHolding etfHolding) {
    final Etf etf = responses.stream()
        .filter(r -> isNull(r.getTicker()) || isNull(r.getTicker().getValue()))
        .findFirst().orElseThrow();
    final StringDatapoint tickerDatapoint = new StringDatapoint().setValue(etfHolding.getTicker());
    etf.setTicker(tickerDatapoint);
  }

  /**
   * returns tickers that don't exist
   *
   * @param holdings
   *          holdings from request payload
   * @param responses
   *          all not null tickers from FDS response
   * @return not existing tickers
   */
  public List<EtfHolding> getNotExistingHoldings(final List<EtfHolding> holdings,
      final List<Etf> responses) {
    final List<String> tickersFromResponse = getTickersFromResponse(responses);
    return holdings.stream()
        .filter(h -> !tickersFromResponse.contains(h.getTicker()))
        .collect(Collectors.toList());
  }

  /**
   * returns list of not null tickers from FDS response
   *
   * @param responses
   *          response data from FDS
   * @return tickers from FDS response
   */
  public List<String> getTickersFromResponse(final List<Etf> responses) {
    return responses.stream()
        .filter(r -> nonNull(r.getTicker()) && nonNull(r.getTicker().getValue()))
        .map(etf -> etf.getTicker().getValue())
        .collect(Collectors.toList());
  }

}
