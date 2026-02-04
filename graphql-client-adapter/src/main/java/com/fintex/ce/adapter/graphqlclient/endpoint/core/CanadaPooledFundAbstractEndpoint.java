package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaPooledFundHolding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.fintex.smclient.graphql.ExternalIdentifierType.MORNINGSTAR_ID;

/**
 * @param <Res>
 *          response object
 */
public abstract class CanadaPooledFundAbstractEndpoint<Res>
    extends
      AbstractSMEndpoint<CanadaPooledFundHolding, String, PooledFundQuery, PooledFund, Res> {

  public CanadaPooledFundAbstractEndpoint(Function<Query, List<PooledFund>> getSMEntityFunction,
      List<DataProvider> supportedProviders,
      String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> tickers, UnaryOperator<PooledFundQuery> preDefinedSMQuery) {
    return q -> q.getCanadaPooledFundsByMorningstarIds(tickers, preDefinedSMQuery::apply);
  }

  @Override
  public List<String> collectIds(List<CanadaPooledFundHolding> holdings) {
    var result = holdings.stream().map(CanadaPooledFundHolding::getMorningstarId).collect(Collectors.toList());
    return result;
  }

  @Override
  CanadaPooledFundHolding findHoldingBasedOnRes(List<CanadaPooledFundHolding> holdings, PooledFund fund) {
    List<String> ids = getIds(fund);
    return holdings.stream().filter(holding -> ids.contains(holding.getMorningstarId())).findFirst().orElseThrow();
  }

  List<String> getIds(PooledFund fund) {
    List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(fund.getExternalIdentifiers()).getCodes();
    return Objects.requireNonNull(codes).stream().map(ExternalIdentifierTypeValue::getValue).collect(Collectors
        .toList());
  }

  @Override
  protected void populateEmptyResponseWithIdentifier(List<PooledFund> responses, CanadaPooledFundHolding holding) {
    var pooledFund = responses.stream()
        .filter(this::isWithoutMorningstarIdInExternalIdentifiers)
        .findFirst().orElseThrow();
    var codeWithMorningstarId = new ExternalIdentifierTypeValue()
        .setType(ExternalIdentifierType.MORNINGSTAR_ID)
        .setValue(holding.getMorningstarId());
    var externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(codeWithMorningstarId));
    pooledFund.setExternalIdentifiers(externalIdentifiers);
  }

  private boolean isWithoutMorningstarIdInExternalIdentifiers(PooledFund r) {
    var externalIdentifiers = r.getExternalIdentifiers();
    if (externalIdentifiers != null && externalIdentifiers.getCodes() != null) {
      return externalIdentifiers.getCodes()
          .stream()
          .noneMatch(c -> c.getType() == MORNINGSTAR_ID);
    }

    return true;
  }

  @Override
  protected List<CanadaPooledFundHolding> getNotExistingHoldings(List<CanadaPooledFundHolding> holdings,
      List<PooledFund> responses) {
    List<String> morningstarIds = getMorningstarIds(responses);
    return holdings.stream()
        .filter(h -> !morningstarIds.contains(h.getMorningstarId()))
        .collect(Collectors.toList());
  }

  private List<String> getMorningstarIds(List<PooledFund> responses) {
    List<String> result = new ArrayList<>();
    for (PooledFund r : responses) {
      var externalIdentifiers = r.getExternalIdentifiers();
      if (externalIdentifiers != null && externalIdentifiers.getCodes() != null) {
        for (ExternalIdentifierTypeValue code : externalIdentifiers.getCodes()) {
          if (code.getType() == MORNINGSTAR_ID && code.getValue() != null) {
            result.add(code.getValue());
          }
        }
      }
    }
    return result;
  }
}
