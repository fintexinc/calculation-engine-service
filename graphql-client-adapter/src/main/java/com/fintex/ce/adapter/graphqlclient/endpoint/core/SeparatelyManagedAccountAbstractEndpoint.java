package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.smclient.graphql.ExternalIdentifierType;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.smclient.graphql.SmaIdentifier;
import com.fintex.smclient.graphql.SmaIdentifierType;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.SmaHolding;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class SeparatelyManagedAccountAbstractEndpoint<Res>
    extends
      AbstractSMEndpoint<SmaHolding, SmaIdentifier, SeparatelyManagedAccountQuery, SeparatelyManagedAccount, Res> {

  public SeparatelyManagedAccountAbstractEndpoint(
      final Function<Query, List<SeparatelyManagedAccount>> getSMEntityFunction,
      final List<DataProvider> supportedProviders,
      final String endpointName) {
    super(getSMEntityFunction, supportedProviders, endpointName);
  }

  @Override
  public List<SmaIdentifier> collectIds(final List<SmaHolding> holdings) {
    return holdings.stream()
        .map(this::getSmaIdentifier)
        .collect(toList());
  }

  private SmaIdentifier getSmaIdentifier(final SmaHolding smaHolding) {
    final SmaIdentifierType identifierType = SmaIdentifierType.valueOf(smaHolding.getHoldingIdentifier().name());
    return new SmaIdentifier(identifierType, smaHolding.getIdentifier());
  }

  @Override
  public SmaHolding findHoldingBasedOnRes(final List<SmaHolding> holdings,
      final SeparatelyManagedAccount separatelyManagedAccount) {
    final List<String> ids = getIds(separatelyManagedAccount);
    return holdings.stream()
        .filter(holding -> ids.contains(holding.getIdentifier()))
        .findFirst()
        .orElseThrow();
  }

  /**
   * Collect ids from the FDS response
   *
   * @param separatelyManagedAccount
   *          SeparatelyManagedAccount object from FDS
   * @return ids
   */
  private List<String> getIds(final SeparatelyManagedAccount separatelyManagedAccount) {
    final List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(separatelyManagedAccount
        .getExternalIdentifiers()).getCodes();
    return Objects.requireNonNull(codes).stream()
        .map(ExternalIdentifierTypeValue::getValue)
        .collect(Collectors.toList());
  }

  @Override
  public QueryQueryDefinition queryDefinition(final List<SmaIdentifier> smaIdentifiers,
      final UnaryOperator<SeparatelyManagedAccountQuery> preDefinedSMQuery) {
    return q -> q.getSeparatelyManagedAccountsBy(smaIdentifiers, preDefinedSMQuery::apply);
  }

  @Override
  public void populateEmptyResponseWithIdentifier(final List<SeparatelyManagedAccount> responses,
      final SmaHolding holding) {
    final SeparatelyManagedAccount separatelyManagedAccount = responses.stream()
        .filter(r -> isNull(r.getExternalIdentifiers()) || isEmpty(r.getExternalIdentifiers().getCodes()))
        .findFirst()
        .orElseThrow();

    final ExternalIdentifierType type = ExternalIdentifierType.valueOf(holding.getHoldingIdentifier().name());
    final var identifier = new ExternalIdentifierTypeValue()
        .setType(type)
        .setValue(holding.getIdentifier());

    var externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(identifier));
    separatelyManagedAccount.setExternalIdentifiers(externalIdentifiers);
  }

  @Override
  public List<SmaHolding> getNotExistingHoldings(final List<SmaHolding> holdings,
      final List<SeparatelyManagedAccount> separatelyManagedAccounts) {
    final List<String> identifiers = getIdentifiers(separatelyManagedAccounts);
    return holdings.stream()
        .filter(h -> !identifiers.contains(h.getIdentifier()))
        .collect(Collectors.toList());
  }

  /**
   * returns list of not null external identifiers from FDS response
   *
   * @param separatelyManagedAccounts
   *          response data from FDS
   * @return external identifiers from FDS response
   */
  private List<String> getIdentifiers(final List<SeparatelyManagedAccount> separatelyManagedAccounts) {
    return separatelyManagedAccounts.stream()
        .filter(r -> nonNull(r.getExternalIdentifiers()) && Objects.nonNull(r.getExternalIdentifiers().getCodes()))
        .map(SeparatelyManagedAccount::getExternalIdentifiers)
        .map(ExternalIdentifiers::getCodes)
        .flatMap(Collection::stream)
        .filter(externalIdentifier -> Objects.equals(externalIdentifier.getType(),
            ExternalIdentifierType.MORNINGSTAR_ID)
            || Objects.equals(externalIdentifier.getType(), ExternalIdentifierType.ENVESTNET_ID))
        .map(ExternalIdentifierTypeValue::getValue)
        .toList();
  }

}
