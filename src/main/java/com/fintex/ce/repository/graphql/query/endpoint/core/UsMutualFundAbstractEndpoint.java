package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.smclient.graphql.UsFund;
import com.fintex.smclient.graphql.UsFundQuery;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @param <Res> response object
 */
public abstract class UsMutualFundAbstractEndpoint<Res extends RedisId>
        extends AbstractSMEndpoint<UsMutualFundHolding, String, UsFundQuery, UsFund, Res> {

    public UsMutualFundAbstractEndpoint(Function<Query, List<UsFund>> getSMEntityFunction,
                                        List<DataProvider> supportedProviders,
                                        String endpointName) {
        super(getSMEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public QueryQueryDefinition queryDefinition(List<String> tickers, UnaryOperator<UsFundQuery> preDefinedSMQuery) {
        return q -> q.getUsFundsByTickers(tickers, preDefinedSMQuery::apply);
    }

    @Override
    public List<String> collectIds(List<UsMutualFundHolding> holdings) {
        var result = holdings.stream().map(UsMutualFundHolding::getTicker).collect(Collectors.toList());
        return result;
    }

    @Override
    UsMutualFundHolding findHoldingBasedOnRes(List<UsMutualFundHolding> holdings, UsFund usFund) {
        String ticker = Objects.requireNonNull(usFund.getTicker().getValue());
        return holdings.stream().filter(holding -> ticker.equals(holding.getTicker())).findFirst().orElseThrow();
    }


    @Override
    public Res basicResponseMapper(UsFund qEntity, UsMutualFundHolding holding) {
        Res response = responseMapper(qEntity, holding);
        response.setHoldingId(holding.generateUserIdentifier());
        return response;
    }

    @Override
    protected void populateEmptyResponseWithIdentifier(List<UsFund> responses, UsMutualFundHolding holding) {
        UsFund fund = responses.stream()
                .filter(r -> isNull(r.getTicker()) || isNull(r.getTicker().getValue()))
                .findFirst().orElseThrow();
        StringDatapoint tickerDatapoint = new StringDatapoint().setValue(holding.getTicker());
        fund.setTicker(tickerDatapoint);
    }

    /**
     * returns tickers that don't exist
     *
     * @param holdings            holdings from request payload
     * @param responses all not null tickers from FDS response
     * @return not existing tickers
     */
    protected List<UsMutualFundHolding> getNotExistingHoldings(List<UsMutualFundHolding> holdings,
                                                               List<UsFund> responses) {
        List<String> tickersFromResponse = getTickersFromResponse(responses);
        return holdings.stream()
                .filter(h -> !tickersFromResponse.contains(h.getTicker()))
                .collect(Collectors.toList());
    }

    /**
     * returns list of not null tickers from FDS response
     *
     * @param responses response data from FDS
     * @return tickers from FDS response
     */
    protected List<String> getTickersFromResponse(List<UsFund> responses) {
        return responses.stream()
                .filter(r -> nonNull(r.getTicker()) && nonNull(r.getTicker().getValue()))
                .map(etf -> etf.getTicker().getValue())
                .collect(Collectors.toList());
    }
}
