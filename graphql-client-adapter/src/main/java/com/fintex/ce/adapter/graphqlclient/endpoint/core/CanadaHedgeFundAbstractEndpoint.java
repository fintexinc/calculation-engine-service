package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.smclient.graphql.*;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;

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
public abstract class CanadaHedgeFundAbstractEndpoint<Res>
    extends
      AbstractSMEndpoint<CanadaHedgeFundHolding, String, HedgeFundQuery, HedgeFund, Res> {

  public CanadaHedgeFundAbstractEndpoint(Function<Query, List<HedgeFund>> getSMEntityFunction,
      List<DataProvider> supportedProviders,
      String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public QueryQueryDefinition queryDefinition(List<String> tickers, UnaryOperator<HedgeFundQuery> preDefinedSMQuery) {
    return q -> q.getCanadaHedgeFundsByMorningstarIds(tickers, preDefinedSMQuery::apply);
  }

  @Override
  public List<String> collectIds(List<CanadaHedgeFundHolding> holdings) {
    return holdings.stream().map(CanadaHedgeFundHolding::getMorningstarId).collect(Collectors.toList());
  }

  @Override
  CanadaHedgeFundHolding findHoldingBasedOnRes(List<CanadaHedgeFundHolding> holdings, HedgeFund fund) {
    List<String> ids = getIds(fund);
    return holdings.stream().filter(holding -> ids.contains(holding.getMorningstarId())).findFirst().orElseThrow();
  }

  List<String> getIds(HedgeFund fund) {
    List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(fund.getExternalIdentifiers()).getCodes();
    return Objects.requireNonNull(codes).stream().map(ExternalIdentifierTypeValue::getValue).collect(Collectors
        .toList());
  }

  @Override
  protected void populateEmptyResponseWithIdentifier(List<HedgeFund> responses, CanadaHedgeFundHolding holding) {
    var pooledFund = responses.stream()
        .filter(this::isWithoutMorningstarIdInExternalIdentifiers)
        .findFirst().orElseThrow();
    var codeWithMorningstarId = new ExternalIdentifierTypeValue()
        .setType(ExternalIdentifierType.MORNINGSTAR_ID)
        .setValue(holding.getMorningstarId());
    var externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(codeWithMorningstarId));
    pooledFund.setExternalIdentifiers(externalIdentifiers);
  }

  private boolean isWithoutMorningstarIdInExternalIdentifiers(HedgeFund r) {
    var externalIdentifiers = r.getExternalIdentifiers();
    if (externalIdentifiers != null && externalIdentifiers.getCodes() != null) {
      return externalIdentifiers.getCodes()
          .stream()
          .noneMatch(c -> c.getType() == MORNINGSTAR_ID);
    }

    return true;
  }

  @Override
  protected List<CanadaHedgeFundHolding> getNotExistingHoldings(List<CanadaHedgeFundHolding> holdings,
      List<HedgeFund> responses) {
    List<String> morningstarIds = getMorningstarIds(responses);
    return holdings.stream()
        .filter(h -> !morningstarIds.contains(h.getMorningstarId()))
        .collect(Collectors.toList());
  }

  private List<String> getMorningstarIds(List<HedgeFund> responses) {
    List<String> result = new ArrayList<>();
    for (HedgeFund r : responses) {
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
