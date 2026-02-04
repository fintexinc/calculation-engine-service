package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @param <Res>
 *          response object
 */
public abstract class FundAbstractEndpoint<Res>
    extends
      AbstractSMEndpoint<FundSeriesHolding, FundHoldingIdentifiersCodes, FundSeriesQuery, FundSeries, Res> {

  public FundAbstractEndpoint(final Function<Query, List<FundSeries>> getSMEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<FundHoldingIdentifiersCodes> identifiersCodes,
      UnaryOperator<FundSeriesQuery> preDefinedSMQuery) {
    return q -> q.getFundSeriesByHoldingCodes(identifiersCodes, preDefinedSMQuery::apply);
  }

  @Override
  public List<FundHoldingIdentifiersCodes> collectIds(final List<FundSeriesHolding> holdings) {
    return holdings.stream()
        .map(h -> new FundHoldingIdentifiersCodes(FundHoldingIdentifier.fromGraphQl(h.getHoldingIdentifier().name()), h
            .getFundServCode()))
        .collect(Collectors.toList());
  }

  @Override
  public FundSeriesHolding findHoldingBasedOnRes(final List<FundSeriesHolding> holdings, final FundSeries fundSeries) {
    final List<String> ids = getIds(fundSeries);
    return holdings.stream().filter(holding -> ids.contains(holding.getFundServCode())).findFirst().orElseThrow();
  }

  public List<String> getIds(final FundSeries fundSeries) {
    final List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(fundSeries.getExternalIdentifiers())
        .getCodes();
    return Objects.requireNonNull(codes).stream().map(ExternalIdentifierTypeValue::getValue).collect(Collectors
        .toList());
  }

  /**
   * this method should never be used by FundAbstractEndpoint
   *
   * @param holdings
   *          holdings from request payload
   * @param responses
   *          all not null tickers from FDS response
   * @return
   */
  @Override
  protected List<FundSeriesHolding> getNotExistingHoldings(final List<FundSeriesHolding> holdings,
      final List<FundSeries> responses) {
    throw new UnsupportedOperationException();
  }

  /**
   * this method should never be used by FundAbstractEndpoint
   *
   * @param responses
   * @param holding
   */
  @Override
  public void populateEmptyResponseWithIdentifier(final List<FundSeries> responses, final FundSeriesHolding holding) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<FundSeries> populateIdentifiersIfEmpty(final List<FundSeriesHolding> holdings,
      final List<FundSeries> responses) {
    return responses;
  }
}
