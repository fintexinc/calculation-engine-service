package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.smclient.graphql.ExternalIdentifierType;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.smclient.graphql.FixedIncomeQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.FixedIncomeHolding;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class FixedIncomeAbstractEndpoint<Res>
    extends
      AbstractSMEndpoint<FixedIncomeHolding, String, FixedIncomeQuery, FixedIncome, Res> {

  public FixedIncomeAbstractEndpoint(final Function<Query, List<FixedIncome>> getSMEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public List<String> collectIds(final List<FixedIncomeHolding> holdings) {
    return holdings.stream()
        .map(FixedIncomeHolding::getIdentifier)
        .collect(Collectors.toList());
  }

  @Override
  public FixedIncomeHolding findHoldingBasedOnRes(final List<FixedIncomeHolding> holdings,
      final FixedIncome fixedIncome) {
    final List<String> ids = getIds(fixedIncome);
    return holdings.stream()
        .filter(holding -> ids.contains(holding.getIdentifier()))
        .findFirst()
        .orElseThrow();
  }

  /**
   * Collect ids from the FDS response
   *
   * @param fixedIncome
   *          FixedIncome object from FDS
   * @return ids
   */
  private List<String> getIds(final FixedIncome fixedIncome) {
    final List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(fixedIncome.getExternalIdentifiers())
        .getCodes();
    return Objects.requireNonNull(codes).stream()
        .map(ExternalIdentifierTypeValue::getValue)
        .collect(Collectors.toList());
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<String> adpNumbers,
      final UnaryOperator<FixedIncomeQuery> preDefinedSMQuery) {
    return q -> q.getFixedIncomeByBroadridgeAdpNumbers(adpNumbers, preDefinedSMQuery::apply);
  }

  @Override
  public void populateEmptyResponseWithIdentifier(final List<FixedIncome> responses,
      final FixedIncomeHolding holding) {
    final FixedIncome fixedIncome = responses.stream()
        .filter(r -> isNull(r.getExternalIdentifiers()) || isEmpty(r.getExternalIdentifiers().getCodes()))
        .findFirst()
        .orElseThrow();

    final var adpNumber = new ExternalIdentifierTypeValue()
        .setType(ExternalIdentifierType.BROADRIDGE_ADP_NUMBER)
        .setValue(holding.getIdentifier());
    var externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(adpNumber));
    fixedIncome.setExternalIdentifiers(externalIdentifiers);
  }

  @Override
  public List<FixedIncomeHolding> getNotExistingHoldings(final List<FixedIncomeHolding> holdings,
      final List<FixedIncome> fixedIncomes) {
    final List<String> adpNumbers = getAdpNumbersFromResponse(fixedIncomes);
    return holdings.stream()
        .filter(h -> !adpNumbers.contains(h.getIdentifier()))
        .collect(Collectors.toList());
  }

  /**
   * returns list of not null adpNumbers from FDS response
   *
   * @param fixedIncomes
   *          response data from FDS
   * @return adpNumbers from FDS response
   */
  private List<String> getAdpNumbersFromResponse(final List<FixedIncome> fixedIncomes) {
    return fixedIncomes.stream()
        .filter(r -> nonNull(r.getExternalIdentifiers()) && Objects.nonNull(r.getExternalIdentifiers().getCodes()))
        .map(r -> getAdpNumber(r.getExternalIdentifiers()))
        .collect(Collectors.toList());
  }

  private String getAdpNumber(final ExternalIdentifiers externalIdentifiers) {
    return externalIdentifiers.getCodes()
        .stream()
        .filter(externalIdentifier -> Objects.equals(externalIdentifier.getType(),
            ExternalIdentifierType.BROADRIDGE_ADP_NUMBER))
        .map(ExternalIdentifierTypeValue::getValue)
        .findFirst()
        .orElse(null);
  }

}
