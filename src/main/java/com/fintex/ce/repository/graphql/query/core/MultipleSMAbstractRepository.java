package com.fintex.ce.repository.graphql.query.core;

import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.repository.graphql.query.endpoint.core.AbstractSMEndpoint;

import java.util.List;
import java.util.Map;

/**
 * @param <F> Fund Canada
 * @param <C> ETF Canada
 * @param <U> ETF US
 * @param <S> Stock
 */
public abstract class MultipleSMAbstractRepository<F, C, U, S> implements MultipleSMRepository<F, C, U, S> {

    final GraphqlTransportComponent graphqlTransport;

    protected MultipleSMAbstractRepository(GraphqlTransportComponent graphqlTransport) {
        this.graphqlTransport = graphqlTransport;
    }

    /**
     * Performs the actual request to SM
     *
     * @param <H>       holding type
     * @param <QId>     id type to query SM
     * @param <QReq>    query request for SM
     * @param <QEntity> core entity from the SM response
     * @param <Res>     user return type
     */
    public <H, QId, QReq, QEntity, Res> Map<H, Res> doQuery(final List<H> holdings,
                                                            final AbstractSMEndpoint<H, QId, QReq, QEntity, Res> endpoint,
                                                            final List<DataProvider> providers) {
        final QueryQuery queryQuery = endpoint.setUserEnteredProviders(providers).makeQuery(holdings);
        final List<QEntity> responses = graphqlTransport.query(queryQuery, endpoint.getSMEntityFunction());
        return endpoint.collectResultToMap(holdings, responses);
    }
}

