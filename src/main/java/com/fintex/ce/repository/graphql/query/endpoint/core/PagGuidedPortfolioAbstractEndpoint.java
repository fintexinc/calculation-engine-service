package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.PagGuidedPortfolio;
import com.fintex.smclient.graphql.PagGuidedPortfolioQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.model.redis.core.RedisId;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;

public abstract class PagGuidedPortfolioAbstractEndpoint<Res extends RedisId>
        extends AbstractSMEndpoint<PagHolding, String, PagGuidedPortfolioQuery, PagGuidedPortfolio, Res> {

    public PagGuidedPortfolioAbstractEndpoint(final Function<Query, List<PagGuidedPortfolio>> getSMEntityFunction,
                                              final List<DataProvider> supportedProviders,
                                              final String endpointName) {
        super(getSMEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public List<String> collectIds(final List<PagHolding> holdings) {
        return holdings.stream()
                .map(this::getPagIdentifier)
                .toList();
    }

    private String getPagIdentifier(final PagHolding pagHolding) {
        return pagHolding.getIdentifier();
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> pagIds,
                                                final UnaryOperator<PagGuidedPortfolioQuery> preDefinedSMQuery) {
        return q -> q.getPagGuidedPortfolios(pagIds, preDefinedSMQuery::apply);
    }

    @Override
    public Res basicResponseMapper(final PagGuidedPortfolio pagModel,
                                   final PagHolding holding) {
        final Res response = responseMapper(pagModel, holding);
        response.setHoldingId(holding.generateUserIdentifier());
        return response;
    }

    @Override
    protected void populateEmptyResponseWithIdentifier(final List<PagGuidedPortfolio> responses,
                                                       final PagHolding holding) {
        final PagGuidedPortfolio pagGuidedPortfolio = responses.stream()
                .filter(r -> isNull(r.getIdentifier()))
                .findFirst()
                .orElseThrow();

        pagGuidedPortfolio.setIdentifier(holding.getIdentifier());
    }

    @Override
    protected List<PagHolding> getNotExistingHoldings(final List<PagHolding> holdings,
                                                      final List<PagGuidedPortfolio> pagGuidedPortfolio) {
        final List<String> identifiers = getIdentifiers(pagGuidedPortfolio);
        return holdings.stream()
                .filter(h -> !identifiers.contains(h.getIdentifier()))
                .toList();
    }

    /**
     * returns list of not null external identifiers from FDS response
     *
     * @param performanceAdvisoryGroupModels response data from FAS
     * @return string identifiers from FAS response
     */
    private List<String> getIdentifiers(final List<PagGuidedPortfolio> performanceAdvisoryGroupModels) {
        return performanceAdvisoryGroupModels.stream()
                .map(PagGuidedPortfolio::getIdentifier)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    PagHolding findHoldingBasedOnRes(final List<PagHolding> holdings,
                                     final PagGuidedPortfolio pagGuidedPortfolio) {
        return holdings.stream()
                .filter(holding -> pagGuidedPortfolio.getIdentifier().equals(holding.getIdentifier()))
                .findFirst()
                .orElseThrow();
    }

}
