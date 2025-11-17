package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.Operations;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @param <H>       holding type
 * @param <QId>     id type to query SM
 * @param <QReq>    query request for SM
 * @param <QEntity> core entity from the SM response
 * @param <Res>     query response from SM
 */
@Slf4j
public abstract class AbstractSMEndpoint<H, QId, QReq, QEntity, Res> {

    @Getter
    final Function<Query, List<QEntity>> getSMEntityFunction;
    // default providers
    final List<DataProvider> supportedProviders;

    @Getter
    final String endpointName;

    List<DataProvider> userEnteredProviders;

    protected AbstractSMEndpoint(final Function<Query, List<QEntity>> getSMEntityFunction,
                               final List<DataProvider> supportedProviders,
                               final String endpointName) {
        this.getSMEntityFunction = getSMEntityFunction;
        this.supportedProviders = supportedProviders;
        this.endpointName = endpointName;
    }

    /**
     * IDs mapper
     *
     * @param holdings all holdings
     * @return id's
     */
    public abstract List<QId> collectIds(final List<H> holdings);

    /**
     * Find holding among other holdings based on response object from SM
     *
     * @param holdings all holdings
     * @param key      response from SM
     * @return holding
     */
    abstract H findHoldingBasedOnRes(List<H> holdings, QEntity key);

    /**
     * Defines SM endpoint and link it with user query
     *
     * @param ids                mapped holding ids
     * @param preDefinedSMQuery pre-defined query for SM
     * @return full SM query
     */
    public abstract QueryQueryDefinition queryDefinition(final List<QId> ids, final UnaryOperator<QReq> preDefinedSMQuery);

    /**
     * Creates SM query
     *
     * @param query SM query
     * @return pre-defined SM query
     */
    public abstract QReq requestMapper(final QReq query);

    /**
     * Mapper for SM response
     *
     * @param qEntity main object in the response from SM
     * @return mapped user object
     */
    public abstract Res responseMapper(final QEntity qEntity, final H holding);

    /**
     * Mapper for SM response
     * Uses to populate the basic properties
     *
     * @param qEntity main object in the response from SM
     * @return mapped user object
     */
    public Res basicResponseMapper(final QEntity qEntity, final H holding) {
        return responseMapper(qEntity, holding);
    }

    public Map<H, Res> collectResultToMap(final List<H> holdings, List<QEntity> responses) {
        final Map<H, Res> map = new HashMap<>();
        responses = populateIdentifiersIfEmpty(holdings, responses);
        for (QEntity response : responses) {
            final H holding = findHoldingBasedOnRes(holdings, response);
            if (Objects.isNull(holding)) {
                continue;
            }
            final Res userObject = basicResponseMapper(response, holding);
            map.put(holding, userObject);
        }
        return map;
    }

    /**
     * Populates identifiers (e.g. tickers, fund codes etc.) in response objects in case they are empty.
     *
     * @param holdings  holdings from request payload
     * @param responses response data from SM
     * @return responses with identifiers
     */
    protected List<QEntity> populateIdentifiersIfEmpty(final List<H> holdings, final List<QEntity> responses) {
        final List<H> holdingsThatDontHaveResponsesFromSm = getNotExistingHoldings(holdings, responses);
        populateEmptyResponsesWithIdentifiers(holdingsThatDontHaveResponsesFromSm, responses);
        return responses;
    }


    /**
     * checks if there are empty identifiers in response, if yes populates with not existing identifiers (taken from request)
     *
     * @param responses                            response data from SM
     * @param holdingsThatDontHaveResponsesFromSm tickers that don't exist
     */
    protected void populateEmptyResponsesWithIdentifiers(final List<H> holdingsThatDontHaveResponsesFromSm,
                                                         final List<QEntity> responses) {
        if (!CollectionUtils.isEmpty(holdingsThatDontHaveResponsesFromSm)) {
            log.warn("There are holdings that don't have data from sm. Populating response data with identifiers.");
            holdingsThatDontHaveResponsesFromSm.forEach(holding -> populateEmptyResponseWithIdentifier(responses, holding));
        }
    }

    protected abstract void populateEmptyResponseWithIdentifier(final List<QEntity> responses, final H holding);

    /**
     * returns holdings that don't exist on sm
     *
     * @param holdings  holdings from request payload
     * @param responses all not null tickers from SM response
     * @return not existing tickers
     */
    protected abstract List<H> getNotExistingHoldings(final List<H> holdings, final List<QEntity> responses);

    /**
     * Validates and loads data providers entered by user
     *
     * @return data providers
     */
    public List<com.fintex.smclient.graphql.DataProvider> loadProviders() {
        if (this.userEnteredProviders.isEmpty()) {
            return loadDefaultSupportedProviders();
        }
        List<com.fintex.smclient.graphql.DataProvider> filteredProviders = filterDataProviders();
        if (filteredProviders.isEmpty()) {
            throw new ReqValidationException(String.format("Provider(-s) %s is not supported by %s", userEnteredProviders, this.endpointName));
        }
        return filteredProviders;
    }

    public AbstractSMEndpoint<H, QId, QReq, QEntity, Res> setUserEnteredProviders(final List<DataProvider> userEnteredProviders) {
        this.userEnteredProviders = userEnteredProviders == null ? List.of() : userEnteredProviders;
        return this;
    }

    /**
     * Forces to load default data providers
     *
     * @return default data providers
     * @throws SystemException if ${this.supportedProviders} field is empty
     */
    List<com.fintex.smclient.graphql.DataProvider> loadDefaultSupportedProviders() {
        if (CollectionUtils.isEmpty(supportedProviders)) {
            throw new SystemException("There is no specified default providers", ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return supportedProviders.stream().map(p -> com.fintex.smclient.graphql.DataProvider.valueOf(p.name())).toList();
    }

    List<com.fintex.smclient.graphql.DataProvider> filterDataProviders() {
        List<com.fintex.smclient.graphql.DataProvider> customProviders = new ArrayList<>(3);
        for (DataProvider provider : this.userEnteredProviders) {
            if (this.supportedProviders.contains(provider)) {
                final com.fintex.smclient.graphql.DataProvider smProvider = com.fintex.smclient.graphql.DataProvider.valueOf(provider.name());
                customProviders.add(Objects.requireNonNull(smProvider));
            }
        }
        return customProviders;
    }

    public QueryQuery makeQuery(final List<H> holdings) {
        final List<QId> qIds = collectIds(holdings);
        return Operations.query(queryDefinition(qIds, this::requestMapper));
    }

}

