package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.PagGuidedPortfolio;
import com.fintex.ce.dto.holding.PagHolding;
import com.fintex.ce.model.redis.core.RedisId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagGuidedPortfolioAbstractEndpointTest {

    @Test
    void collectIds_checkResult() {
        //SETUP
        final PagGuidedPortfolioAbstractEndpoint sut = mock(PagGuidedPortfolioAbstractEndpoint.class);

        final String identifier = "PAG001";
        final PagHolding pagHolding = mock(PagHolding.class);

        when(pagHolding.getIdentifier()).thenReturn(identifier);

        doCallRealMethod().when(sut).collectIds(any());

        //ACT
        final List<String> actual = sut.collectIds(List.of(pagHolding));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(identifier, actual.get(0));
    }

    @Test
    void findHoldingBasedOnRes_checkResult() {
        //SETUP
        final String identifier = "PAG001";
        final PagGuidedPortfolioAbstractEndpoint sut = mock(PagGuidedPortfolioAbstractEndpoint.class);
        final PagHolding pagHolding = mock(PagHolding.class);
        final PagGuidedPortfolio pagGuidedPortfolio = mock(PagGuidedPortfolio.class);

        when(pagHolding.getIdentifier()).thenReturn(identifier);
        when(pagGuidedPortfolio.getIdentifier()).thenReturn(identifier);

        doCallRealMethod().when(sut).findHoldingBasedOnRes(any(), any());

        //ACT
        final PagHolding actual = sut.findHoldingBasedOnRes(List.of(pagHolding), pagGuidedPortfolio);

        //VERIFY
        assertEquals(pagHolding, actual);
    }

    @Test
    void basicResponseMapper_verifyResponseMapper() {
        //SETUP
        final PagGuidedPortfolioAbstractEndpoint sut = mock(PagGuidedPortfolioAbstractEndpoint.class);

        when(sut.responseMapper(any(), any())).thenReturn(mock(RedisId.class));

        final PagHolding pagHolding = mock(PagHolding.class);
        final PagGuidedPortfolio entity = mock(PagGuidedPortfolio.class);
        doCallRealMethod().when(sut).basicResponseMapper(any(), any());
        //ACT
        sut.basicResponseMapper(entity, pagHolding);

        //VERIFY
        verify(sut).responseMapper(entity, pagHolding);
    }

    @Test
    void populateEmptyResponseWithIdentifier_checkResult() {
        //SETUP
        final String identifier = "PAG001";
        final PagGuidedPortfolioAbstractEndpoint sut = mock(PagGuidedPortfolioAbstractEndpoint.class);
        final PagHolding pagHolding = mock(PagHolding.class);

        final PagGuidedPortfolio pagGuidedPortfolio = new PagGuidedPortfolio();
        final var holdings = List.of(pagGuidedPortfolio);

        Mockito.when(pagHolding.getIdentifier()).thenReturn(identifier);
        doCallRealMethod().when(sut).populateEmptyResponseWithIdentifier(anyList(), any());

        //ACT
        sut.populateEmptyResponseWithIdentifier(holdings, pagHolding);

        //VERIFY
        assertNotNull(pagGuidedPortfolio.getIdentifier());
        assertEquals(identifier, pagGuidedPortfolio.getIdentifier());
    }

    @Test
    void getNotExistingHoldings_checkResult() {
        //SETUP
        final String identifier = "PAG001";
        final String holdingIdentifier1 = "PAG001";
        final String holdingIdentifier2 = "PAGH002";

        final PagGuidedPortfolioAbstractEndpoint sut = mock(PagGuidedPortfolioAbstractEndpoint.class);
        final PagHolding pagHolding1 = mock(PagHolding.class);
        final PagHolding pagHolding2 = mock(PagHolding.class);
        final PagGuidedPortfolio pagGuidedPortfolio = mock(PagGuidedPortfolio.class);

        when(pagHolding1.getIdentifier()).thenReturn(holdingIdentifier1);
        when(pagHolding2.getIdentifier()).thenReturn(holdingIdentifier2);
        when(pagGuidedPortfolio.getIdentifier()).thenReturn(identifier);

        doCallRealMethod().when(sut).getNotExistingHoldings(any(), any());

        //ACT
        final List<PagHolding> actual = sut.getNotExistingHoldings(
                List.of(pagHolding1, pagHolding2),
                List.of(pagGuidedPortfolio));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(pagHolding2, actual.get(0));
    }

}
