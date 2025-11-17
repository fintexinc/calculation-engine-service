package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class StockAbstractEndpoint<Res extends RedisId>
        extends AbstractSMEndpoint<StockHolding, EquityIdentifiers, StockQuery, Stock, Res> {

    public StockAbstractEndpoint(Function<Query, List<Stock>> getSMEntityFunction,
                                 final List<DataProvider> supportedProviders,
                                 final String endpointName) {
        super(getSMEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<EquityIdentifiers> equityIdentifiers, UnaryOperator<StockQuery> preDefinedSMQuery) {
        return q -> q.getStocksByTickersAndExchangeIds(equityIdentifiers, preDefinedSMQuery::apply);
    }

    @Override
    public List<EquityIdentifiers> collectIds(final List<StockHolding> holdings) {
        return holdings.stream().map(h -> new EquityIdentifiers(h.getExchangeCode(), h.getTicker())).toList();
    }

    @Override
    StockHolding findHoldingBasedOnRes(final List<StockHolding> holdings, final Stock stock) {
        final List<String> ids = getIds(stock);
        return holdings.stream()
                .filter(holding -> ids.contains(holding.getTicker()) && ids.contains(holding.getExchangeCode()))
                .findFirst().orElseThrow();
    }

    /**
     * Collect ids from the FDS response
     *
     * @param stock stock object from FDS
     * @return ids
     */
    List<String> getIds(final Stock stock) {
        final List<ExternalIdentifierTypeValue> codes = Objects.requireNonNull(stock.getExternalIdentifiers()).getCodes();
        return Objects.requireNonNull(codes).stream().map(ExternalIdentifierTypeValue::getValue).toList();
    }

    @Override
    public Res basicResponseMapper(final Stock qEntity, final StockHolding holding) {
        final Res response = responseMapper(qEntity, holding);
        response.setHoldingId(holding.generateUserIdentifier());
        return response;
    }

    @Override
    protected void populateEmptyResponseWithIdentifier(final List<Stock> responses, final StockHolding holding) {
        final Stock stock = responses.stream()
                .filter(r -> isNull(r.getExternalIdentifiers()) || isEmpty(r.getExternalIdentifiers().getCodes()))
                .findFirst().orElseThrow();
        final var ticker = new ExternalIdentifierTypeValue();
        ticker.setValue(holding.getTicker());
        final var exchangeCode = new ExternalIdentifierTypeValue();
        exchangeCode.setValue(holding.getExchangeCode());

        final ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers().setCodes(List.of(ticker, exchangeCode));
        stock.setExternalIdentifiers(externalIdentifiers);
    }


    @Override
    protected List<StockHolding> getNotExistingHoldings(final List<StockHolding> holdings, final List<Stock> responses) {
        final List<List<String>> responseIds = responses.stream().map(this::getIds).toList();
        return holdings
                .stream()
                .filter(h -> holdingsThatDontHaveCorrespondingResponseFromFds(responseIds, h))
                .toList();
    }

    private boolean holdingsThatDontHaveCorrespondingResponseFromFds(final List<List<String>> responseIds, final StockHolding h) {
        return responseIds.stream().noneMatch(r -> r.contains(h.getTicker()) && r.contains(h.getExchangeCode()));
    }
}
